package org.example.notificationsystem.constants;

import lombok.Getter;

/**
 * An enum to represent all possible causes of failure of an initiated Sms Request.
 */
@Getter
public enum FailureCodeConstants {
    /**
     * No Failure
     */
    SUCCESS(0),
    /**
     * The Phone Number was blacklisted.
     */
    BLACKLISTED_PHONE_NUMBER(1),
    /**
     * The External API Timed out.
     */
    EXTERNAL_API_TIMEOUT(2),
    /**
     * An External API Error
     */
    EXTERNAL_API_ERROR(3),
    /**
     * A valid request body could not be constructed.
     */
    INVALID_REQUEST_BODY(4),
    /**
     * The endpoint being queried was an invalid URL.
     */
    INVALID_URL(5),
    /**
     * The Sms Request is still being handled.
     */
    IN_PROGRESS(6);

    private final int code;

    FailureCodeConstants(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
