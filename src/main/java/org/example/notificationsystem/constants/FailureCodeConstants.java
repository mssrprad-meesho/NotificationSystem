package org.example.notificationsystem.constants;

import lombok.Getter;

@Getter
public enum FailureCodeConstants {
    INVALID_PHONE_NUMBER(0),
    BLACKLISTED_PHONE_NUMBER(1),
    EXTERNAL_API_TIMEOUT(2),
    EXTERNAL_API_ERROR(3);

    private final int code;

    FailureCodeConstants(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
