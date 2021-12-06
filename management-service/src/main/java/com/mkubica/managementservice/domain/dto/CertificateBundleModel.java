/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.domain.dto;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonRootName;

@Data
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(setterPrefix = "with", toBuilder = true)
@JsonRootName(value = "certificate-bundle")
public class CertificateBundleModel {

    private final String certificate;
    private final String privateKey;

}
