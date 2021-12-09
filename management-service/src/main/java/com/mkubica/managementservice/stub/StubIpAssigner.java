/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.stub;

import com.mkubica.managementservice.service.ip.IpAssigner;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StubIpAssigner implements IpAssigner {

    @Override
    public Try<String> assignIp(String commonName, String ipAddress) {
        log.debug("{} -> assignIp({}, {})", this.getClass().getSimpleName(), commonName, ipAddress);
        return Try.success(ipAddress);
    }

    @Override
    public Try<String> assignRandomIp(String commonName) {
        return Try.success("10.8.0.121 10.8.0.122");
    }

    @Override
    public Try<Void> revokeIp(String commonName) {
        log.debug("{} -> revokeIp({})", this.getClass().getSimpleName(), commonName);
        return Try.success(null);
    }
}
