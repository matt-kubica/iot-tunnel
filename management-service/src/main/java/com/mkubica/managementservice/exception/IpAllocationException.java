package com.mkubica.managementservice.exception;

public class IpAllocationException extends BaseException {

    public IpAllocationException() {
    }

    public IpAllocationException(String message) {
        super(message);
    }

    public IpAllocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IpAllocationException(Throwable cause) {
        super(cause);
    }

    public IpAllocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
