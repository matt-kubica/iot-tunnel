package com.mkubica.managementservice.service

import com.mkubica.managementservice.domain.dao.GatewayEntity
import com.mkubica.managementservice.provider.TemplateProvider
import com.mkubica.managementservice.provider.cert.CertificateProvider
import io.vavr.control.Try
import spock.lang.Specification

class GatewayConfigProducerSpec extends Specification {

    private TemplateProvider templateProvider = Stub()
    private CertificateProvider certificateProvider = Stub()
    private GatewayConfigProducer gatewayConfigProducer
            = new GatewayConfigProducer("localhost", "443", templateProvider, certificateProvider)
    private static GatewayEntity gatewayEntity

    def setupSpec() {
        gatewayEntity = GatewayEntity.builder()
                .withCommonName("some-common-name")
                .withIpAddress("10.8.0.5 10.8.0.6")
                .withPrivateKey("some-private-key")
                .withCertificate("some-certificate")
                .build()
    }

    def "produce GatewayConfigModel from GatewayEntity"() {
        given:
            templateProvider.obtainTemplate("static/base.conf") >> Try.success(CONFIG_BASE)
            certificateProvider.obtainCACert() >> Try.success("some-ca-cert")
            certificateProvider.obtainTAKey() >> Try.success("some-ta-key")

        when:
            def res = gatewayConfigProducer.produceFrom(gatewayEntity)

        then:
            res.isSuccess()
            def model = res.get()
            verifyAll(model) {
                template == CONFIG_BASE
                caCertificate == "some-ca-cert"
                tlsAuthKey == "some-ta-key"
                certificate == "some-certificate"
                privateKey == "some-private-key"
                externalAddress == "localhost"
                externalPort == "443"
            }
    }

    def "produce GatewayConfigModel from GatewayEntity, fail to obtain config template"() {
        given:
            templateProvider.obtainTemplate("static/base.conf") >> Try.failure(new NoSuchElementException())

        when:
            def res = gatewayConfigProducer.produceFrom(gatewayEntity)

        then:
            res.isFailure()
            res.getCause() instanceof NoSuchElementException
    }

    def "produce GatewayConfigModel from GatewayEntity, fail to obtain ta key"() {
        given:
            templateProvider.obtainTemplate("static/base.conf") >> Try.success(CONFIG_BASE)
            certificateProvider.obtainTAKey() >> Try.failure(new IOException())

        when:
            def res = gatewayConfigProducer.produceFrom(gatewayEntity)

        then:
            res.isFailure()
            res.getCause() instanceof IOException
    }

    def "produce GatewayConfigModel from GatewayEntity, fail to obtain ca cert"() {
        given:
            templateProvider.obtainTemplate("static/base.conf") >> Try.success(CONFIG_BASE)
            certificateProvider.obtainCACert() >> Try.failure(new IOException())

        when:
            def res = gatewayConfigProducer.produceFrom(gatewayEntity)

        then:
            res.isFailure()
            res.getCause() instanceof IOException
    }

    private static final String CONFIG_BASE = "# specify it is client config,\n" +
            "# use layer 3 connectivity and tcp protocol\n" +
            "client\n" +
            "dev tun\n" +
            "proto tcp\n" +
            "\n" +
            "# good for unstable connection\n" +
            "resolv-retry infinite\n" +
            "\n" +
            "# client won't be bind to specific port\n" +
            "nobind\n" +
            "\n" +
            "# downgrade privileges after init\n" +
            "# then try to preserve state between restarts\n" +
            "user nobody\n" +
            "group nogroup\n" +
            "persist-key\n" +
            "persist-tun\n" +
            "\n" +
            "# verify server certificates by checking key usage\n" +
            "remote-cert-tls server\n" +
            "\n" +
            "# shared secret params\n" +
            "cipher AES-256-GCM\n" +
            "auth SHA256\n" +
            "key-direction 1\n" +
            "\n" +
            "# logs will persist between restarts,\n" +
            "# status logs will be produced every 60 seconds,\n" +
            "# moderate verbosity\n" +
            "; status /var/log/openvpn/openvpn-status.log\n" +
            "; log-append  /var/log/openvpn/openvpn.log\n" +
            "verb 3\n" +
            "\n" +
            "# here remote server's ip and port\n" +
            "# will be dynamically appended as well as certs and keys\n"
}
