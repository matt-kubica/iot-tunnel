/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.provider;

import java.io.*;
import java.util.stream.Collectors;

import io.vavr.control.Option;
import io.vavr.control.Try;

public class TemplateProvider {

    public Try<String> obtainTemplate(String path) {
        InputStream is = TemplateProvider.class.getClassLoader().getResourceAsStream(path);
        return Option.of(is)
                .map(stream -> new BufferedReader(new InputStreamReader(stream)))
                .map(reader -> reader.lines().collect(Collectors.joining(System.lineSeparator())))
                .toTry();
    }
}
