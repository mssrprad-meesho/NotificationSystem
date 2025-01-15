package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.kafka.Producer;
import org.example.notificationsystem.models.PhoneNumber;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.repositories.PhoneNumberRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SmsService {

    @Autowired
    private SmsRequestRepository smsRequestRepository;

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired
    private Producer producer;

    @Transactional
    public SmsRequest createSmsRequest(String number, String message) {
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

        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setNumber(phoneNumber);
        smsRequest.setPhoneNumber(phoneNumber.getPhoneNumber());
        smsRequest.setMessage(message);
        smsRequest.setStatus(StatusConstants.IN_PROGRESS.ordinal());
        smsRequest.setCreatedAt(LocalDateTime.now());
        smsRequest.setUpdatedAt(LocalDateTime.now());

        smsRequestRepository.saveAndFlush(smsRequest);

        producer.sendMessage(smsRequest.getId().toString());
        return smsRequest;
    }

    @Transactional
    public List<SmsRequest> getAllSmsRequests() {
        return smsRequestRepository.findAll();
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
