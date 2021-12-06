/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.domain.dto;

import com.mkubica.managementservice.domain.dao.GatewayEntity;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonRootName;

@Data
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(setterPrefix = "with", toBuilder = true)
@JsonRootName(value = "gateway")
public class GatewayModel {

    private final String commonName;
    private final String ipAddress;
    private final String certificate;
    private final String privateKey;

    public static GatewayModel from(GatewayEntity entity) {
        return GatewayModel
            .builder()
            .withCommonName(entity.getCommonName())
            .withIpAddress(entity.getIpAddress())
            .withCertificate(entity.getCertificate())
            .withPrivateKey(entity.getPrivateKey())
            .build();
    }
}
