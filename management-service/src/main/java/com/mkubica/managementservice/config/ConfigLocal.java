/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.config;

import com.mkubica.managementservice.provider.TemplateProvider;
import com.mkubica.managementservice.provider.cert.CertificateProvider;
import com.mkubica.managementservice.repository.GatewayRepository;
import com.mkubica.managementservice.service.GatewayConfigProducer;
import com.mkubica.managementservice.service.GatewayService;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import com.mkubica.managementservice.service.ip.IpAssigner;
import com.mkubica.managementservice.stub.StubCertificateProvider;
import com.mkubica.managementservice.stub.StubClientCertificateRequester;
import com.mkubica.managementservice.stub.StubIpAssigner;

import com.mkubica.managementservice.transaction.TransactionChainExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class ConfigLocal {

    @Bean
    public TemplateProvider templateProvider() {
        return new TemplateProvider();
    }

    @Bean
    public ClientCertificateRequester clientCertificateRequester() {
        return new StubClientCertificateRequester();
    }

    @Bean
    public CertificateProvider certificateProvider() {
        return new StubCertificateProvider();
    }

    @Bean
    public IpAssigner ipAssigner() {
        return new StubIpAssigner();
    }

    @Bean
    public GatewayConfigProducer gatewayConfigProducer(
            @Value("${defaults.open-vpn.external-address}") String ovpnExternalAddress,
            @Value("${defaults.open-vpn.external-port}") String ovpnExternalPort,
            TemplateProvider templateProvider,
            CertificateProvider certificateProvider
    ) {
        return new GatewayConfigProducer(ovpnExternalAddress, ovpnExternalPort, templateProvider, certificateProvider);
    }

    @Bean
    public GatewayService gatewayService(
            GatewayConfigProducer gatewayConfigProducer,
            GatewayRepository gatewayRepository,
            ClientCertificateRequester clientCertificateRequester,
            IpAssigner ipAssigner,
            TransactionChainExecutor transactionChainExecutor
    ) {
        return new GatewayService(
                gatewayConfigProducer,
                gatewayRepository,
                clientCertificateRequester,
                ipAssigner,
                transactionChainExecutor
        );
    }

    @Bean
    public TransactionChainExecutor chainExecutor() {
        return new TransactionChainExecutor();
    }
}
