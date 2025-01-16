package org.example.notificationsystem.kafka;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.services.BlacklistService;
import org.example.notificationsystem.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class Consumer {

    private final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private SmsService smsService;

    @KafkaListener(topics = "notificationsystem", groupId = "group_id")
    public void consume(String smsRequestId) throws IOException {
        Optional<String> optionalPhoneNumber = this.smsService.getPhoneNumber(Long.parseLong(smsRequestId));
        Optional<org.example.notificationsystem.models.SmsRequest> optionalSmsRequest = this.smsService.getSmsRequest(Long.parseLong(smsRequestId));
        if (optionalPhoneNumber.isPresent() && optionalSmsRequest.isPresent()) {
            if (blacklistService.isNumberBlacklisted(optionalPhoneNumber.get())) {
                logger.info("Phone number", optionalPhoneNumber.get(), " is blacklisted.");
                this.smsService.setStatus(Long.parseLong(smsRequestId), StatusConstants.FAILED.ordinal());
            } else {
                logger.info("Phone number", optionalPhoneNumber.get(), " is not blacklisted");

                // Send to third party api here
                this.smsService.setStatus(Long.parseLong(smsRequestId), StatusConstants.FINISHED.ordinal());
            }
        } else {
            logger.info("Invalid Sms Request", smsRequestId);
        }
    }
}
