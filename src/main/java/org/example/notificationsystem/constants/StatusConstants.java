package org.example.notificationsystem.constants;

import lombok.Getter;

/**
 * An enum to represent all possible states of an SmsRequest that has been initiated.
 * */
@Getter
public enum StatusConstants {
    /**
     * Sms Request is in progress.
     * */
    IN_PROGRESS(0),
    /**
     * Sms Request has been completed.
     * */
    FINISHED(1),
    /**
     * Sms Request has failed.
     * */
    FAILED(2);

    private final int code;

    StatusConstants(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}