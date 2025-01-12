package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.models.PhoneNumber;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.repositories.PhoneNumberRepository;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
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
        return smsRequest;
    }

    @Transactional
    public List<SmsRequest> getAllSmsRequests() {
        return smsRequestRepository.findAll();
    }

    @Transactional
    public SmsRequest getSmsRequest(Long Id) {
        return smsRequestRepository.findById(Id).orElse(null);
    }
}
