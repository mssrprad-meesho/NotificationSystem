package org.example.notificationsystem.services.impl;

import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.response.ElasticSearchResponse;
import org.example.notificationsystem.dto.response.SmsRequestElasticsearchResponse;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.ElasticSearchRepository;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.example.notificationsystem.services.SmsService;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.example.notificationsystem.constants.Time.MAX_DATE;
import static org.example.notificationsystem.utils.NotificationSystemUtils.isValidPageRequest;
import static org.example.notificationsystem.utils.NotificationSystemUtils.parseIstToUtcDate;

/**
 * Implementation of SmsService interface, handling SMS requests, statuses, and interactions with MySQL, Elasticsearch, and Kafka.
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final SmsRequestRepository smsRequestRepository;
    private final SmsRequestElasticsearchRepository smsRequestElasticsearchRepository;
    private final Producer producer;
    private final ElasticSearchRepository elasticSearchRepository;

    @Autowired
    public SmsServiceImpl(SmsRequestRepository smsRequestRepository, SmsRequestElasticsearchRepository smsRequestElasticsearchRepository, Producer producer, ElasticSearchRepository elasticSearchRepository) {
        this.smsRequestRepository = smsRequestRepository;
        this.smsRequestElasticsearchRepository = smsRequestElasticsearchRepository;
        this.producer = producer;
        this.elasticSearchRepository = elasticSearchRepository;
    }

    /**
     * Creates a new SMS request, saves it to MySQL, Elasticsearch, and sends a Kafka message.
     *
     * @param number  The phone number to send the SMS to.
     * @param message The content of the SMS message.
     * @return The saved {@link SmsRequest} object.
     */
    @Transactional
    public SmsRequest createSmsRequest(String number, String message) {
        logger.info("Creating SMS request for number: {}", number);

        // Save into MySQL - sms_request
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setPhoneNumber(number);
        smsRequest.setMessage(message);
        smsRequest.setStatus(StatusConstants.IN_PROGRESS.ordinal());
        smsRequest.setCreatedAt(NotificationSystemUtils.getNowAsDateIST());
        smsRequest.setUpdatedAt(NotificationSystemUtils.getNowAsDateIST());

        SmsRequest persistedSmsRequest = smsRequestRepository.saveAndFlush(smsRequest);
        logger.info("SMS request for number {} saved to MySQL with status {}", number, StatusConstants.IN_PROGRESS.name());

        // Save into Elasticsearch
        try {
            SmsRequestElasticsearch smsRequestElasticsearch = NotificationSystemUtils.getSmsRequestElasticsearchFromSmsRequest(persistedSmsRequest);
            smsRequestElasticsearchRepository.save(smsRequestElasticsearch);
            logger.info("SMS request for number {} saved to Elasticsearch", number);
        } catch (Exception e) {
            logger.error("Failed to save SMS request to Elasticsearch for number {}: {}", number, e.getMessage());
            throw e;
        }

        // Send Kafka Message
        boolean success = producer.publishSync(persistedSmsRequest.getId());
        if (success) {
            logger.info("Kafka message sent for SMS request ID {}", persistedSmsRequest.getId());
        } else {
            logger.error("Failed to send Kafka message for SMS request ID {}", persistedSmsRequest.getId());
        }
        return persistedSmsRequest;
    }

    /**
     * Fetches SMS requests from Elasticsearch based on given date range, phone number, and terms.
     *
     * @param from   The start date of the search range.
     * @param to     The end date of the search range.
     * @param number Optional phone number filter for the search.
     * @param terms  List of terms to search within the SMS message.
     * @return A list of {@link SmsRequestElasticsearch} objects matching the search criteria.
     */
    private List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumber(Date from, Date to, Optional<String> number, List<String> terms) {
        logger.info("Fetching SMS requests from Elasticsearch");
        return elasticSearchRepository.getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequest(from, to, number, terms);
    }

    /**
     * Fetches paginated SMS requests from Elasticsearch based on given date range, phone number, and terms.
     *
     * @param from   The start date of the search range.
     * @param to     The end date of the search range.
     * @param number Optional phone number filter for the search.
     * @param terms  List of terms to search within the SMS message.
     * @param page   The page number for pagination.
     * @param size   The size of the page.
     * @return A list of {@link SmsRequestElasticsearch} objects for the requested page.
     */
    private List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumberPageSize(Date from, Date to, Optional<String> number, List<String> terms, int page, int size) {
        logger.info("Fetching SMS requests from Elasticsearch");
        return elasticSearchRepository.getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequestPageSize(from, to, number, terms, page, size);
    }

    /**
     * Fetches an SMS request by its ID from MySQL.
     *
     * @param Id The ID of the SMS request to fetch.
     * @return An Optional containing the SmsRequest object if found, else an empty Optional.
     */
    public Optional<SmsRequest> getSmsRequest(Long Id) {
        logger.info("Fetching SMS request by ID: {}", Id);
        return smsRequestRepository.findById(Id);
    }

    /**
     * Fetches all SMS requests from MySQL.
     *
     * @return A list of all {@link SmsRequest} objects from MySQL.
     */
    public List<SmsRequest> getAllSmsRequests() {
        logger.info("Fetching all SMS requests from MySQL");
        List<SmsRequest> smsRequests = smsRequestRepository.findAll();
        logger.info("Fetched {} SMS requests from MySQL", smsRequests.size());
        return smsRequests;
    }

    /**
     * Fetches all finished SMS requests from MySQL.
     *
     * @return A list of all finished {@link SmsRequest} objects from MySQL.
     */
    public List<SmsRequest> getFinishedSmsRequests() {
        logger.info("Fetching finished SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.FINISHED.ordinal());
    }

    /**
     * Fetches all in-progress SMS requests from MySQL.
     *
     * @return List of {@link SmsRequest} A list of all in-progress SmsRequest objects from MySQL.
     */
    public List<SmsRequest> getInProgressSmsRequests() {
        logger.info("Fetching in-progress SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.IN_PROGRESS.ordinal());
    }

    /**
     * Fetches all failed SMS requests from MySQL.
     *
     * @return A list of all failed {@link SmsRequest} objects from MySQL.
     */
    public List<SmsRequest> getFailedSmsRequests() {
        logger.info("Fetching failed SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.FAILED.ordinal());
    }

    /**
     * Updates the status of an SMS request in MySQL.
     *
     * @param smsRequestId The ID of the SMS request to update.
     * @param smsStatus    The new status to set for the SMS request.
     * @return Optional {@link SmsRequest}
     */
    public Optional<SmsRequest> setStatus(Long smsRequestId, StatusConstants smsStatus) {
        logger.info("Updating status for SMS request ID: {} to status: {}", smsRequestId, smsStatus);
        Optional<SmsRequest> optSmsRequest = smsRequestRepository.findById(smsRequestId);
        if (optSmsRequest.isPresent()) {
            SmsRequest smsRequest = optSmsRequest.get();
            smsRequest.setStatus(smsStatus.ordinal());
            smsRequestRepository.saveAndFlush(smsRequest);
            logger.info("Successfully updated status for SMS request ID: {} to {}", smsRequestId, smsStatus);
        } else {
            logger.error("SMS request ID: {} not found, status update failed", smsRequestId);
        }
        return optSmsRequest;
    }

    /**
     * Updates the failure code of an SMS request in MySQL.
     *
     * @param smsRequestId   The ID of the SMS request to update.
     * @param smsFailureCode The failure code to set for the SMS request.
     * @param failureComments The failure comments.
     * @return Optional {@link SmsRequest}
     */
    @Transactional
    public Optional<SmsRequest> setFailureCode(Long smsRequestId, FailureCodeConstants smsFailureCode, String failureComments) {
        logger.info("Setting failure code for SMS request ID: {} to status: {} with failure comments: {}", smsRequestId, smsFailureCode, failureComments);
        Optional<SmsRequest> optSmsRequest = smsRequestRepository.findById(smsRequestId);
        if (optSmsRequest.isPresent()) {
            SmsRequest smsRequest = optSmsRequest.get();
            smsRequest.setFailureCode(smsFailureCode.ordinal());
            smsRequest.setFailureComments(failureComments);
            smsRequestRepository.saveAndFlush(smsRequest);
            logger.info("Successfully set failure code for SMS request ID: {} to {}", smsRequestId, smsFailureCode);
        } else {
            logger.error("SMS request ID: {} not found, failure code update failed", smsRequestId);
        }
        return optSmsRequest;
    }


    /**
     *
     * @param query The elasticsearch query.
     * @return Response Entity containing {@link SmsRequestElasticsearchResponse}
     *
     * Handles an Elasticsearch query request and returns the Sms Requests (in elastic search) that satisfy the criteria.
     * Handles various scenarios like:
     * a) Optional Pagination
     * b) Optional Date based filtering
     * c) Optional substring based filtering
     * d) Optional Phone Number based filtering
     */
    public ResponseEntity<SmsRequestElasticsearchResponse> getAllSmsRequestElasticsearchFromQuery(ElasticSearchRequest query) {
        boolean isPageable = isValidPageRequest(query);
        Date effectiveStartTime = query.getStartTime() != null ? parseIstToUtcDate(query.getStartTime()) : Date.from(Instant.EPOCH);
        Date effectiveEndTime = query.getEndTime() != null ? parseIstToUtcDate(query.getEndTime()) : parseIstToUtcDate(MAX_DATE);
        boolean hasPhoneNumber = query.getPhoneNumber() != null;
        List<String> message = query.getMessageContaining() != null ? query.getMessageContaining() : new ArrayList<>();

        logger.info("effectiveStartTime: {}, effectiveEndTime: {}, message containing: {}", effectiveStartTime, effectiveEndTime, message);
        List<SmsRequestElasticsearch> result;

        if (isPageable) {
            result = getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumberPageSize(
                    effectiveStartTime,
                    effectiveEndTime,
                    (hasPhoneNumber ? Optional.of(query.getPhoneNumber()) : Optional.empty()),
                    message,
                    query.getPage(),
                    query.getSize()
            );
        } else {
            result = getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumber(
                    effectiveStartTime, effectiveEndTime,
                    (hasPhoneNumber ? Optional.of(query.getPhoneNumber()) : Optional.empty()),
                    message
            );
        }
        logger.info("Fetched {} SMS requests from Elasticsearch", result.size());

        List<ElasticSearchResponse> _result = new ArrayList<>();
        result.forEach(res ->
                _result.add(
                        ElasticSearchResponse.builder()
                                .phoneNumber(res.getPhoneNumber())
                                .id(res.getId())
                                .smsRequestId(res.getSmsRequestId())
                                .updatedAt(res.getUpdatedAt().toString())
                                .createdAt(res.getCreatedAt().toString())
                                .message(res.getMessage())
                                .build()
                )
        );

        return ResponseEntity.ok(
                SmsRequestElasticsearchResponse
                        .builder()
                        .data(_result)
                        .build()
        );
    }
}