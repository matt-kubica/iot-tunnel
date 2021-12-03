/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

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
