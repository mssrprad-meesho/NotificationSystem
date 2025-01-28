package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.PhoneNumber;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.PhoneNumberRepository;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for handling SMS requests.
 * It provides methods to create, update, and fetch SMS requests from both MySQL and Elasticsearch,
 * and interacts with Kafka to send SMS-related messages.
 */
@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final SmsRequestRepository smsRequestRepository;
    private final PhoneNumberRepository phoneNumberRepository;
    private final SmsRequestElasticsearchRepository smsRequestElasticsearchRepository;
    private final Producer producer;

    /**
     * Constructor for SmsService.
     *
     * @param smsRequestRepository Repository to interact with SMS requests in MySQL.
     * @param phoneNumberRepository Repository to interact with phone numbers in MySQL.
     * @param smsRequestElasticsearchRepository Repository to interact with SMS requests in Elasticsearch.
     * @param producer Kafka producer to send SMS request messages.
     */
    @Autowired
    public SmsService(SmsRequestRepository smsRequestRepository, PhoneNumberRepository phoneNumberRepository,
                      SmsRequestElasticsearchRepository smsRequestElasticsearchRepository, Producer producer) {
        this.smsRequestRepository = smsRequestRepository;
        this.phoneNumberRepository = phoneNumberRepository;
        this.smsRequestElasticsearchRepository = smsRequestElasticsearchRepository;
        this.producer = producer;
    }

    /**
     * Creates a new SMS request. It checks if the phone number is new, saves it to the database if necessary,
     * then creates an SMS request and saves it in both MySQL and Elasticsearch. A Kafka message is sent as well.
     *
     * @param number The phone number to send the SMS to.
     * @param message The message to be sent.
     * @return The created SmsRequest object.
     */
    @Transactional
    public SmsRequest createSmsRequest(String number, String message) {
        logger.info("Creating SMS request for number: {}", number);

        // Get phone number after checking if new phone number
        PhoneNumber phoneNumber;
        Optional<PhoneNumber> phoneNumbers = phoneNumberRepository.findByPhoneNumber(number);
        if (!phoneNumbers.isPresent()) {
            phoneNumber = new PhoneNumber();
            phoneNumber.setPhoneNumber(number);
            phoneNumberRepository.save(phoneNumber);
            phoneNumberRepository.flush();
            logger.info("New phone number {} added to database", number);
        } else {
            phoneNumber = phoneNumbers.get();
            logger.info("Phone number {} already exists in database", number);
        }

        // Save into MySQL - sms_request
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setNumber(phoneNumber);
        smsRequest.setPhoneNumber(phoneNumber.getPhoneNumber());
        smsRequest.setMessage(message);
        smsRequest.setStatus(StatusConstants.IN_PROGRESS.ordinal());
        smsRequest.setCreatedAt(NotificationSystemUtils.getNowAsDateIST());
        smsRequest.setUpdatedAt(NotificationSystemUtils.getNowAsDateIST());

        smsRequestRepository.saveAndFlush(smsRequest);
        logger.info("SMS request for number {} saved to MySQL with status {}", number, StatusConstants.IN_PROGRESS.name());

        // Save into Elasticsearch
        try {
            SmsRequestElasticsearch smsRequestElasticsearch = NotificationSystemUtils.getSmsRequestElasticsearchFromSmsRequest(smsRequest);
            smsRequestElasticsearchRepository.save(smsRequestElasticsearch);
            logger.info("SMS request for number {} saved to Elasticsearch", number);
        } catch (Exception e) {
            logger.error("Failed to save SMS request to Elasticsearch for number {}: {}", number, e.getMessage());
        }

        // Send Kafka Message
        boolean success = producer.sendMessage(smsRequest.getId().toString());
        if (success) {
            logger.info("Kafka message sent for SMS request ID {}", smsRequest.getId());
        } else {
            logger.error("Failed to send Kafka message for SMS request ID {}", smsRequest.getId());
        }

        return smsRequest;
    }

    /**
     * Retrieves all SMS requests from MySQL.
     *
     * @return A list of all SMS requests in MySQL.
     */
    @Transactional
    public List<SmsRequest> getAllSmsRequests() {
        logger.info("Fetching all SMS requests from MySQL");
        List<SmsRequest> smsRequests = smsRequestRepository.findAll();
        logger.info("Fetched {} SMS requests from MySQL", smsRequests.size());
        return smsRequests;
    }

    /**
     * Retrieves all SMS requests from Elasticsearch.
     *
     * @return A list of all SMS requests in Elasticsearch.
     */
    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticsearch() {
        logger.info("Fetching all SMS requests from Elasticsearch");
        List<SmsRequestElasticsearch> smsRequestElasticsearchList = new ArrayList<>();
        smsRequestElasticsearchRepository.findAll().forEach(
                smsRequestElasticsearchList::add);
        logger.info("Fetched {} SMS requests from Elasticsearch", smsRequestElasticsearchList.size());
        return smsRequestElasticsearchList;
    }

    /**
     * Retrieves SMS requests from Elasticsearch filtered by date range, with pagination.
     *
     * @param from The start date of the range.
     * @param to The end date of the range.
     * @param page The page number for pagination.
     * @param size The number of results per page.
     * @return A list of SMS requests matching the date range and pagination.
     */
    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromToPageSize(Date from, Date to, int page, int size) {
        logger.info("Fetching SMS requests from Elasticsearch between {} and {} with pagination (page: {}, size: {})", from, to, page, size);
        return smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to, PageRequest.of(page, size));
    }

    /**
     * Retrieves SMS requests from Elasticsearch filtered by date range.
     *
     * @param from The start date of the range.
     * @param to The end date of the range.
     * @return A list of SMS requests matching the date range.
     */
    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromTo(Date from, Date to) {
        logger.info("Fetching SMS requests from Elasticsearch between {} and {}", from, to);
        return smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to);
    }

    /**
     * Retrieves SMS requests from Elasticsearch that contain a substring in the message,
     * filtered by date range, with pagination.
     *
     * @param substr The substring to search for in the SMS request messages.
     * @param from The start date of the range.
     * @param to The end date of the range.
     * @param page The page number for pagination.
     * @param size The number of results per page.
     * @return A list of SMS requests matching the substring and date range with pagination.
     */
    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToPageSize(String substr, Date from, Date to, int page, int size) {
        logger.info("Fetching SMS requests containing '{}' from Elasticsearch between {} and {} with pagination (page: {}, size: {})", substr, from, to, page, size);
        return smsRequestElasticsearchRepository.findByMessageContainsIgnoreCaseAndCreatedAtIsBetween(substr, from, to, PageRequest.of(page, size));
    }

    /**
     * Retrieves SMS requests from Elasticsearch that contain a substring in the message,
     * filtered by date range.
     *
     * @param substr The substring to search for in the SMS request messages.
     * @param from The start date of the range.
     * @param to The end date of the range.
     * @return A list of SMS requests matching the substring and date range.
     */
    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromTo(String substr, Date from, Date to) {
        logger.info("Fetching SMS requests containing '{}' from Elasticsearch between {} and {}", substr, from, to);
        return smsRequestElasticsearchRepository.findByMessageContainsIgnoreCaseAndCreatedAtIsBetween(substr, from, to);
    }

    /**
     * Retrieves an SMS request by its ID from MySQL.
     *
     * @param Id The ID of the SMS request to retrieve.
     * @return The SMS request if found, or an empty Optional if not found.
     */
    @Transactional
    public Optional<SmsRequest> getSmsRequest(Long Id) {
        logger.info("Fetching SMS request by ID: {}", Id);
        return smsRequestRepository.findById(Id);
    }

    /**
     * Retrieves all finished SMS requests from MySQL.
     *
     * @return A list of finished SMS requests.
     */
    @Transactional
    public List<SmsRequest> getFinishedSmsRequests() {
        logger.info("Fetching finished SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.FINISHED.ordinal());
    }

    /**
     * Retrieves all in-progress SMS requests from MySQL.
     *
     * @return A list of in-progress SMS requests.
     */
    @Transactional
    public List<SmsRequest> getInProgressSmsRequests() {
        logger.info("Fetching in-progress SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.IN_PROGRESS.ordinal());
    }

    /**
     * Retrieves all failed SMS requests from MySQL.
     *
     * @return A list of failed SMS requests.
     */
    @Transactional
    public List<SmsRequest> getFailedSmsRequests() {
        logger.info("Fetching failed SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.FAILED.ordinal());
    }

    /**
     * Retrieves the phone number associated with a specific SMS request ID.
     *
     * @param smsRequestId The ID of the SMS request.
     * @return The phone number associated with the SMS request, or an empty Optional if not found.
     */
    @Transactional
    public Optional<String> getPhoneNumber(Long smsRequestId) {
        logger.info("Fetching phone number for SMS request ID: {}", smsRequestId);
        Optional<SmsRequest> smsRequest = smsRequestRepository.findById(smsRequestId);
        return smsRequest.map(SmsRequest::getPhoneNumber);
    }

    /**
     * Updates the status of an SMS request.
     *
     * @param smsRequestId The ID of the SMS request to update.
     * @param smsStatus The new status to set for the SMS request.
     */
    @Transactional
    public void setStatus(Long smsRequestId, Integer smsStatus) {
        logger.info("Updating status for SMS request ID: {} to status: {}", smsRequestId, smsStatus);
        SmsRequest smsRequest = smsRequestRepository.findById(smsRequestId).orElse(null);
        if (smsRequest != null) {
            smsRequest.setStatus(smsStatus);
            smsRequestRepository.saveAndFlush(smsRequest);
            logger.info("Successfully updated status for SMS request ID: {} to {}", smsRequestId, smsStatus);
        } else {
            logger.error("SMS request ID: {} not found, status update failed", smsRequestId);
        }
    }
}