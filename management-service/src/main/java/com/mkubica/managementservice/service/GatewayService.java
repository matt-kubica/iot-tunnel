/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.domain.dto.GatewaySimplifiedModel;
import com.mkubica.managementservice.exception.CommonNameNotUniqueException;
import com.mkubica.managementservice.exception.IpAddressNotUniqueException;
import com.mkubica.managementservice.repository.GatewayRepository;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import com.mkubica.managementservice.service.ip.IpAssigner;


import com.mkubica.managementservice.transaction.TransactionChain;
import com.mkubica.managementservice.transaction.TransactionChainExecutor;
import lombok.RequiredArgsConstructor;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import static com.mkubica.managementservice.transaction.Transaction.*;
import static java.lang.String.format;


@Slf4j
@RequiredArgsConstructor
public class GatewayService {

    private final GatewayConfigProducer gatewayConfigProducer;
    private final GatewayRepository gatewayRepository;
    private final ClientCertificateRequester clientCertificateRequester;
    private final IpAssigner ipAssigner;
    private final TransactionChainExecutor transactionChainExecutor;

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
        GatewayModel dto = GatewayModel.builder()
                .withCommonName(initialModel.getCommonName())
                .withIpAddress(initialModel.getIpAddress())
                .build();

        return transactionChainExecutor.executeChain(dto, TransactionChain.of(
                pure(this::validateCommonName),
                conditional(dto.getIpAddress() != null, pure(this::validateIpAddress)),
                alternative(dto.getIpAddress() != null,
                        dirty(this::assignIp, this::revokeIp),
                        dirty(this::assignRandomIp, this::revokeIp)
                ),
                dirty(this::requestBundle, this::revokeBundle)
        )).map(GatewayEntity::from).map(gatewayRepository::save).map(GatewayModel::from);
    }

    public void deleteGateway(String commonName) {
         ipAssigner.revokeIp(commonName);
         gatewayRepository.deleteByCommonName(commonName);
    }

    private Try<GatewayModel> assignIp(GatewayModel model) {
        return ipAssigner
                .assignIp(model.getCommonName(), model.getIpAddress())
                .map(assignedIp -> model.toBuilder()
                        .withIpAddress(assignedIp)
                        .build());
    }

    private Try<GatewayModel> assignRandomIp(GatewayModel model){
        return ipAssigner
                .assignRandomIp(model.getCommonName())
                .map(assignedIp -> model.toBuilder()
                        .withIpAddress(assignedIp)
                        .build());
    }

    private Try<GatewayModel> requestBundle(GatewayModel model){
        return clientCertificateRequester
                .requestBundle(model.getCommonName())
                .map(bundle -> model.toBuilder()
                        .withCertificate(bundle.getCertificate())
                        .withPrivateKey(bundle.getPrivateKey())
                        .build());
    }

    private Try<Void> revokeIp(GatewayModel model) {
        return ipAssigner.revokeIp(model.getCommonName());
    }

    private Try<Void> revokeBundle(GatewayModel model) {
        return clientCertificateRequester.revokeBundle(model.getCommonName());
    }

    private Try<GatewayModel> validateCommonName(GatewayModel model) {
        log.debug("{} -> validateCommonName({})", this.getClass().getSimpleName(), model);
        return gatewayRepository.getGatewayEntityByCommonName(model.getCommonName()).isEmpty()
                ? Try.success(model)
                : Try.failure(new CommonNameNotUniqueException(
                format("Gateway with common name '%s' already exist", model.getCommonName())));
    }

    private Try<GatewayModel> validateIpAddress(GatewayModel model) {
        log.debug("{} -> validateIpAddress({})", this.getClass().getSimpleName(), model);
        return gatewayRepository.getGatewayEntityByIpAddress(model.getIpAddress()).isEmpty()
                ? Try.success(model)
                : Try.failure(new IpAddressNotUniqueException(
                format("Ip address '%s' already allocated", model.getIpAddress())));
    }
}
