package org.example.notificationsystem.constants;

import lombok.Getter;

@Getter
public enum ErrorCodeConstants {
    INVALID_REQUEST(0);

    private final int code;

    ErrorCodeConstants(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}