package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * Represents the request body of the Third Party API Request.
 * Has all the information required by the Third Party API.
 */
@Builder
@Data
public class ThirdPartySmsApiRequest {
    /**
     * Hardcoded to "sms" for our purpose.
     */
    private String deliveryChannel;
    /**
     * The Sms Text is contained in this field.
     */
    private ChannelsThirdPartySmsApiRequest channels;
    /**
     * Unique Correlation Id and the phone number (s) to which we are delivering the message.
     */
    private List<DestinationThirdPartySmsApiRequest> destination;
}

