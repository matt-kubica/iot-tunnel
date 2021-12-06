/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.ip;

import io.vavr.control.Try;

public interface IpAssigner {

    Try<String> assignIp(String commonName, String ipAddress);
    Try<String> assignRandomIp(String commonName);
    Try<Void> revokeIp(String commonName);

}
