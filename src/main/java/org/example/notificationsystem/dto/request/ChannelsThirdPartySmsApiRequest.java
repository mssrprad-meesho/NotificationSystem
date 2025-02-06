package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * Object to represent the Channels field in Third Party API Request Body.
 * Has all text content of sms we are sending.
 * */
@Builder
@Data
public class ChannelsThirdPartySmsApiRequest{
    /**
     * The sms text.
     * */
    private SmsThirdPartySmsApiRequest sms;
}
