/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.cert;

import com.mkubica.managementservice.domain.dto.CertificateBundleModel;
import com.mkubica.managementservice.provider.TemplateProvider;
import com.mkubica.managementservice.util.HttpUtil;

import lombok.AllArgsConstructor;

import io.vavr.control.Try;
import org.json.JSONObject;


@AllArgsConstructor
public class DefaultClientCertificateRequester implements ClientCertificateRequester {

    private final String newCertEndpoint;
    private final TemplateProvider templateProvider;
    private final HttpUtil httpUtil;

    public Try<CertificateBundleModel> requestBundle(String commonName) {

        return prepareBody(commonName)
                .map(body -> httpUtil.post(newCertEndpoint, body).get())
                .map(response -> CertificateBundleModel.builder()
                        .withCertificate(extractCertificate(response))
                        .withPrivateKey(extractPrivateKey(response))
                        .build()
                );
    }

    private Try<String> prepareBody(String commonName) {
        return templateProvider.obtainTemplate("static/client-csr.json")
                .map(JSONObject::new)
                .map(jo -> jo.getJSONObject("request").put("CN", commonName))
                .map(JSONObject::toString);
    }

    private static String extractCertificate(String response) {
        return (String) new JSONObject(response).getJSONObject("result").get("certificate");
    }

    private static String extractPrivateKey(String response) {
        return (String) new JSONObject(response).getJSONObject("result").get("private_key");
    }
}
