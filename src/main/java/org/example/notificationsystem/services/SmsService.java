package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing SMS requests.
 */
public interface SmsService {

    /**
     * Creates a new SMS request with the provided number and message.
     *
     * @param number The phone number to send the SMS to.
     * @param message The content of the SMS.
     * @return The created {@link SmsRequest}.
     */
    SmsRequest createSmsRequest(String number, String message);

    /**
     * Retrieves an SMS request by its ID.
     *
     * @param Id The ID of the SMS request.
     * @return An Optional containing the {@link SmsRequest} if found.
     */
    Optional<SmsRequest> getSmsRequest(Long Id);

    /**
     * Retrieves all SMS requests with a finished status.
     *
     * @return A list of finished {@link SmsRequest} objects.
     */
    List<SmsRequest> getFinishedSmsRequests();

    /**
     * Retrieves all SMS requests with an in-progress status.
     *
     * @return A list of in-progress {@link SmsRequest} objects.
     */
    List<SmsRequest> getInProgressSmsRequests();

    /**
     * Retrieves all SMS requests that failed.
     *
     * @return A list of failed {@link SmsRequest} objects.
     */
    List<SmsRequest> getFailedSmsRequests();

    /**
     * Retrieves all SMS requests.
     *
     * @return A list of all {@link SmsRequest} objects.
     */
    List<SmsRequest> getAllSmsRequests();

    /**
     * Sets the status of an SMS request.
     *
     * @param smsRequestId The ID of the SMS request.
     * @param smsStatus    The status to set.
     * @return
     */
    Optional<SmsRequest> setStatus(Long smsRequestId, StatusConstants smsStatus);

    /**
     * Sets the failure code of an SMS request.
     *
     * @param smsRequestId   The ID of the SMS request.
     * @param smsFailureCode The failure code to set.
     * @return
     */
    Optional<SmsRequest> setFailureCode(Long smsRequestId, FailureCodeConstants smsFailureCode);

    /**
     * Retrieves SMS requests from Elasticsearch based on a date range, optional phone number, and search terms.
     *
     * @param from The start date for querying.
     * @param to The end date for querying.
     * @param number The optional phone number to query.
     * @param substr The list of substrings to search for in the message.
     * @return A list of {@link SmsRequestElasticsearch} objects.
     */
    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumber(Date from, Date to, Optional<String> number, List<String> substr);

    /**
     * Retrieves SMS requests from Elasticsearch with pagination based on a date range, optional phone number, and search terms.
     *
     * @param from The start date for querying.
     * @param to The end date for querying.
     * @param number The optional phone number to query.
     * @param substr The list of substrings to search for in the message.
     * @param page The page number.
     * @param size The size of each page.
     * @return A list of {@link SmsRequestElasticsearch} objects.
     */
    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumberPageSize(Date from, Date to, Optional<String> number, List<String> substr, int page, int size);
}