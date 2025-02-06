package org.example.notificationsystem.services.impl;

import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.example.notificationsystem.services.SmsService;
import org.example.notificationsystem.repositories.ElasticSearchRepository;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class  SmsServiceImpl implements SmsService {

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

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticsearch() {
        logger.info("Fetching all SMS requests from Elasticsearch");
        List<SmsRequestElasticsearch> smsRequestElasticsearchList = new ArrayList<>();
        smsRequestElasticsearchRepository.findAll().forEach(
                smsRequestElasticsearchList::add);
        logger.info("Fetched {} SMS requests from Elasticsearch", smsRequestElasticsearchList.size());
        return smsRequestElasticsearchList;
    }

    // Elastic Search queries
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumber(Date from, Date to, Optional<String> number, List<String> terms) {
        logger.info("Fetching SMS requests from Elasticsearch");
        return elasticSearchRepository.getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequest(from, to, number, terms);
    }

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumberPageSize(Date from, Date to, Optional<String> number, List<String> terms, int page, int size) {
        logger.info("Fetching SMS requests from Elasticsearch");
        return elasticSearchRepository.getCreatedAtBetweenAndMessageContainingAndPhoneNumberSearchRequestPageSize(from, to, number, terms, page, size);
    }

    // SmsRequest Queries
    public Optional<SmsRequest> getSmsRequest(Long Id) {
        logger.info("Fetching SMS request by ID: {}", Id);
        return smsRequestRepository.findById(Id);
    }

    public List<SmsRequest> getAllSmsRequests() {
        logger.info("Fetching all SMS requests from MySQL");
        List<SmsRequest> smsRequests = smsRequestRepository.findAll();
        logger.info("Fetched {} SMS requests from MySQL", smsRequests.size());
        return smsRequests;
    }

    public List<SmsRequest> getFinishedSmsRequests() {
        logger.info("Fetching finished SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.FINISHED.ordinal());
    }

    public List<SmsRequest> getInProgressSmsRequests() {
        logger.info("Fetching in-progress SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.IN_PROGRESS.ordinal());
    }

    public List<SmsRequest> getFailedSmsRequests() {
        logger.info("Fetching failed SMS requests");
        return smsRequestRepository.findByStatus(StatusConstants.FAILED.ordinal());
    }

    @Transactional
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

    @Transactional
    public Optional<SmsRequest> setFailureCode(Long smsRequestId, FailureCodeConstants smsFailureCode) {
        logger.info("Setting failure code for SMS request ID: {} to status: {}", smsRequestId, smsFailureCode);
        Optional<SmsRequest> optSmsRequest = smsRequestRepository.findById(smsRequestId);
        if(optSmsRequest.isPresent()) {
            SmsRequest smsRequest = optSmsRequest.get();
            smsRequest.setFailureCode(smsFailureCode.ordinal());
            smsRequestRepository.saveAndFlush(smsRequest);
            logger.info("Successfully set failure code for SMS request ID: {} to {}", smsRequestId, smsFailureCode);
        } else {
            logger.error("SMS request ID: {} not found, failure code update failed", smsRequestId);
        }
        return optSmsRequest;
    }
}