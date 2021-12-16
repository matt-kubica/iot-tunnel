package com.mkubica.managementservice.exception;

public class IpAddressNotWithinPoolException extends IpAllocationException {

    public IpAddressNotWithinPoolException(String message) {
        super(message);
    }
}
