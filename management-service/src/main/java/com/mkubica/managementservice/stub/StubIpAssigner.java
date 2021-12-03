package com.mkubica.managementservice.stub;

import com.mkubica.managementservice.service.ip.IpAssigner;
import io.vavr.control.Try;

public class StubIpAssigner implements IpAssigner {

    @Override
    public Try<Void> assignIp(String commonName, String ipAddress) {
        return Try.success(null);
    }

    @Override
    public Try<String> assignRandomIp(String commonName) {
        return Try.success("10.8.0.121 10.8.0.122");
    }

    @Override
    public Try<Void> revokeIp(String commonName) {
        return Try.success(null);
    }
}