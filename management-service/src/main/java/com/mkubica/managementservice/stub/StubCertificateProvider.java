/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.stub;

import com.mkubica.managementservice.provider.cert.CertificateProvider;

import io.vavr.control.Try;

public class StubCertificateProvider implements CertificateProvider {

    @Override
    public Try<String> obtainCACert() {
        return Try.success("some-ca-cert");
    }

    @Override
    public Try<String> obtainTAKey() {
        return Try.success("some-ta-key");
    }
}
