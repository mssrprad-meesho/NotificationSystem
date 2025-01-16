package org.example.notificationsystem.services;

import org.elasticsearch.client.RestHighLevelClient;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.PhoneNumber;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.PhoneNumberRepository;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SmsService {

    @Autowired
    private SmsRequestRepository smsRequestRepository;

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired
    private SmsRequestElasticsearchRepository smsRequestElasticsearchRepository;

    @Autowired
    private Producer producer;

    @Transactional
    public SmsRequest createSmsRequest(String number, String message) {
        // Get phone number after checking if new phone number
        PhoneNumber phoneNumber = null;
        Optional<PhoneNumber> phoneNumbers = phoneNumberRepository.findByPhoneNumber(number);
        if (!phoneNumbers.isPresent()) {
            phoneNumber = new PhoneNumber();
            phoneNumber.setPhoneNumber(number);
            phoneNumberRepository.save(phoneNumber);
            phoneNumberRepository.flush();
        } else {
            phoneNumber = phoneNumbers.get();
        }

        // Save into mysql - sms_request
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setNumber(phoneNumber);
        smsRequest.setPhoneNumber(phoneNumber.getPhoneNumber());
        smsRequest.setMessage(message);
        smsRequest.setStatus(StatusConstants.IN_PROGRESS.ordinal());
        smsRequest.setCreatedAt(LocalDateTime.now());
        smsRequest.setUpdatedAt(LocalDateTime.now());

        smsRequestRepository.saveAndFlush(smsRequest);

        // Save into Elastic Search
        System.out.println("Creating Elastic Search object..... " + smsRequest);
        SmsRequestElasticsearch smsRequestElasticsearch = new SmsRequestElasticsearch();
        smsRequestElasticsearch.setCreatedAt(
                smsRequest.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        );
        smsRequestElasticsearch.setUpdatedAt(
                smsRequest.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        );
        smsRequestElasticsearch.setPhoneNumber(smsRequest.getPhoneNumber());
        smsRequestElasticsearch.setMessage(smsRequest.getMessage());
        smsRequestElasticsearch.setSmsRequestId(smsRequest.getId().toString());

        try {
//            smsRequestElasticsearch.toString()
            smsRequestElasticsearchRepository.save(smsRequestElasticsearch);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Failed to save Elastic Search object..... " + smsRequestElasticsearch);
        }

        // Send Kafka Message
        producer.sendMessage(smsRequest.getId().toString());
        return smsRequest;
    }

    @Transactional
    public List<SmsRequest> getAllSmsRequests() {
        return smsRequestRepository.findAll();
    }

    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticsearch() {
        List<SmsRequestElasticsearch> smsRequestElasticsearchList = new ArrayList<>();
        smsRequestElasticsearchRepository.findAll().forEach(
                smsRequestElasticsearchList::add);
        return smsRequestElasticsearchList;
    }

    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromToPageSize(LocalDateTime from, LocalDateTime to, int page, int size) {
        return smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to, PageRequest.of(page, size));
    }

    @Transactional
    public List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromTo(LocalDateTime from, LocalDateTime to) {
        return smsRequestElasticsearchRepository.findByCreatedAtIsBetween(from, to);
    }

    @Transactional
    public Optional<SmsRequest> getSmsRequest(Long Id) {
        return smsRequestRepository.findById(Id);
    }

    @Transactional
    public List<SmsRequest> getFinishedSmsRequests() {
        return this.smsRequestRepository.findByStatus(StatusConstants.FINISHED.ordinal());
    }

    @Transactional
    public List<SmsRequest> getInProgressSmsRequests() {
        return this.smsRequestRepository.findByStatus(StatusConstants.IN_PROGRESS.ordinal());
    }

    @Transactional
    public List<SmsRequest> getFailedSmsRequests() {
        return this.smsRequestRepository.findByStatus(StatusConstants.FAILED.ordinal());
    }

    @Transactional
    public Optional<String> getPhoneNumber(Long smsRequestId) {
        Optional<SmsRequest> smsRequest = this.smsRequestRepository.findById(smsRequestId);
        if (!smsRequest.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(smsRequest.get().getPhoneNumber());
        }
    }

    @Transactional
    public void setStatus(Long smsRequestId, Integer smsStatus) {
        SmsRequest smsRequest = this.smsRequestRepository.findById(smsRequestId).orElse(null);
        if (smsRequest != null) {
            smsRequest.setStatus(smsStatus);
            this.smsRequestRepository.saveAndFlush(smsRequest);
        }
    }
}
