/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import static java.lang.String.format;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.domain.dto.GatewaySimplifiedModel;
import com.mkubica.managementservice.exception.CommonNameNotUniqueException;
import com.mkubica.managementservice.exception.IpAddressNotUniqueException;
import com.mkubica.managementservice.repository.GatewayRepository;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import com.mkubica.managementservice.service.ip.IpAssigner;

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
        return Try.of(() -> GatewaySimplifiedModel.builder()
                    .withCommonName(validateCommonName(initialModel).get().getCommonName())
                    .withIpAddress(validateIpAddress(initialModel).get().getIpAddress())
                    .build())
                .flatMap(simplifiedModel -> ipAssigner
                        .assignIp(simplifiedModel.getCommonName(), simplifiedModel.getIpAddress())
                        .map(assignedIp -> simplifiedModel)
                )
                .flatMap(simplifiedModel -> clientCertificateRequester
                        .requestBundle(simplifiedModel.getCommonName())
                        .map(bundle -> GatewayModel.builder()
                                .withCommonName(simplifiedModel.getCommonName())
                                .withIpAddress(simplifiedModel.getIpAddress())
                                .withCertificate(bundle.getCertificate())
                                .withPrivateKey(bundle.getPrivateKey())
                                .build()
                        )
                )
                .map(GatewayEntity::from)
                .map(gatewayRepository::save)
                .map(GatewayModel::from);
                // TODO: what if something fails in the end?
    }

    public void deleteGateway(String commonName) {
        ipAssigner.revokeIp(commonName);
        gatewayRepository.deleteByCommonName(commonName);
    }

    private Try<GatewaySimplifiedModel> validateCommonName(GatewaySimplifiedModel model) {
        return gatewayRepository.getGatewayEntityByCommonName(model.getCommonName()).isEmpty()
                ? Try.success(model)
                : Try.failure(new CommonNameNotUniqueException(
                        format("Gateway with common name '%s' already exist", model.getCommonName())));
    }

    private Try<GatewaySimplifiedModel> validateIpAddress(GatewaySimplifiedModel model) {
        return gatewayRepository.getGatewayEntityByIpAddress(model.getIpAddress()).isEmpty()
                ? Try.success(model)
                : Try.failure(new IpAddressNotUniqueException(
                        format("Ip address '%s' already allocated", model.getIpAddress())));
    }
}
