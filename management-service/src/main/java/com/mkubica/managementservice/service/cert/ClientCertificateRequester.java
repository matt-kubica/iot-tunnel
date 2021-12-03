package com.mkubica.managementservice.service.cert;

import com.mkubica.managementservice.domain.dto.CertificateBundleModel;
import io.vavr.control.Try;


public interface ClientCertificateRequester {

    // TODO: consider using vavr.Future
    Try<CertificateBundleModel> requestCertificate(String commonName);
}
