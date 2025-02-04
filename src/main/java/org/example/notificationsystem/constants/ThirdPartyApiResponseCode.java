package org.example.notificationsystem.constants;

import lombok.Getter;

@Getter
public enum ThirdPartyApiResponseCode {
    SUCCESS(0),
    INVALID_REQUEST_BODY(1),
    TIMEOUT(2),
    API_ERROR(3),
    MALFORMED_URL(4);

    private final int code;

    ThirdPartyApiResponseCode(int i) {
        this.code = i;
    }

}