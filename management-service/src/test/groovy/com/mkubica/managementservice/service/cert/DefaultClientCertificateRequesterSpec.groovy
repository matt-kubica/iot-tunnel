package com.mkubica.managementservice.service.cert

import com.mkubica.managementservice.domain.dto.CertificateBundleModel
import com.mkubica.managementservice.provider.TemplateProvider
import com.mkubica.managementservice.util.HttpUtil
import io.vavr.control.Try
import org.json.JSONException
import spock.lang.Specification

class DefaultClientCertificateRequesterSpec extends Specification {

    private TemplateProvider templateProvider = Stub()
    private HttpUtil httpUtil = Stub()
    private DefaultClientCertificateRequester certificateRequester
            = new DefaultClientCertificateRequester(_ as String, templateProvider, httpUtil)

    def "requests certificate, TemplateProvider and HttpUtil succeed"() {
        given:
            templateProvider.obtainTemplate(_ as String) >> Try.success(CSR_TEMPLATE)
            httpUtil.post(_ as String, _ as String) >> Try.success(CERT_RESPONSE)


        when:
            def res = certificateRequester.requestBundle(_ as String)

        then:
            res.isSuccess()
            res.get() instanceof CertificateBundleModel
            res.get() == CERT_BUNDLE_RESPONSE
    }

    def "requests certificate, TemplateProvider succeeds, HttpUtil fails"() {
        given:
            templateProvider.obtainTemplate(_ as String) >> Try.success(CSR_TEMPLATE)
            httpUtil.post(_ as String, _ as String) >> Try.failure(new IOException())

        when:
            def res = certificateRequester.requestBundle(_ as String)

        then:
            res.isFailure()
            res.getCause() instanceof IOException
    }

    def "requests certificate, TemplateProvider fails"() {
        given:
            templateProvider.obtainTemplate(_ as String) >> Try.failure(new NoSuchElementException())

        when:
            def res = certificateRequester.requestBundle(_ as String)

        then:
            res.isFailure()
            res.getCause() instanceof NoSuchElementException
    }

    // NOTE: this is rather not likely to happen
    def "requests certificate, TemplateProvider returns corrupted template"() {
        given:
            templateProvider.obtainTemplate(_ as String) >> Try.success(CORRUPTED_CSR_TEMPLATE)

        when:
            def res = certificateRequester.requestBundle(_ as String)

        then:
            res.isFailure()
            res.getCause() instanceof JSONException
    }

    // NOTE: this is rather not likely to happen
    def "requests certificate, TemplateProvider succeeds, HttpUtil returns corrupted response"() {
        given:
            templateProvider.obtainTemplate(_ as String) >> Try.success(CSR_TEMPLATE)
            httpUtil.post(_ as String, _ as String) >> Try.success(CORRUPTED_CERT_RESPONSE)

        when:
            def res = certificateRequester.requestBundle(_ as String)

        then:
            res.isFailure()
            res.getCause() instanceof JSONException
    }

    private static final CertificateBundleModel CERT_BUNDLE_RESPONSE = CertificateBundleModel.builder()
        .withCertificate("-----BEGIN CERTIFICATE-----\nMIIDRzCCAjGgAwIBAgIIV2zafpyQtp4wCwYJKoZIhvcNAQELMIGMMQswCQYDVQQG\nEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNj\nbzETMBEGA1UEChMKQ0ZTU0wgVEVTVDEbMBkGA1UEAxMSQ0ZTU0wgVEVTVCBSb290\nIENBMR4wHAYJKoZIhvcNAQkBFg90ZXN0QHRlc3QubG9jYWwwHhcNMTUwODAzMDYx\nMjAwWhcNMTYwODAyMDYxMjAwWjBqMQswCQYDVQQGEwJVUzEUMBIGA1UEChMLZXhh\nbXBsZS5jb20xFjAUBgNVBAcTDVNhbiBGcmFuY2lzY28xEzARBgNVBAgTCkNhbGlm\nb3JuaWExGDAWBgNVBAMTD3d3dy5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqG\nSM49AwEHA0IABK/CtZaQ4VliKE+DLIVGLwtSxJgtUKRzGvN1EwI3HRgKDQ3l3urB\nIzHtUcdMq6HZb8jX0O9fXYUOf4XWggrLk1ajgZwwgZkwDgYDVR0PAQH/BAQDAgCg\nMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0G\nA1UdDgQWBBTF8UwoRdK0rWK8FWiyRxl3H2Wr+TAfBgNVHSMEGDAWgBS30veEuqg5\n1fusEM4p/YuWpBPsvTAaBgNVHREEEzARgg93d3cuZXhhbXBsZS5jb20wCwYJKoZI\nhvcNAQELA4IBAQCT+9xoBO39nFesT0dmdqpwHExU09/IYrkvYwWesX5U9z/f3HYP\nLz/NnXIs6a+k8MglvZgHwr5R8nzVtayfPTWyML6L6AOX8EfV5UXbnXW4XRUhHAik\n+E1gYhOCD1dLQJyQkX8VVr725BUk1yQD3Kf0PJUvagLJjn8Gn7QoGWfvVgpR8iMd\ncBJqlx8Z9KCYcLrpXliD8OJqT7Z8TGbnehpcaNwPPI6dMX57wgXSNuP5g8OkxMcL\nxZEP3q9JRjN3ZiM5xIeoTc/zl1WhZ+YpOHSbv/T9DX3f74ms9GEc0JnR8iENJTu6\nRx0/qPDPpqZ+Fr9v/13/OvQ+jAY5qe/6l1d6\n-----END CERTIFICATE-----\n")
        .withPrivateKey("-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIJfVVIvXclN1jCWefEwhYYq7y1ya2RjxO5o8QjehD3YdoAoGCCqGSM49\nAwEHoUQDQgAEr8K1lpDhWWIoT4MshUYvC1LEmC1QpHMa83UTAjcdGAoNDeXe6sEj\nMe1Rx0yrodlvyNfQ719dhQ5/hdaCCsuTVg==\n-----END EC PRIVATE KEY-----\n")
        .build()

    private static final String CSR_TEMPLATE = "{\n" +
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

    // NOTE: There is one quote mark missing so JSON is corrupted
    private static final String CORRUPTED_CSR_TEMPLATE = "{\n" +
            "    \"request\": {\n" +
            "        \"CN\": blank\",\n" +
            "        \"hosts\": [ ],\n" +
            "        \"key\": {\n" +
            "            \"algo\": \"rsa\",\n" +
            "            \"size\": 2048\n" +
            "        },\n" +
            "        \"names\": [ ]\n" +
            "    }\n" +
            "}"

    private static final String CERT_RESPONSE = "{\n" +
            "    \"errors\": [],\n" +
            "    \"messages\": [],\n" +
            "    \"result\": {\n" +
            "        \"certificate\": \"-----BEGIN CERTIFICATE-----\\nMIIDRzCCAjGgAwIBAgIIV2zafpyQtp4wCwYJKoZIhvcNAQELMIGMMQswCQYDVQQG\\nEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNj\\nbzETMBEGA1UEChMKQ0ZTU0wgVEVTVDEbMBkGA1UEAxMSQ0ZTU0wgVEVTVCBSb290\\nIENBMR4wHAYJKoZIhvcNAQkBFg90ZXN0QHRlc3QubG9jYWwwHhcNMTUwODAzMDYx\\nMjAwWhcNMTYwODAyMDYxMjAwWjBqMQswCQYDVQQGEwJVUzEUMBIGA1UEChMLZXhh\\nbXBsZS5jb20xFjAUBgNVBAcTDVNhbiBGcmFuY2lzY28xEzARBgNVBAgTCkNhbGlm\\nb3JuaWExGDAWBgNVBAMTD3d3dy5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqG\\nSM49AwEHA0IABK/CtZaQ4VliKE+DLIVGLwtSxJgtUKRzGvN1EwI3HRgKDQ3l3urB\\nIzHtUcdMq6HZb8jX0O9fXYUOf4XWggrLk1ajgZwwgZkwDgYDVR0PAQH/BAQDAgCg\\nMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0G\\nA1UdDgQWBBTF8UwoRdK0rWK8FWiyRxl3H2Wr+TAfBgNVHSMEGDAWgBS30veEuqg5\\n1fusEM4p/YuWpBPsvTAaBgNVHREEEzARgg93d3cuZXhhbXBsZS5jb20wCwYJKoZI\\nhvcNAQELA4IBAQCT+9xoBO39nFesT0dmdqpwHExU09/IYrkvYwWesX5U9z/f3HYP\\nLz/NnXIs6a+k8MglvZgHwr5R8nzVtayfPTWyML6L6AOX8EfV5UXbnXW4XRUhHAik\\n+E1gYhOCD1dLQJyQkX8VVr725BUk1yQD3Kf0PJUvagLJjn8Gn7QoGWfvVgpR8iMd\\ncBJqlx8Z9KCYcLrpXliD8OJqT7Z8TGbnehpcaNwPPI6dMX57wgXSNuP5g8OkxMcL\\nxZEP3q9JRjN3ZiM5xIeoTc/zl1WhZ+YpOHSbv/T9DX3f74ms9GEc0JnR8iENJTu6\\nRx0/qPDPpqZ+Fr9v/13/OvQ+jAY5qe/6l1d6\\n-----END CERTIFICATE-----\\n\",\n" +
            "        \"certificate_request\": \"-----BEGIN CERTIFICATE REQUEST-----\\nMIIBUjCB+QIBADBqMQswCQYDVQQGEwJVUzEUMBIGA1UEChMLZXhhbXBsZS5jb20x\\nFjAUBgNVBAcTDVNhbiBGcmFuY2lzY28xEzARBgNVBAgTCkNhbGlmb3JuaWExGDAW\\nBgNVBAMTD3d3dy5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\\nBK/CtZaQ4VliKE+DLIVGLwtSxJgtUKRzGvN1EwI3HRgKDQ3l3urBIzHtUcdMq6HZ\\nb8jX0O9fXYUOf4XWggrLk1agLTArBgkqhkiG9w0BCQ4xHjAcMBoGA1UdEQQTMBGC\\nD3d3dy5leGFtcGxlLmNvbTAKBggqhkjOPQQDAgNIADBFAiAcvfhXnsLtzep2sKSa\\n36W7G9PRbHh8zVGlw3Hph8jR1QIhAKfrgplKwXcUctU5grjQ8KXkJV8RxQUo5KKs\\ngFnXYtkb\\n-----END CERTIFICATE REQUEST-----\\n\",\n" +
            "        \"private_key\": \"-----BEGIN EC PRIVATE KEY-----\\nMHcCAQEEIJfVVIvXclN1jCWefEwhYYq7y1ya2RjxO5o8QjehD3YdoAoGCCqGSM49\\nAwEHoUQDQgAEr8K1lpDhWWIoT4MshUYvC1LEmC1QpHMa83UTAjcdGAoNDeXe6sEj\\nMe1Rx0yrodlvyNfQ719dhQ5/hdaCCsuTVg==\\n-----END EC PRIVATE KEY-----\\n\",\n" +
            "        \"sums\": {\n" +
            "            \"certificate\": {\n" +
            "                \"md5\": \"E9308D1892F1B77E6721EA2F79C026BE\",\n" +
            "                \"sha-1\": \"4640E6DEC2C40B74F46C409C1D31928EE0073D25\"\n" +
            "            },\n" +
            "            \"certificate_request\": {\n" +
            "                \"md5\": \"AA924136405006E36CEE39FED9CBA5D7\",\n" +
            "                \"sha-1\": \"DF955A43DF669D38E07BF0479789D13881DC9024\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"success\": true\n" +
            "}"

    // NOTE: There is one quote mark missing so JSON is corrupted
    private static final String CORRUPTED_CERT_RESPONSE = "{\n" +
            "    \"errors\": [],\n" +
            "    \"messages\": [],\n" +
            "    \"result: {\n" +
            "        \"certificate\": \"-----BEGIN CERTIFICATE-----\\nMIIDRzCCAjGgAwIBAgIIV2zafpyQtp4wCwYJKoZIhvcNAQELMIGMMQswCQYDVQQG\\nEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNj\\nbzETMBEGA1UEChMKQ0ZTU0wgVEVTVDEbMBkGA1UEAxMSQ0ZTU0wgVEVTVCBSb290\\nIENBMR4wHAYJKoZIhvcNAQkBFg90ZXN0QHRlc3QubG9jYWwwHhcNMTUwODAzMDYx\\nMjAwWhcNMTYwODAyMDYxMjAwWjBqMQswCQYDVQQGEwJVUzEUMBIGA1UEChMLZXhh\\nbXBsZS5jb20xFjAUBgNVBAcTDVNhbiBGcmFuY2lzY28xEzARBgNVBAgTCkNhbGlm\\nb3JuaWExGDAWBgNVBAMTD3d3dy5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqG\\nSM49AwEHA0IABK/CtZaQ4VliKE+DLIVGLwtSxJgtUKRzGvN1EwI3HRgKDQ3l3urB\\nIzHtUcdMq6HZb8jX0O9fXYUOf4XWggrLk1ajgZwwgZkwDgYDVR0PAQH/BAQDAgCg\\nMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0G\\nA1UdDgQWBBTF8UwoRdK0rWK8FWiyRxl3H2Wr+TAfBgNVHSMEGDAWgBS30veEuqg5\\n1fusEM4p/YuWpBPsvTAaBgNVHREEEzARgg93d3cuZXhhbXBsZS5jb20wCwYJKoZI\\nhvcNAQELA4IBAQCT+9xoBO39nFesT0dmdqpwHExU09/IYrkvYwWesX5U9z/f3HYP\\nLz/NnXIs6a+k8MglvZgHwr5R8nzVtayfPTWyML6L6AOX8EfV5UXbnXW4XRUhHAik\\n+E1gYhOCD1dLQJyQkX8VVr725BUk1yQD3Kf0PJUvagLJjn8Gn7QoGWfvVgpR8iMd\\ncBJqlx8Z9KCYcLrpXliD8OJqT7Z8TGbnehpcaNwPPI6dMX57wgXSNuP5g8OkxMcL\\nxZEP3q9JRjN3ZiM5xIeoTc/zl1WhZ+YpOHSbv/T9DX3f74ms9GEc0JnR8iENJTu6\\nRx0/qPDPpqZ+Fr9v/13/OvQ+jAY5qe/6l1d6\\n-----END CERTIFICATE-----\\n\",\n" +
            "        \"certificate_request\": \"-----BEGIN CERTIFICATE REQUEST-----\\nMIIBUjCB+QIBADBqMQswCQYDVQQGEwJVUzEUMBIGA1UEChMLZXhhbXBsZS5jb20x\\nFjAUBgNVBAcTDVNhbiBGcmFuY2lzY28xEzARBgNVBAgTCkNhbGlmb3JuaWExGDAW\\nBgNVBAMTD3d3dy5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\\nBK/CtZaQ4VliKE+DLIVGLwtSxJgtUKRzGvN1EwI3HRgKDQ3l3urBIzHtUcdMq6HZ\\nb8jX0O9fXYUOf4XWggrLk1agLTArBgkqhkiG9w0BCQ4xHjAcMBoGA1UdEQQTMBGC\\nD3d3dy5leGFtcGxlLmNvbTAKBggqhkjOPQQDAgNIADBFAiAcvfhXnsLtzep2sKSa\\n36W7G9PRbHh8zVGlw3Hph8jR1QIhAKfrgplKwXcUctU5grjQ8KXkJV8RxQUo5KKs\\ngFnXYtkb\\n-----END CERTIFICATE REQUEST-----\\n\",\n" +
            "        \"private_key\": \"-----BEGIN EC PRIVATE KEY-----\\nMHcCAQEEIJfVVIvXclN1jCWefEwhYYq7y1ya2RjxO5o8QjehD3YdoAoGCCqGSM49\\nAwEHoUQDQgAEr8K1lpDhWWIoT4MshUYvC1LEmC1QpHMa83UTAjcdGAoNDeXe6sEj\\nMe1Rx0yrodlvyNfQ719dhQ5/hdaCCsuTVg==\\n-----END EC PRIVATE KEY-----\\n\",\n" +
            "        \"sums\": {\n" +
            "            \"certificate\": {\n" +
            "                \"md5\": \"E9308D1892F1B77E6721EA2F79C026BE\",\n" +
            "                \"sha-1\": \"4640E6DEC2C40B74F46C409C1D31928EE0073D25\"\n" +
            "            },\n" +
            "            \"certificate_request\": {\n" +
            "                \"md5\": \"AA924136405006E36CEE39FED9CBA5D7\",\n" +
            "                \"sha-1\": \"DF955A43DF669D38E07BF0479789D13881DC9024\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"success\": true\n" +
            "}"
}
