/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.domain.dao;

import com.mkubica.managementservice.domain.dto.GatewayModel;
import javax.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gateways")
public class GatewayEntity {

    @Id
    private String commonName;
    @Lob
    private String certificate;
    @Lob
    private String privateKey;

    private String ipAddress;

    public static GatewayEntity from(GatewayModel model) {
        return GatewayEntity
            .builder()
            .withCommonName(model.getCommonName())
            .withIpAddress(model.getIpAddress())
            .withCertificate(model.getCertificate())
            .withPrivateKey(model.getPrivateKey())
            .build();
    }
}
