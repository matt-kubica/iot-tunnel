/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.stub;

import com.mkubica.managementservice.provider.cert.CertificateProvider;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StubCertificateProvider implements CertificateProvider {

    @Override
    public Try<String> obtainCACert() {
        log.debug("{} -> obtainCACert()", this.getClass().getSimpleName());
        return Try.success("some-ca-cert");
    }

    @Override
    public Try<String> obtainTAKey() {
        log.debug("{} -> obtainTAKey()", this.getClass().getSimpleName());
        return Try.success("some-ta-key");
    }
}
