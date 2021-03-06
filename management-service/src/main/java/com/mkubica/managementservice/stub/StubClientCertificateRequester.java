/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.stub;

import com.mkubica.managementservice.domain.dto.CertificateBundleModel;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;

import io.vavr.control.Try;

public class StubClientCertificateRequester implements ClientCertificateRequester {

    @Override
    public Try<CertificateBundleModel> requestBundle(String commonName) {
        return Try.of(() -> CertificateBundleModel.builder()
                .withCertificate("some-certificate")
                .withPrivateKey("some-private-key")
                .build()
        );
    }

    @Override
    public Try<Void> revokeBundle(String commonName) {
        return Try.success(null);
    }
}
