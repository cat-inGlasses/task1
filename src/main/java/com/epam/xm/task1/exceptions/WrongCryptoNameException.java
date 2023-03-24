package com.epam.xm.task1.exceptions;

public class WrongCryptoNameException extends RuntimeException {

    public WrongCryptoNameException(String message) {
        super(message);
    }
}
