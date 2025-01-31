package org.example.notificationsystem.services.impl;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.example.notificationsystem.services.SmsService;
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


@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final SmsRequestRepository smsRequestRepository;
    private final SmsRequestElasticsearchRepository smsRequestElasticsearchRepository;
    private final Producer producer;

    @Autowired
    public SmsServiceImpl(SmsRequestRepository smsRequestRepository, SmsRequestElasticsearchRepository smsRequestElasticsearchRepository, Producer producer) {
        this.smsRequestRepository = smsRequestRepository;
        this.smsRequestElasticsearchRepository = smsRequestElasticsearchRepository;
        this.producer = producer;
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
        boolean success = producer.publishSync(smsRequest.getId().toString());
        if (success) {
            logger.info("Kafka message sent for SMS request ID {}", smsRequest.getId());
        } else {
            logger.error("Failed to send Kafka message for SMS request ID {}", smsRequest.getId());
        }

        return smsRequest;
    }

    public List<SmsRequest> getAllSmsRequests() {
        logger.info("Fetching all SMS requests from MySQL");
        List<SmsRequest> smsRequests = smsRequestRepository.findAll();
        logger.info("Fetched {} SMS requests from MySQL", smsRequests.size());
        return smsRequests;
    }

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticsearch() {
        logger.info("Fetching all SMS requests from Elasticsearch");
        List<SmsRequestElasticsearch> smsRequestElasticsearchList = new ArrayList<>();
        smsRequestElasticsearchRepository.findAll().forEach(
                smsRequestElasticsearchList::add);
        logger.info("Fetched {} SMS requests from Elasticsearch", smsRequestElasticsearchList.size());
        return smsRequestElasticsearchList;
    }

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromToPageSize(Date from, Date to, int page, int size) {
        logger.info("Fetching SMS requests from Elasticsearch between {} and {} with pagination (page: {}, size: {})", from, to, page, size);
        return smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to, PageRequest.of(page, size));
    }

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromTo(Date from, Date to) {
        logger.info("Fetching SMS requests from Elasticsearch between {} and {}", from, to);
        return smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to);
    }

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToPageSize(String substr, Date from, Date to, int page, int size) {
        logger.info("Fetching SMS requests containing '{}' from Elasticsearch between {} and {} with pagination (page: {}, size: {})", substr, from, to, page, size);
        return smsRequestElasticsearchRepository.findByMessageContainsIgnoreCaseAndCreatedAtIsBetween(substr, from, to, PageRequest.of(page, size));
    }

    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromTo(String substr, Date from, Date to) {
        logger.info("Fetching SMS requests containing '{}' from Elasticsearch between {} and {}", substr, from, to);
        return smsRequestElasticsearchRepository.findByMessageContainsIgnoreCaseAndCreatedAtIsBetween(substr, from, to);
    }

    public Optional<SmsRequest> getSmsRequest(Long Id) {
        logger.info("Fetching SMS request by ID: {}", Id);
        return smsRequestRepository.findById(Id);
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

    public Optional<String> getPhoneNumber(Long smsRequestId) {
        logger.info("Fetching phone number for SMS request ID: {}", smsRequestId);
        Optional<SmsRequest> smsRequest = smsRequestRepository.findById(smsRequestId);
        return smsRequest.map(SmsRequest::getPhoneNumber);
    }

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