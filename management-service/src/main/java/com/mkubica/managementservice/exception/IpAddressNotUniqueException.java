/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.exception;

public class IpAddressNotUniqueException extends IpAllocationException {

    public IpAddressNotUniqueException() {
    }

    public IpAddressNotUniqueException(String message) {
        super(message);
    }
}
