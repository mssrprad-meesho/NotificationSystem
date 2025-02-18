package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.response.SmsRequestElasticsearchResponse;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.springframework.http.ResponseEntity;

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
     * @param number  The phone number to send the SMS to.
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
     * @param smsRequestId    The ID of the SMS request.
     * @param smsFailureCode  The failure code to set.
     * @param failureComments The Failure Comments
     * @return {@link SmsRequest}
     */
    Optional<SmsRequest> setFailureCode(Long smsRequestId, FailureCodeConstants smsFailureCode, String failureComments);

    /**
     * Handles an Elasticsearch query request and returns the Sms Requests (in elastic search) that satisfy the criteria.
     * Handles various scenarios like:
     * a) Optional Pagination
     * b) Optional Date based filtering
     * c) Optional substring based filtering
     * d) Optional Phone Number based filtering
     * @param query
     * @return Response Entity containing {@link SmsRequestElasticsearchResponse}
     */
    ResponseEntity<SmsRequestElasticsearchResponse> getAllSmsRequestElasticsearchFromQuery(ElasticSearchRequest query);
}