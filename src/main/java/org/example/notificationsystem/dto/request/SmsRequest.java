package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Obect to represent the request body of an SmsRequest.
 * It is used to initiate an Sms Request.
 */
@Data
@Builder
public class SmsRequest {
    /**
     * Phone Number of the recipient.
     */
    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{10,14}$")
    private String phoneNumber;

    /**
     * Message to be send to the recipient.
     */
    @NotBlank
    @Size(max = 10000)
    private String message;
}