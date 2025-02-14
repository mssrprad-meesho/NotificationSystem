package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Object to represent the response body of the response on initiating a successful Sms Request.
 */
@Data
@Builder
public class SmsRequestResponse {
    /**
     * The data field has the id of sms request in MySQL and a message.
     */
    private DataResponse data;
}