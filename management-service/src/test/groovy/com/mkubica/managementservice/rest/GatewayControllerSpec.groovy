package com.mkubica.managementservice.rest

import com.mkubica.managementservice.domain.dao.GatewayEntity
import com.mkubica.managementservice.repository.GatewayRepository
import com.mkubica.managementservice.service.GatewayService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class GatewayControllerSpec extends Specification {

    private GatewayService gatewayService = Stub()
    private GatewayRepository gatewayRepository = Stub()
    private MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new GatewayController(gatewayService, gatewayRepository))
            .build()


    def "get list of available gateways"() {
        given:
            gatewayRepository.findAll() >> List.of(
                    GatewayEntity.builder().withCommonName("some-common-name-1").withIpAddress("10.10.10.2").build(),
                    GatewayEntity.builder().withCommonName("some-common-name-2").withIpAddress("10.10.10.4").build()
            )
        and:
            def expectedResponse = "[\n" +
                    "    {\n" +
                    "        \"commonName\": \"some-common-name-1\",\n" +
                    "        \"ipAddress\": \"10.10.10.2\",\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"commonName\": \"some-common-name-2\",\n" +
                    "        \"ipAddress\": \"10.10.10.4\",\n" +
                    "    }\n" +
                    "]"

        when:
            def result = mockMvc.perform(get("/gateway"))

        then:
            result.andExpect(status().is2xxSuccessful())
            result.andExpect(content().contentType("application/json"))
            result.andExpect(content().json(expectedResponse))
    }

    // TODO: more cases of using controller(s)
}
