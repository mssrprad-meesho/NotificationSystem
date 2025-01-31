package org.example.notificationsystem.kafka;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.services.impl.BlacklistServiceImpl;
import org.example.notificationsystem.services.impl.SmsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("NotificationService")
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final BlacklistServiceImpl blacklistServiceImpl;

    private final SmsServiceImpl smsServiceImpl;

    public Consumer(BlacklistServiceImpl blacklistServiceImpl, SmsServiceImpl smsServiceImpl) {
        this.blacklistServiceImpl = blacklistServiceImpl;
        this.smsServiceImpl = smsServiceImpl;
    }

    @KafkaListener(topics = "${spring.kafka.topic_name}", containerFactory = "NotificationContainerFactory")
    public void consume(@Payload String smsRequestId, Acknowledgment ack) {
        logger.info("Received Kafka message for SMS request ID: {}", smsRequestId);

        try {
            Optional<String> optionalPhoneNumber = this.smsServiceImpl.getPhoneNumber(Long.parseLong(smsRequestId));
            Optional<org.example.notificationsystem.models.SmsRequest> optionalSmsRequest = this.smsServiceImpl.getSmsRequest(Long.parseLong(smsRequestId));

            if (optionalPhoneNumber.isPresent() && optionalSmsRequest.isPresent()) {
                String phoneNumber = optionalPhoneNumber.get();

                logger.info("Found SMS request ID: {} for phone number: {}", smsRequestId, phoneNumber);

                if (blacklistServiceImpl.isNumberBlacklisted(phoneNumber)) {
                    logger.warn("Phone number {} is blacklisted. Marking SMS request ID: {} as FAILED", phoneNumber, smsRequestId);
                    this.smsServiceImpl.setStatus(Long.parseLong(smsRequestId), StatusConstants.FAILED.ordinal());
                } else {
                    logger.info("Phone number {} is not blacklisted. Proceeding with SMS sending...", phoneNumber);

                    // Send to third-party API here


                    logger.info("SMS request ID: {} sent successfully. Marking as FINISHED", smsRequestId);
                    this.smsServiceImpl.setStatus(Long.parseLong(smsRequestId), StatusConstants.FINISHED.ordinal());
                }
            } else {
                logger.error("Invalid SMS Request ID: {}. Unable to find phone number or SMS request details.", smsRequestId);
            }
        } catch (Exception e) {
            logger.error("Error processing SMS request ID: {}. Exception: {}", smsRequestId, e.getMessage());
        }

        ack.acknowledge();
        logger.info("Acknowledgment sent for SMS request ID: {}", smsRequestId);
    }
}