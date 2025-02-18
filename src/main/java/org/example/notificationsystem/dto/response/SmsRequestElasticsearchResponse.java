package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Object to represent the response body of requests that return a list of ElasticSearch Sms Request objects.
 */
@Builder
@Data
public class SmsRequestElasticsearchResponse {
    /**
     * The SmsRequests (with a readable String for dates) objects.
     */
    List<ElasticSearchResponse> data;
}
