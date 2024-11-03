package org.example.exception;

import lombok.Getter;


@Getter
public class IllegalRequestException extends RuntimeException {
    private final String location;

    public IllegalRequestException(String location, String msg) {
        super(msg);
        this.location = location;
    }

}
