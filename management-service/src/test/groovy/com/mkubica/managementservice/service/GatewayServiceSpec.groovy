package com.mkubica.managementservice.service

import com.mkubica.managementservice.domain.dao.GatewayEntity
import com.mkubica.managementservice.domain.dto.CertificateBundleModel
import com.mkubica.managementservice.domain.dto.GatewayConfigModel
import com.mkubica.managementservice.domain.dto.GatewaySimplifiedModel
import com.mkubica.managementservice.exception.CommonNameNotUniqueException
import com.mkubica.managementservice.exception.IpAddressNotUniqueException
import com.mkubica.managementservice.repository.GatewayRepository
import com.mkubica.managementservice.service.cert.ClientCertificateRequester
import com.mkubica.managementservice.service.ip.IpAssigner
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

class GatewayServiceSpec extends Specification {

    private GatewayConfigProducer gatewayConfigProducer = Stub()
    private GatewayRepository gatewayRepository = Stub()
    private ClientCertificateRequester clientCertificateRequester = Stub()
    private IpAssigner ipAssigner = Stub()
    private GatewayService gatewayService
            = new GatewayService(gatewayConfigProducer, gatewayRepository, clientCertificateRequester, ipAssigner)

    def "get gateway that exist"() {
        given: "GatewayRepository stub that returns certain entity"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name")
                    >> Option.of(GatewayEntity.builder()
                        .withCommonName("some-common-name")
                        .withIpAddress("10.8.0.5 10.8.0.6")
                        .withPrivateKey("some-private-key")
                        .withCertificate("some-certificate")
                        .build())

        when: "getGateway method is called"
            def res = gatewayService.getGateway("some-common-name")

        then: "result is valid, encapsulated object corresponds to stubbed entity"
            res.isSuccess()
            def model = res.get()
            verifyAll(model) {
                commonName == "some-common-name"
                ipAddress == "10.8.0.5 10.8.0.6"
                privateKey == "some-private-key"
                certificate == "some-certificate"
            }
    }

    def "try to get gateway that does not exist"() {
        given: "GatewayRepository stub that doesn't return entity"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name") >> Option.of(null)

        when: "getGateway method is called"
            def res = gatewayService.getGateway("some-common-name")

        then: "result is invalid, because of NoSuchElementException"
            res.isFailure()
            res.getCause() instanceof NoSuchElementException
    }

    def "create gateway without problems"() {
        given: "GatewayRepository stub that doesn't contain entity with requested cn and ip"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.of(null)
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.of(null)
        and: "IpAssigner stub that successfully allocates ip"
            ipAssigner.assignIp(_ as String, _ as String) >> Try.success("10.8.0.5 10.8.0.6")
        and: "ClientCertificateRequester stub that returns valid bundle"
            clientCertificateRequester.requestBundle(_ as String)
                    >> Try.success(new CertificateBundleModel("crt", "pk"))
        and: "GatewayRepository stub that returns certain GatewayEntity on save"
            gatewayRepository.save(_ as GatewayEntity)
                    >> new GatewayEntity("cn", "crt", "pk", "10.8.0.5 10.8.0.6")

        when: "createGateway method is called"
            def res = gatewayService.createGateway(
                    new GatewaySimplifiedModel("cn", "10.8.0.5 10.8.0.6"))

        then: "result is valid and encapsulated model corresponds to saved entity"
            res.isSuccess()
            def model = res.get()
            verifyAll(model) {
                commonName == "cn"
                ipAddress == "10.8.0.5 10.8.0.6"
                privateKey == "pk"
                certificate == "crt"
            }
    }

    def "try to create gateway with already allocated common name"() {
        given: "GatewayRepository stub that contains entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName(_ as String)
                    >> Option.of(new GatewayEntity(_ as String, _ as String, _ as String, _ as String))

        when: "createGateway method is called"
            def res = gatewayService.createGateway(new GatewaySimplifiedModel(_ as String, _ as String))

        then: "result is invalid because of CommonNameNotUniqueException"
            res.isFailure()
            res.getCause() instanceof CommonNameNotUniqueException

    }

    def "try to create gateway with already allocated ip address"() {
        given: "GatewayRepository stub that contains entity with requested ip"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.of(null)
            gatewayRepository.getGatewayEntityByIpAddress(_ as String)
                    >> Option.of(new GatewayEntity(_ as String, _ as String, _ as String, _ as String))

        when: "createGateway method is called"
            def res = gatewayService.createGateway(new GatewaySimplifiedModel(_ as String, _ as String))

        then: "result is invalid because of IpAddressNotUniqueException"
            res.isFailure()
            res.getCause() instanceof IpAddressNotUniqueException
    }

    // TODO: IOException probably won't be relevant with newer version of IpAssigner
    def "try to create gateway but ip allocation fails"() {
        given: "GatewayRepository stub that doesn't contain entity with requested ip or common name"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.of(null)
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.of(null)
        and: "IpAssigner stub that fails to assign ip"
            ipAssigner.assignIp(_ as String, _ as String) >> Try.failure(new IOException())


        when: "createGateway method is called"
            def res = gatewayService.createGateway(new GatewaySimplifiedModel(_ as String, _ as String))

        then: "result is invalid because of IOException"
            res.isFailure()
            res.getCause() instanceof IOException
    }

    def "try to create gateway but certificate request fails"() {
        given: "GatewayRepository stub that doesn't contain entity with requested ip or common name"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.of(null)
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.of(null)
        and: "IpAssigner stub that successfully allocates ip"
            ipAssigner.assignIp(_ as String, _ as String) >> Try.success(_ as String)
        and: "ClientCertificateRequester stub that fails to generate cert"
            clientCertificateRequester.requestBundle(_ as String) >> Try.failure(new IOException())

        when: "createGateway method is called"
            def res = gatewayService.createGateway(new GatewaySimplifiedModel(_ as String, _ as String))

        then: "result is invalid because of IOException"
            res.isFailure()
            res.getCause() instanceof IOException
    }

    def "get gateway config"() {
        given: "certain entity"
            def entity = GatewayEntity.builder()
                .withCommonName("some-common-name")
                .withIpAddress("10.8.0.5 10.8.0.6")
                .withPrivateKey("some-private-key")
                .withCertificate("some-certificate")
                .build()
        and: "GatewayRepository stub that returns certain entity"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name") >> Option.of(entity)
        and: "GatewayConfigProducer stub that returns config"
            gatewayConfigProducer.produceFrom(entity) >> Try.success(GatewayConfigModel.builder().build())

        when: "getGatewayConfig method is called"
            def res = gatewayService.getGatewayConfig("some-common-name")

        then: "result is valid"
            res.isSuccess()
    }

    def "try to get gateway config, but entity with provided cn doesnt exist"() {
        given: "GatewayRepository stub that doesn't return certain entity"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.of(null)

        when: "getGatewayConfig method is called"
            def res = gatewayService.getGatewayConfig(_ as String)

        then: "result is invalid because of NoSuchElementException"
            res.isFailure()
            res.getCause() instanceof NoSuchElementException
    }

    def "try to get gateway config, but GatewayConfigProducer fails to produce config"() {
        given: "GatewayRepository stub that returns certain entity"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name")
                    >> Option.of(GatewayEntity.builder().build())
        and: "GatewayConfigProducer stub fails to produce config"
            gatewayConfigProducer.produceFrom(_ as GatewayEntity) >> Try.failure(new IOException())

        when: "getGatewayConfig method is called"
            def res = gatewayService.getGatewayConfig("some-common-name")

        then: "result is invalid because of IOException"
            res.isFailure()
            res.getCause() instanceof IOException
    }
}
