/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.cert;

import com.mkubica.managementservice.domain.dto.CertificateBundleModel;
import com.mkubica.managementservice.provider.TemplateProvider;

import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

@AllArgsConstructor
public class DefaultClientCertificateRequester implements ClientCertificateRequester {

    private final String newCertEndpoint;
    private final TemplateProvider templateProvider;

    public Try<CertificateBundleModel> requestCertificate(String commonName) {
        StringEntity entity = new StringEntity(prepareBody(commonName), ContentType.APPLICATION_JSON);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(newCertEndpoint);
        request.setEntity(entity);

        return Try.of(() -> client.execute(request, new BasicResponseHandler()))
                .map((response) -> CertificateBundleModel.builder()
                        .withCertificate(extractCertificate(response))
                        .withPrivateKey(extractPrivateKey(response))
                        .build()
                );
    }

    private String prepareBody(String commonName) {
        String template = templateProvider.obtainTemplate("static/client-csr.json");
        JSONObject jo = new JSONObject(template);
        jo.getJSONObject("request").put("CN", commonName);
        return jo.toString();
    }

    private static String extractCertificate(String response) {
        return (String) new JSONObject(response).getJSONObject("result").get("certificate");
    }

    private static String extractPrivateKey(String response) {
        return (String) new JSONObject(response).getJSONObject("result").get("private_key");
    }
}
