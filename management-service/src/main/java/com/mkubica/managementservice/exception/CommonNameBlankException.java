package com.mkubica.managementservice.exception;

public class CommonNameBlankException extends BaseException {

    public CommonNameBlankException() {
        super("Common name cannot be blank");
    }

    public CommonNameBlankException(String message) {
        super(message);
    }
}
