package com.mkubica.managementservice.util;

import io.vavr.control.Try;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpUtil {

    HttpClient client = HttpClientBuilder.create().build();

    public Try<String> post(String endpoint, String body) {
        return Try.of(() -> new StringEntity(body, ContentType.APPLICATION_JSON))
                .map(entity -> preparePostRequest(endpoint, entity))
                .mapTry(request -> client.execute(request, new BasicResponseHandler()));
    }

    private static HttpPost preparePostRequest(String endpoint, HttpEntity entity) {
        var post = new HttpPost(endpoint);
        post.setEntity(entity);
        return post;
    }
}
