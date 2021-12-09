/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.config;

import com.mkubica.managementservice.provider.TemplateProvider;
import com.mkubica.managementservice.provider.cert.CertificateProvider;
import com.mkubica.managementservice.provider.cert.SharedVolumeCertificateProvider;
import com.mkubica.managementservice.repository.GatewayRepository;
import com.mkubica.managementservice.service.GatewayConfigProducer;
import com.mkubica.managementservice.service.GatewayService;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import com.mkubica.managementservice.service.cert.DefaultClientCertificateRequester;
import com.mkubica.managementservice.service.ip.IpAssigner;
import com.mkubica.managementservice.service.ip.SharedVolumeIpAssigner;
import com.mkubica.managementservice.transaction.TransactionChainExecutor;
import com.mkubica.managementservice.util.HttpUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("remote")
public class ConfigRemote {

    @Bean
    public TemplateProvider templateProvider() {
        return new TemplateProvider();
    }

    @Bean
    public HttpUtil httpUtil() {
        return new HttpUtil();
    }

    @Bean
    public ClientCertificateRequester clientCertificateRequester(
            @Value("${endpoints.certificate-authority.new-cert}") String newCertEndpoint,
            TemplateProvider templateProvider,
            HttpUtil httpUtil
    ) {
        return new DefaultClientCertificateRequester(newCertEndpoint, templateProvider, httpUtil);
    }

    @Bean
    public CertificateProvider certificateProvider(
            @Value("${paths.certificate-authority.ca-cert}") String caCertPath,
            @Value("${paths.open-vpn.ta-key}") String taKeyPath
    ) {
        return new SharedVolumeCertificateProvider(caCertPath, taKeyPath);
    }

    @Bean
    public IpAssigner ipAssigner(
            @Value("${paths.open-vpn.client-config-directory}") String ccdPath,
            GatewayRepository gatewayRepository
    ) {
        return new SharedVolumeIpAssigner(ccdPath, gatewayRepository);
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
