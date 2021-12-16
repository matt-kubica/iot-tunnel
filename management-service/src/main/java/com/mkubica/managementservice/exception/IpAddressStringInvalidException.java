package com.mkubica.managementservice.exception;

// TODO: should this extend IpAllocationException?
public class IpAddressStringInvalidException extends BaseException {

    public IpAddressStringInvalidException() {
        super("Provided ip address string is not valid");
    }

    public IpAddressStringInvalidException(String message) {
        super(message);
    }
}
