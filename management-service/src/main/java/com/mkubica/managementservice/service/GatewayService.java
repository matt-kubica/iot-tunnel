/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import static java.lang.String.format;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.exception.CommonNameBlankException;
import com.mkubica.managementservice.exception.CommonNameNotUniqueException;
import com.mkubica.managementservice.exception.IpAddressStringInvalidException;
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

    public Try<GatewayConfigModel> getGatewayConfig(GatewayModel model) {
        return gatewayRepository.getGatewayEntityByCommonName(model.getCommonName())
                .toTry().flatMap(gatewayConfigProducer::produceFrom);
    }

    public Try<GatewayModel> getGateway(GatewayModel model) {
        return gatewayRepository.getGatewayEntityByCommonName(model.getCommonName())
                .map(GatewayModel::from)
                .toTry();
    }

    public Try<GatewayModel> createGateway(GatewayModel initialModel) {
        return Try.of(() -> GatewayModel.builder()
                    .withCommonName(validateCommonName(initialModel).get().getCommonName())
                    .withIpAddress(validateIpAddressString(initialModel).get().getIpAddress())
                    .build())
                .flatMap(model -> ipAssigner
                        .assignIp(model.getCommonName(), model.getIpAddress())
                        .map(assignedIp -> model.toBuilder().withIpAddress(assignedIp).build()))
                .flatMap(model -> clientCertificateRequester
                        .requestBundle(model.getCommonName())
                        .map(bundle -> GatewayModel.builder()
                                .withCommonName(model.getCommonName())
                                .withIpAddress(model.getIpAddress())
                                .withCertificate(bundle.getCertificate())
                                .withPrivateKey(bundle.getPrivateKey())
                                .build()))
                .map(GatewayEntity::from)
                .map(gatewayRepository::save)
                .map(GatewayModel::from);
    }

    public Try<GatewayModel> deleteGateway(GatewayModel initialModel) {
        return Try.success(initialModel)
                .flatMap(model -> gatewayRepository.getGatewayEntityByCommonName(model.getCommonName()).toTry())
                .map(GatewayModel::from)
                .flatMap(model -> ipAssigner
                        .revokeIp(model.getCommonName())
                        .map(revokedIp -> model.toBuilder().withIpAddress(revokedIp).build()))
                .flatMap(model -> clientCertificateRequester.revokeBundle(model.getCommonName()).map(x -> model))
                .map(GatewayEntity::from)
                .andThen(gatewayRepository::delete)
                .map(GatewayModel::from);
    }

    private Try<GatewayModel> validateCommonName(GatewayModel model) {
        return gatewayRepository.getGatewayEntityByCommonName(model.getCommonName()).isEmpty()
                ? !Strings.isNullOrEmpty(model.getCommonName()) && !model.getCommonName().isBlank()
                    ? Try.success(model)
                    : Try.failure(new CommonNameBlankException())
                : Try.failure(new CommonNameNotUniqueException(
                        format("Gateway with common name '%s' already exist", model.getCommonName())));
    }

    private Try<GatewayModel> validateIpAddressString(GatewayModel model) {
        return model.getIpAddress() != null
                ? InetAddresses.isInetAddress(model.getIpAddress())
                    ? Try.success(model)
                    : Try.failure(new IpAddressStringInvalidException())
                : Try.success(model);
    }
}
