package com.mkubica.managementservice.provider.cert;

import io.vavr.control.Try;


public interface CertificateProvider {

    Try<String> obtainCACert();
    Try<String> obtainTAKey();

}
