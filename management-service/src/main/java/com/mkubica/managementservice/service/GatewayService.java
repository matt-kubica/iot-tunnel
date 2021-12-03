/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.domain.dto.GatewaySimplifiedModel;
import com.mkubica.managementservice.repository.GatewayRepository;

import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import com.mkubica.managementservice.service.ip.IpAssigner;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GatewayService {

    private final GatewayConfigProducer gatewayConfigProducer;
    private final GatewayRepository gatewayRepository;
    private final ClientCertificateRequester clientCertificateRequester;
    private final IpAssigner ipAssigner;

    public GatewayConfigModel getGatewayConfig(String commonName) {
        GatewayEntity entity = gatewayRepository
            .getGatewayEntityByCommonName(commonName)
            .orElseThrow(
                () -> new RuntimeException(String.format("Not found gateway with '%s' common name", commonName))
            );

        return gatewayConfigProducer.from(entity);
    }

    public GatewayModel getGateway(String commonName) {
        GatewayEntity entity = gatewayRepository
            .getGatewayEntityByCommonName(commonName)
            .orElseThrow(
                () -> new RuntimeException(String.format("Not found gateway with '%s' common name", commonName))
            );

        return GatewayModel.from(entity);
    }

    public void createGateway(GatewaySimplifiedModel model) {
        var builder = GatewayEntity.builder();

        // check whether gateway with such common name exist
        if (gatewayRepository.getGatewayEntityByCommonName(model.getCommonName()).isPresent()) {
            throw new RuntimeException(
                String.format("Gateway with common name '%s' already exist", model.getCommonName())
            );
        }
        builder.withCommonName(model.getCommonName());

        // allocate ip address
        if (model.getIpAddress() != null) {
            if (gatewayRepository.getGatewayEntityByIpAddress(model.getIpAddress()).isPresent()) {
                throw new RuntimeException(String.format("Ip address '%s' already allocated", model.getIpAddress()));
            }
            ipAssigner.assignIp(model.getCommonName(), model.getIpAddress());
            builder.withIpAddress(model.getIpAddress());
        } else {
            // TODO: try from below should be validated
            String ipAddress = ipAssigner.assignRandomIp(model.getCommonName()).get();
            builder.withIpAddress(ipAddress);
        }

        // generate certificate and private key
        var bundle = clientCertificateRequester.requestCertificate(model.getCommonName()).get();
        builder.withPrivateKey(bundle.getPrivateKey()).withCertificate(bundle.getCertificate());

        gatewayRepository.save(builder.build());
    }

    public void deleteGateway(String commonName) {
        ipAssigner.revokeIp(commonName);
        gatewayRepository.deleteByCommonName(commonName);
    }
}
