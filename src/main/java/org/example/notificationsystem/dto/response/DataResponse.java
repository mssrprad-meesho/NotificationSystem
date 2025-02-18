package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * The data field of the SmsRequestResponse
 */
@Data
@Builder
public class DataResponse {
    /**
     * The requestId of the created SmsRequest
     */
    private String requestId;

    /**
     * Comments field.
     */
    private String comments;
}
