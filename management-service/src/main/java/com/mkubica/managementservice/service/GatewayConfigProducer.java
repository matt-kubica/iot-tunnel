/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.provider.cert.CertificateProvider;
import com.mkubica.managementservice.provider.TemplateProvider;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class GatewayConfigProducer {

    private final String ovpnExternalAddress;
    private final String ovpnExternalPort;
    private final TemplateProvider templateProvider;
    private final CertificateProvider certificateProvider;

    public GatewayConfigModel from(GatewayEntity entity) {
        String template = templateProvider.obtainTemplate("static/base.conf");
        return GatewayConfigModel
                .builder()
                .withTemplate(template)
                .withExternalAddress(ovpnExternalAddress)
                .withExternalPort(ovpnExternalPort)
                // TODO: right now there is simply Try.get(), but it should be somehow validated whether there was an error
                .withCaCertificate(certificateProvider.obtainCACert().get())
                .withTlsAuthKey(certificateProvider.obtainTAKey().get())
                .withCertificate(entity.getCertificate())
                .withPrivateKey(entity.getPrivateKey())
                .build();
    }
}
