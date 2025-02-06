package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.notificationsystem.models.SmsRequest;

/**
 * Object to represent the response body of a request requesting for a specific SmsRequest.
 * */
@Data
@Builder
public class GetSmsRequestResponse {
    /**
     * The specific Sms Request.
     * */
    SmsRequest data;
}