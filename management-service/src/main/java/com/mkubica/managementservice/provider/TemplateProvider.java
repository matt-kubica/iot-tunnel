/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.provider;

import java.io.*;
import java.util.stream.Collectors;

public class TemplateProvider {

    // TODO: change it to use vavr.Try
    public String obtainTemplate(String path) {
        ClassLoader classLoader = TemplateProvider.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream(path);
        if (is == null) throw new RuntimeException(String.format("Resource '%s' not found...", path));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
