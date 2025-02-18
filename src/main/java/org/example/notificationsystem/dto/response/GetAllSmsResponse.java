package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.notificationsystem.models.SmsRequest;

import java.util.List;

/**
 * Object to represent the response body's data field for requests returning a list of SmsRequests (mysql).
 */
@Data
@Builder
public class GetAllSmsResponse {
    /**
     * The Sms Requests being returned.
     */
    List<SmsRequest> data;
}
