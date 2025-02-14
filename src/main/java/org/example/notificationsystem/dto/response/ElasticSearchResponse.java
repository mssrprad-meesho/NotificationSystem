package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Object to represent the ElasticSearch Sms Request object in responses.
 * It is different from the SmsRequestElasticSearch object to make the createdAt and updatedAt fields more readable.
 */
@Builder
@Getter
@Setter
public class ElasticSearchResponse {
    /**
     * Id of the SmsRequest in Elastic Search.
     */
    private String id;
    /***
     * Id of the SmsRequest in MySQL.
     * */
    private String smsRequestId;
    /**
     * Phone Number of the recipient.
     */
    private String phoneNumber;
    /**
     * Message sent.
     */
    private String message;
    /**
     * When the request was created, in readable format, in IST.
     * Ex: "Fri Jan 17 00:49:48 IST 2025"
     */
    private String createdAt;
    /**
     * When the request was updated, in readable format, in IST.
     * Ex: "Fri Jan 17 00:49:48 IST 2025"
     */
    private String updatedAt;
}