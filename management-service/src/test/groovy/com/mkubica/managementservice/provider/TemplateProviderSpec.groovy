package com.mkubica.managementservice.provider

import spock.lang.Specification

class TemplateProviderSpec extends Specification {

    private TemplateProvider templateProvider

    def setup() {
        templateProvider = new TemplateProvider()
    }

    def "obtain csr template"() {
        given:
            def path = "static/client-csr.json"

        when:
            def result = templateProvider.obtainTemplate(path)

        then:
            result.isSuccess()
            result.get() == "{\n" +
                    "    \"request\": {\n" +
                    "        \"CN\": \"blank\",\n" +
                    "        \"hosts\": [ ],\n" +
                    "        \"key\": {\n" +
                    "            \"algo\": \"rsa\",\n" +
                    "            \"size\": 2048\n" +
                    "        },\n" +
                    "        \"names\": [ ]\n" +
                    "    }\n" +
                    "}"
    }

    def "obtain non existent template"() {
        given:
            def path = "static/non-existent.template"

        when:
            def result = templateProvider.obtainTemplate(path)
            println result.getCause().getMessage()

        then:
            result.isFailure()
            result.getCause() instanceof NoSuchElementException
            result.cause.message == "No value present"
    }
}
