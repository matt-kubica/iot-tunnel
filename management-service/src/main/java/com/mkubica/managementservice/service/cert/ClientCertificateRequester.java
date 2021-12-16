/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.cert;

import com.mkubica.managementservice.domain.dto.CertificateBundleModel;

import io.vavr.control.Try;


public interface ClientCertificateRequester {

    Try<CertificateBundleModel> requestBundle(String commonName);
    Try<Void> revokeBundle(String commonName);
}
