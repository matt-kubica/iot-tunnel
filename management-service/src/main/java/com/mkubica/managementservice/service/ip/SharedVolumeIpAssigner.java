/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.ip;

import com.mkubica.managementservice.repository.GatewayRepository;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AllArgsConstructor;

import io.vavr.control.Try;


@AllArgsConstructor
public class SharedVolumeIpAssigner implements IpAssigner {

    private final String ccdPath;
    private final GatewayRepository gatewayRepository;

    public Try<String> assignIp(String commonName, String ipAddress) {
        File file = new File(ccdPath, commonName);
        return Try.of(() -> {
            // TODO: Probably use another exception
            if (!file.createNewFile()) throw new RuntimeException("Common name already allocated!");
            FileWriter fw = new FileWriter(file);
            fw.write("ifconfig-push " + ipAddress + "\n");
            fw.close();
            return ipAddress;
        });
    }

    public Try<String> assignRandomIp(String commonName) {
        return Try.of(() -> {
            String randomIp = chooseIp();
            assignIp(commonName, randomIp);
            return randomIp;
        });
    }

    public Try<Void> revokeIp(String commonName) {
        return Try.of(() -> {
            new File(ccdPath, commonName).delete();
            return null;
        });
    }

    // TODO: this is very simplified algorithm, just for now...
    private String chooseIp() {
        String ipAddressPrefix = "10.8.0.";
        while (true) {
            int seed = ThreadLocalRandom.current().nextInt(6, 254 + 1);
            int random = (seed % 2 == 0) ? seed : seed - 1;
            String ipAddressSuffix = String.valueOf(random);
            if (gatewayRepository.getGatewayEntityByIpAddress(ipAddressPrefix + ipAddressSuffix).isEmpty()) return (
                ipAddressPrefix + ipAddressSuffix
            );
        }
    }
}
