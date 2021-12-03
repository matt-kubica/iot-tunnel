package com.mkubica.managementservice.service.ip;

import io.vavr.control.Try;

public interface IpAssigner {

    Try<Void> assignIp(String commonName, String ipAddress);
    Try<String> assignRandomIp(String commonName);
    Try<Void> revokeIp(String commonName);

}
