package org.example.notificationsystem.constants;

import lombok.Getter;

@Getter
public enum StatusConstants {
    IN_PROGRESS(0),
    FINISHED(1),
    FAILED(2);

    private final int code;

    StatusConstants(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}