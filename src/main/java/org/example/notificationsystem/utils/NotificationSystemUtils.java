package org.example.notificationsystem.utils;

import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;

import java.time.Instant;
import java.util.Date;

import static org.example.notificationsystem.constants.Time.INDIA_ZONE_ID;

/**
 * Utility class providing helper methods for the notification system.
 * It includes methods for validating query parameters, converting SMS requests to Elasticsearch format,
 * and getting the current time in IST.
 */
public class NotificationSystemUtils {

    /**
     * Validates the query parameters for an Elasticsearch request.
     * This method is a placeholder and can be extended in the future as needed.
     *
     * @param query The Elasticsearch request containing query parameters.
     */
    public static void validateQueryParameters(ElasticSearchRequest query) {
        // Validation logic can be added here
    }

    /**
     * Checks if the page request for Elasticsearch query is valid.
     * A valid page request requires both page and size to be non-null and positive.
     *
     * @param query The Elasticsearch request containing page and size parameters.
     * @return true if the page and size are valid, false otherwise.
     */
    public static boolean isValidPageRequest(ElasticSearchRequest query) {
        // Check if both page and size are non-null and valid
        return query.getPage() != null
                && query.getSize() != null
                && query.getPage() >= 0
                && query.getSize() > 0;
    }

    /**
     * Converts an SmsRequest object to an SmsRequestElasticsearch object.
     * This is typically used when indexing SMS requests into Elasticsearch.
     *
     * @param smsRequest The SmsRequest object to be converted.
     * @return A new SmsRequestElasticsearch object with the same data as the SmsRequest.
     */
    public static SmsRequestElasticsearch getSmsRequestElasticsearchFromSmsRequest(SmsRequest smsRequest) {
        SmsRequestElasticsearch smsRequestElasticsearch = new SmsRequestElasticsearch();
        smsRequestElasticsearch.setCreatedAt(smsRequest.getCreatedAt());
        smsRequestElasticsearch.setUpdatedAt(smsRequest.getUpdatedAt());
        smsRequestElasticsearch.setPhoneNumber(smsRequest.getPhoneNumber());
        smsRequestElasticsearch.setMessage(smsRequest.getMessage());
        smsRequestElasticsearch.setSmsRequestId(smsRequest.getId().toString());
        return smsRequestElasticsearch;
    }

    /**
     * Gets the current date and time in IST (Indian Standard Time).
     *
     * @return The current date and time in IST.
     */
    public static Date getNowAsDateIST() {
        return Date.from(Instant.now().atZone(INDIA_ZONE_ID).toInstant());
    }
}
