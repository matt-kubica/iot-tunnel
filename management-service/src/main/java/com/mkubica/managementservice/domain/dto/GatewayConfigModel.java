/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.domain.dto;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonRootName;

@Data
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(setterPrefix = "with", toBuilder = true)
@JsonRootName(value = "gateway-config")
public class GatewayConfigModel {

    private final String template;
    private final String certificate;
    private final String privateKey;
    private final String caCertificate;
    private final String tlsAuthKey;
    private final String externalAddress;
    private final String externalPort;

    public String makeString() {
        return new StringBuilder(template)
            .append(String.format("\nremote %s %s\n", externalAddress, externalPort))
            .append(tag("ca", caCertificate))
            .append(tag("cert", certificate))
            .append(tag("key", privateKey))
            .append(tag("tls-crypt", tlsAuthKey))
            .toString();
    }

    private static String tag(String tag, String payload) {
        return String.format("<%s>\n%s</%s>\n", tag, payload, tag);
    }
}
