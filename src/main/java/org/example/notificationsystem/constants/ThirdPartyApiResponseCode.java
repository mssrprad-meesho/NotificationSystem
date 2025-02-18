package org.example.notificationsystem.constants;

import lombok.Getter;

/**
 * An enum to represent all possible results of querying the third party API.
 */
@Getter
public enum ThirdPartyApiResponseCode {
    /**
     * Received a 200 response Code signifying success
     */
    SUCCESS(0),
    /**
     * Request body could either not be serialized or written into the request body.
     */
    INVALID_REQUEST_BODY(1),
    /**
     * TCP Connection timeout.
     */
    TIMEOUT(2),
    /**
     * Non 200 response code.
     */
    API_ERROR(3),
    /**
     * The endpoint which is being requested is malformed.
     */
    MALFORMED_URL(4);

    private final int code;

    ThirdPartyApiResponseCode(int i) {
        this.code = i;
    }

}