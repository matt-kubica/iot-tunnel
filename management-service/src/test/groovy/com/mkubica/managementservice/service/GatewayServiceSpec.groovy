package com.mkubica.managementservice.service

import com.mkubica.managementservice.domain.dao.GatewayEntity
import com.mkubica.managementservice.domain.dto.CertificateBundleModel
import com.mkubica.managementservice.domain.dto.GatewayConfigModel
import com.mkubica.managementservice.domain.dto.GatewayModel
import com.mkubica.managementservice.exception.CommonNameBlankException
import com.mkubica.managementservice.exception.CommonNameNotUniqueException
import com.mkubica.managementservice.exception.IpAddressNotUniqueException
import com.mkubica.managementservice.exception.IpAddressStringInvalidException
import com.mkubica.managementservice.exception.IpAllocationException
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
                        .withIpAddress("10.8.0.4")
                        .withPrivateKey("some-private-key")
                        .withCertificate("some-certificate")
                        .build())

        when: "getGateway method is called"
            def res = gatewayService.getGateway(withName("some-common-name"))

        then: "result is valid, encapsulated object corresponds to stubbed entity"
            res.isSuccess()
            def model = res.get()
            verifyAll(model) {
                commonName == "some-common-name"
                ipAddress == "10.8.0.4"
                privateKey == "some-private-key"
                certificate == "some-certificate"
            }
    }

    def "try to get gateway that does not exist"() {
        given: "GatewayRepository stub that doesn't return entity"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name") >> Option.none()

        when: "getGateway method is called"
            def res = gatewayService.getGateway(withName("some-common-name"))

        then: "result is invalid, because of NoSuchElementException"
            res.isFailure()
            res.getCause() instanceof NoSuchElementException
    }

    def "create gateway without problems"() {
        given: "GatewayRepository stub that doesn't contain entity with requested cn and ip"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.none()
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()
        and: "IpAssigner stub that successfully allocates ip"
            ipAssigner.assignIp(_ as String, ip) >> Try.success("10.8.0.2")
        and: "ClientCertificateRequester stub that returns valid bundle"
            clientCertificateRequester.requestBundle(_ as String)
                    >> Try.success(new CertificateBundleModel("crt", "pk"))
        and: "GatewayRepository stub that returns certain GatewayEntity on save"
            gatewayRepository.save(_ as GatewayEntity)
                    >> new GatewayEntity("cn", "crt", "pk", "10.8.0.2")

        when: "createGateway method is called"
            def res = gatewayService.createGateway(withNameAndIp("some-common-name", ip))

        then: "result is valid and encapsulated model corresponds to saved entity"
            res.isSuccess()
            def model = res.get()
            verifyAll(model) {
                commonName == "cn"
                ipAddress == "10.8.0.2"
                privateKey == "pk"
                certificate == "crt"
            }

        where: ip << ["10.8.0.2", null]
    }

    def "try to create gateway with blank or null common name"() {
        given: "GatewayRepository stub that doesn't contain entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName(cn) >> Option.none()

        when: "createGateway method is called providing blank or null common name"
            def res = gatewayService.createGateway(withNameAndIp(cn, "10.8.0.2"))

        then: "result is invalid because of CommonNameBlankException"
            res.isFailure()
            res.getCause() instanceof CommonNameBlankException

        where: cn << [null, "", " "]
    }

    def "try to create gateway with invalid ip"() {
        given: "GatewayRepository stub that doesn't contain any entity"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.none()

        when: "createGateway method is called providing invalid ip address"
            def res = gatewayService.createGateway(withNameAndIp(_ as String, ip))

        then: "result is invalid because of IpAddressStringInvalidException"
            res.isFailure()
            res.getCause() instanceof IpAddressStringInvalidException

        where: ip << ["1.1.1.300", "...", "blah", "1.*.*.2", "10.8.0.2/12", "1.1.1.*"]
    }

    def "try to create gateway with already allocated common name"() {
        given: "GatewayRepository stub that contains entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName(_ as String)
                    >> Option.of(new GatewayEntity(_ as String, _ as String, _ as String, _ as String))

        when: "createGateway method is called"
            def res = gatewayService.createGateway(GatewayModel.builder().build())

        then: "result is invalid because of CommonNameNotUniqueException"
            res.isFailure()
            res.getCause() instanceof CommonNameNotUniqueException
    }

    def "try to create gateway with already allocated ip address"() {
        given: "IpAssigner that returns IpAddressNotUniqueException"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.none()
            ipAssigner.assignIp(_ as String, "10.8.0.2") >> Try.failure(new IpAddressNotUniqueException())

        when: "createGateway method is called"
            def res = gatewayService.createGateway(withNameAndIp(_ as String, "10.8.0.2"))

        then: "result is invalid because of IpAddressNotUniqueException"
            res.isFailure()
            res.getCause() instanceof IpAddressNotUniqueException
    }

    def "try to create gateway but ip allocation fails"() {
        given: "GatewayRepository stub that doesn't contain entity with requested common name"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.none()
        and: "IpAssigner stub that fails to assign ip"
            ipAssigner.assignIp(_ as String, "10.8.0.2") >> Try.failure(new IpAllocationException())

        when: "createGateway method is called"
            def res = gatewayService.createGateway(withNameAndIp(_ as String, "10.8.0.2"))

        then: "result is invalid because of IpAllocationException"
            res.isFailure()
            res.getCause() instanceof IpAllocationException
    }

    // TODO: IO exception might be not relevan in future versions of certificate requester
    def "try to create gateway but certificate request fails"() {
        given: "GatewayRepository stub that doesn't contain entity with requested common name"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.none()
        and: "IpAssigner stub that successfully allocates ip"
            ipAssigner.assignIp(_ as String, _ as String) >> Try.success(_ as String)
        and: "ClientCertificateRequester stub that fails to generate cert"
            clientCertificateRequester.requestBundle(_ as String) >> Try.failure(new IOException())

        when: "createGateway method is called"
            def res = gatewayService.createGateway(withNameAndIp(_ as String, "10.8.0.4"))

        then: "result is invalid because of IOException"
            res.isFailure()
            res.getCause() instanceof IOException
    }

    def "get gateway config"() {
        given: "certain entity"
            def entity = GatewayEntity.builder()
                .withCommonName("some-common-name")
                .withIpAddress("10.8.0.4")
                .withPrivateKey("some-private-key")
                .withCertificate("some-certificate")
                .build()
        and: "GatewayRepository stub that returns certain entity"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name") >> Option.of(entity)
        and: "GatewayConfigProducer stub that returns config"
            gatewayConfigProducer.produceFrom(entity) >> Try.success(GatewayConfigModel.builder().build())

        when: "getGatewayConfig method is called"
            def res = gatewayService.getGatewayConfig(withName("some-common-name"))

        then: "result is valid"
            res.isSuccess()
    }

    def "try to get gateway config, but entity with provided cn doesnt exist"() {
        given: "GatewayRepository stub that doesn't return certain entity"
            gatewayRepository.getGatewayEntityByCommonName(_ as String) >> Option.none()

        when: "getGatewayConfig method is called"
            def res = gatewayService.getGatewayConfig(withName(_ as String))

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
            def res = gatewayService.getGatewayConfig(withName("some-common-name"))

        then: "result is invalid because of IOException"
            res.isFailure()
            res.getCause() instanceof IOException
    }

    def "delete gateway"() {
        given: "GatewayRepository stub that contains entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name")
                    >> Option.of(GatewayEntity.builder()
                        .withCommonName("some-common-name")
                        .withIpAddress("10.8.0.4")
                        .build()
                    )
        and: "IpAssigner that successfully revokes ip"
            ipAssigner.revokeIp("some-common-name") >> Try.success("10.8.0.4")
        and: "ClientCertificateRequester that successfully revokes bundle"
            clientCertificateRequester.revokeBundle("some-common-name") >> Try.success(null)

        when: "deleteGateway method is called"
            def res = gatewayService.deleteGateway(withName("some-common-name"))

        then: "result is valid"
            res.isSuccess()
            res.get() == withNameAndIp("some-common-name", "10.8.0.4")
    }

    def "try to delete gateway but gateway with provided common name doesn't exist"() {
        given: "GatewayRepository stub that contains entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name") >> Option.none()

        when: "deleteGateway method is called"
            def res = gatewayService.deleteGateway(withName("some-common-name"))

        then: "result is invalid"
            res.isFailure()
            res.getCause() instanceof NoSuchElementException
    }

    def "try to delete gateway but IpAssigner fails to revoke ip"() {
        given: "GatewayRepository stub that contains entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name")
                    >> Option.of(GatewayEntity.builder()
                        .withCommonName("some-common-name")
                        .withIpAddress("10.8.0.4")
                        .build()
                )
        and: "IpAssigner that fails to revoke ip"
            ipAssigner.revokeIp("some-common-name") >> Try.failure(new FileNotFoundException())

        when: "deleteGateway method is called"
            def res = gatewayService.deleteGateway(withName("some-common-name"))

        then: "result is invalid"
            res.isFailure()
            res.getCause() instanceof FileNotFoundException
    }

    def "try to delete gateway but ClientCertificateRequester fails to revoke bundle"() {
        given: "GatewayRepository stub that contains entity with requested cn"
            gatewayRepository.getGatewayEntityByCommonName("some-common-name")
                    >> Option.of(GatewayEntity.builder()
                        .withCommonName("some-common-name")
                        .withIpAddress("10.8.0.4")
                        .build()
                )
        and: "IpAssigner revokes ip"
            ipAssigner.revokeIp("some-common-name") >> Try.success("10.8.0.4")
        and: "ClientCertificateRequester fails to revoke bundle"
            // NOTE: now it's just runtime exception since there is no implementation
            clientCertificateRequester.revokeBundle("some-common-name") >> Try.failure(new RuntimeException())

        when: "deleteGateway method is called"
            def res = gatewayService.deleteGateway(withName("some-common-name"))

        then: "result is invalid"
            res.isFailure()
            res.getCause() instanceof RuntimeException
    }

    static GatewayModel withName(String commonName) {
        return GatewayModel.builder().withCommonName(commonName).build()
    }

    static GatewayModel withNameAndIp(String commonName, String ip) {
        return GatewayModel.builder().withCommonName(commonName).withIpAddress(ip).build()
    }
}
