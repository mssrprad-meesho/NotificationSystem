package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * Object to represent the message text in Third Party API "channels.text" field.
 * */
@Builder
@Data
public class SmsThirdPartySmsApiRequest{
    /**
     * Sms text to be sent to the recipient(s)
     * */
    private String text;
}
