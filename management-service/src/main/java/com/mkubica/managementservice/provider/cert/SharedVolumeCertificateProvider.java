/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.provider.cert;

import java.io.*;
import java.util.stream.Collectors;

import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SharedVolumeCertificateProvider implements CertificateProvider {

    private final String caCertPath;
    private final String taKeyPath;

    @Override
    public Try<String> obtainCACert() {
        return Try.of(() -> new BufferedReader(new FileReader(caCertPath)).lines())
                .map(lines -> lines.map(line -> line + "\n").collect(Collectors.joining()));
    }

    @Override
    public Try<String> obtainTAKey() {
        return Try.of(() -> new BufferedReader(new FileReader(taKeyPath)).lines())
                .map(lines -> lines.map(line -> line + "\n").collect(Collectors.joining()));
    }
}
