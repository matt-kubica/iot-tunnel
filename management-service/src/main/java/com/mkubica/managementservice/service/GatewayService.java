/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.domain.dto.GatewaySimplifiedModel;
import com.mkubica.managementservice.repository.GatewayRepository;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import com.mkubica.managementservice.service.ip.IpAssigner;

import com.mkubica.managementservice.transaction.*;
import lombok.RequiredArgsConstructor;

import io.vavr.control.Try;


@RequiredArgsConstructor
public class GatewayService {

    private final GatewayConfigProducer gatewayConfigProducer;
    private final GatewayRepository gatewayRepository;
    private final ClientCertificateRequester clientCertificateRequester;
    private final IpAssigner ipAssigner;

    public Try<GatewayConfigModel> getGatewayConfig(String commonName) {
        return gatewayRepository.getGatewayEntityByCommonName(commonName)
                .toTry().flatMap(gatewayConfigProducer::produceFrom);
    }

    public Try<GatewayModel> getGateway(String commonName) {
        return gatewayRepository.getGatewayEntityByCommonName(commonName)
                .map(GatewayModel::from)
                .toTry();
    }

    public Try<GatewayModel> createGateway(GatewaySimplifiedModel initialModel) {
        GatewayModel model = GatewayModel.builder()
                .withCommonName(initialModel.getCommonName())
                .withIpAddress(initialModel.getIpAddress())
                .build();

        return new ChainExecutor<GatewayModel>().execute(model,
                new TransactionalGatewayModelValidator(gatewayRepository),
                new TransactionalIpAssigner(ipAssigner),
                new TransactionalClientCertificateRequester(clientCertificateRequester)
        ).map(GatewayEntity::from).map(gatewayRepository::save).map(GatewayModel::from);
    }

    public void deleteGateway(String commonName) {
        ipAssigner.revokeIp(commonName);
        gatewayRepository.deleteByCommonName(commonName);
    }
}
