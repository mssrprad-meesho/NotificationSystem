package org.example.notificationsystem.constants;

import lombok.Getter;

/**
 * An enum to represent all the error codes that is sent to the user of the API.
 * */
@Getter
public enum ErrorCodeConstants {
    /**
     * Used to indicate that the request is invalid.
     * */
    INVALID_REQUEST(0);

    private final int code;

    ErrorCodeConstants(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}