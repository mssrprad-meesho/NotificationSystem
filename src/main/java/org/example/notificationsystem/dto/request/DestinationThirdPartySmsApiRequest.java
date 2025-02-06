package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Object to represent the destination field of Third Party API request.
 * */
@Builder
@Data
public class DestinationThirdPartySmsApiRequest{
    /**
     * The phone number(s) we are sending the notification to.
     * */
    private List<String> msisdn;
    /**
     * Unique correlation id. To be generated using UUID.
     * */
    private String correlationId;
}
