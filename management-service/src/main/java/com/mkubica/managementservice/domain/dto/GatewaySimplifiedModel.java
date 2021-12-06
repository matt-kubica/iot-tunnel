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
@JsonRootName(value = "gateway-simplified")
public class GatewaySimplifiedModel {

    private final String commonName;
    private final String ipAddress;

    public static GatewaySimplifiedModel from(GatewayEntity entity) {
        return GatewaySimplifiedModel
            .builder()
            .withCommonName(entity.getCommonName())
            .withIpAddress(entity.getIpAddress())
            .build();
    }
}
