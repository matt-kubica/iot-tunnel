/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.stub;

import com.mkubica.managementservice.domain.dto.CertificateBundleModel;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StubClientCertificateRequester implements ClientCertificateRequester {

    @Override
    public Try<CertificateBundleModel> requestBundle(String commonName) {
        log.debug("{} -> requestBundle({})", this.getClass().getSimpleName(), commonName);
        return Try.of(() -> CertificateBundleModel.builder()
                .withCertificate("some-certificate")
                .withPrivateKey("some-private-key")
                .build()
        );
    }

    @Override
    public Try<Void> revokeBundle(String commonName) {
        log.debug("{} -> revokeBundle({})", this.getClass().getSimpleName(), commonName);
        return Try.success(null);
    }
}
