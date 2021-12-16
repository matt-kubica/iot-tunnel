package com.mkubica.managementservice.exception;

public class IpAddressPoolFullyAllocatedException extends IpAllocationException {

    public IpAddressPoolFullyAllocatedException() {
        super("Ip address pool is full, cannot assign more addresses");
    }

    public IpAddressPoolFullyAllocatedException(String message) {
        super(message);
    }
}
