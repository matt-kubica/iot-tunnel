package com.mkubica.managementservice

import com.mkubica.managementservice.repository.GatewayRepository
import com.mkubica.managementservice.service.ip.IpAssigner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest()
@ActiveProfiles("local")
@AutoConfigureMockMvc
class ManagementServiceApplicationSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private GatewayRepository gatewayRepository

    @Autowired
    private IpAssigner ipAssigner

    // NOTE: before every case, repository is cleaned as well as ip assigner revokes all assigned addresses,
    // that's why there are autowired dependencies to GatewayRepository and IpAssigner
    def setup() {
        gatewayRepository.findAll().each { ipAssigner.revokeIp(it.commonName) }
        gatewayRepository.deleteAll()

    }

    def "context loads"() {
        expect: true
    }

    def "get gateway when there are no gateways"() {
        when: "performing get request on empty state"
            def result = mockMvc.perform(get("/gateway"))

        then: "gateway controller returns empty list"
            result.andExpect(status().is2xxSuccessful())
            result.andExpect(content().contentType("application/json"))
            result.andExpect(content().json("[]"))
    }

    def "create new gateway with explicit ip address"() {
        given:
            def commonName = "some-common-name"
            def explicitIp = "10.8.0.10"

        when: "performing post request to create new gateway"
            def result = mockMvc.perform(
                    post("/gateway")
                            .contentType("application/json")
                            .content("{\"commonName\": \"$commonName\", \"ipAddress\": \"$explicitIp\"}")
            )

        then: "gateway controller returns newly created entity"
            result.andExpect(status().is2xxSuccessful())
            result.andExpect(content().contentType("application/json"))
            result.andExpect(content().json("{\n" +
                    "    \"commonName\": \"$commonName\",\n" +
                    "    \"ipAddress\": \"$explicitIp\",\n" +
                    "    \"certificate\": \"some-certificate\",\n" +
                    "    \"privateKey\": \"some-private-key\"\n" +
                    "}"))
    }

    def "create new gateway with no explicit ip address"() {
        given:
            def commonName = "some-common-name"

        when: "performing post request to create new gateway"
            def result = mockMvc.perform(
                    post("/gateway")
                            .contentType("application/json")
                            .content("{\"commonName\": \"$commonName\"}")
            )

        then: "gateway controller returns newly created entity with auto assigned ip address"
            result.andExpect(status().is2xxSuccessful())
            result.andExpect(content().contentType("application/json"))
            result.andExpect(content().json("{\n" +
                    "    \"commonName\": \"$commonName\",\n" +
                    "    \"ipAddress\": \"10.8.0.2\",\n" +
                    "    \"certificate\": \"some-certificate\",\n" +
                    "    \"privateKey\": \"some-private-key\"\n" +
                    "}"))
    }

    def "create and get gateway"() {
        given:
            def commonName = "some-common-name"

        when: "performing post request to create new gateway"
            def firstResult = mockMvc.perform(
                    post("/gateway")
                            .contentType("application/json")
                            .content("{\"commonName\": \"$commonName\"}")
            )

        and: "performing get request to obtain new gateway"
            def secondResult = mockMvc.perform(get("/gateway/$commonName"))

        then: "gateway controller returns new entity after post request"
            firstResult.andExpect(status().is2xxSuccessful())
            firstResult.andExpect(content().contentType("application/json"))
            firstResult.andExpect(content().json("{\n" +
                    "    \"commonName\": \"$commonName\",\n" +
                    "    \"ipAddress\": \"10.8.0.2\",\n" +
                    "    \"certificate\": \"some-certificate\",\n" +
                    "    \"privateKey\": \"some-private-key\"\n" +
                    "}"))
        and: "returns same entity after executing get request"
            secondResult.andExpect(status().is2xxSuccessful())
            secondResult.andExpect(content().contentType("application/json"))
            secondResult.andExpect(content().json("{\n" +
                    "    \"commonName\": \"$commonName\",\n" +
                    "    \"ipAddress\": \"10.8.0.2\",\n" +
                    "    \"certificate\": \"some-certificate\",\n" +
                    "    \"privateKey\": \"some-private-key\"\n" +
                    "}"))

    }

    def "create gateway and get gateway config"() {
        given:
            def commonName = "some-common-name"

        when: "performing post request to create new gateway"
            def firstResult = mockMvc.perform(
                    post("/gateway")
                            .contentType("application/json")
                            .content("{\"commonName\": \"$commonName\", \"ipAddress\": \"10.8.0.4\"}")
            )
        and: "performing get request to obtain config"
            def secondResult = mockMvc.perform(get("/gateway-config/$commonName"))

        then: "gateway controller returns new entity after post request"
            firstResult.andExpect(status().is2xxSuccessful())
            firstResult.andExpect(content().contentType("application/json"))
            firstResult.andExpect(content().json("{\n" +
                    "    \"commonName\": \"$commonName\",\n" +
                    "    \"ipAddress\": \"10.8.0.4\",\n" +
                    "    \"certificate\": \"some-certificate\",\n" +
                    "    \"privateKey\": \"some-private-key\"\n" +
                    "}"))
        and: "returns config after get request"
            secondResult.andExpect(status().is2xxSuccessful())
            secondResult.andExpect(content().contentType("application/file"))
            secondResult.andExpect(content().string(exemplaryConfig))

    }

    // TODO: add counter cases after errors will be mapped to proper http codes

    // NOTE: config that is being produced for certain gateway is pretty generic,
    // there is no common name or ip address explicitly embedded into config,
    // part of a config that actually differs for certain gateways is certificate and private key
    //
    // "local" version of management-service that is being tested doesn't talk to CA,
    // thus all certificates and keys are stubbed - because of that, belows config can be reused
    // for all cases in this test suite
    static final exemplaryConfig = "# specify it is client config,\n" +
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
            "# will be dynamically appended as well as certs and keys\n" +
            "remote 1.1.1.1 443\n" +
            "<ca>\n" +
            "some-ca-cert</ca>\n" +
            "<cert>\n" +
            "some-certificate</cert>\n" +
            "<key>\n" +
            "some-private-key</key>\n" +
            "<tls-crypt>\n" +
            "some-ta-key</tls-crypt>\n"
}
