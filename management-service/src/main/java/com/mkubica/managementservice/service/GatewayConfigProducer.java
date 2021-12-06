/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.provider.TemplateProvider;
import com.mkubica.managementservice.provider.cert.CertificateProvider;

import lombok.AllArgsConstructor;

import io.vavr.control.Try;


@AllArgsConstructor
public class GatewayConfigProducer {

    private final String ovpnExternalAddress;
    private final String ovpnExternalPort;
    private final TemplateProvider templateProvider;
    private final CertificateProvider certificateProvider;

    public Try<GatewayConfigModel> produceFrom(GatewayEntity entity) {
        return templateProvider.obtainTemplate("static/base.conf").mapTry(template ->
                GatewayConfigModel
                        .builder()
                        .withTemplate(template)
                        .withExternalAddress(ovpnExternalAddress)
                        .withExternalPort(ovpnExternalPort)
                        .withCaCertificate(certificateProvider.obtainCACert().get())
                        .withTlsAuthKey(certificateProvider.obtainTAKey().get())
                        .withCertificate(entity.getCertificate())
                        .withPrivateKey(entity.getPrivateKey())
                        .build()
        );
    }
}
