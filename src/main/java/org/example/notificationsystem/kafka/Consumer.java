package org.example.notificationsystem.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.constants.ThirdPartyApiResponseCode;
import org.example.notificationsystem.dto.request.ChannelsThirdPartySmsApiRequest;
import org.example.notificationsystem.dto.request.DestinationThirdPartySmsApiRequest;
import org.example.notificationsystem.dto.request.SmsThirdPartySmsApiRequest;
import org.example.notificationsystem.dto.request.ThirdPartySmsApiRequest;
import org.example.notificationsystem.services.impl.BlacklistServiceImpl;
import org.example.notificationsystem.services.impl.SmsServiceImpl;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Subscribes to a partition (check KafkaConsumerConfig for configuration parameters) and processes the logs received.
 * */
@Service("NotificationService")
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    /**
     * The blacklist and sms service are injected through the constructor for interacting with Redis (to check if a number is blacklisted) and
     * updating the status of the Sms Request in SmsRequest.
     * */
    private final BlacklistServiceImpl blacklistServiceImpl;
    private final SmsServiceImpl smsServiceImpl;

    public Consumer(BlacklistServiceImpl blacklistServiceImpl, SmsServiceImpl smsServiceImpl) {
        this.blacklistServiceImpl = blacklistServiceImpl;
        this.smsServiceImpl = smsServiceImpl;
    }

    /**
     * Handle an Sms Request.
     * */
    @KafkaListener(topics = "${spring.kafka.topic_name}", containerFactory = "NotificationContainerFactory")
    public void consume(ConsumerRecord<Long, Long> record) {
        /**
         * Log the receival of the request.
         * */
        Long smsRequestId = record.value();
        Long key = record.key();
        logger.info("Received record with key: {} and value: {}", key, record.value());

        try {
            /**
             * Get the Sms Request from MySQL
             * */
            Optional<org.example.notificationsystem.models.SmsRequest> optionalSmsRequest = this.smsServiceImpl.getSmsRequest(smsRequestId);

            if (optionalSmsRequest.isPresent()) {
                String phoneNumber = optionalSmsRequest.get().getPhoneNumber();

                logger.info("Found SMS request ID: {} for phone number: {}", smsRequestId, phoneNumber);

                /**
                 * Check if the number is blacklisted or not.
                 * */
                if (blacklistServiceImpl.isNumberBlacklisted(phoneNumber)) {
                    /**
                     * If blacklisted, update the state in MySQL.
                     * */
                    logger.warn("Phone number {} is blacklisted. Marking SMS request ID: {} as FAILED", phoneNumber, smsRequestId);
                    this.smsServiceImpl.setStatus(smsRequestId, StatusConstants.FAILED);
                    this.smsServiceImpl.setFailureCode(smsRequestId, FailureCodeConstants.BLACKLISTED_PHONE_NUMBER);
                } else {
                    logger.info("Phone number {} is not blacklisted. Proceeding with SMS sending...", phoneNumber);
                    /**
                     * Otherwise, make a request to the third party API.
                     * */
                    // Send to third-party API here
                    ThirdPartySmsApiRequest thirdPartySmsApiRequest = ThirdPartySmsApiRequest
                            .builder()
                            .deliveryChannel("sms")
                            .channels(
                                    ChannelsThirdPartySmsApiRequest
                                            .builder()
                                            .sms(
                                                    SmsThirdPartySmsApiRequest
                                                    .builder()
                                                    .text(optionalSmsRequest.get().getMessage())
                                                    .build()
                                            )
                                            .build()
                            )
                            // For now sending only one message at a time
                            .destination(
                                    new ArrayList<DestinationThirdPartySmsApiRequest>(Arrays.asList(
                                            DestinationThirdPartySmsApiRequest
                                                    .builder()
                                                    .msisdn(
                                                            new ArrayList<String>(Arrays.asList(phoneNumber))
                                                    )
                                                    .correlationId(
                                                            UUID.randomUUID().toString()
                                                    )
                                                    .build()
                                    ))
                            )
                            .build();

                    ThirdPartyApiResponseCode thirdPartyApiResponseCode = NotificationSystemUtils.send2ThirdPartyApi(
                            new ArrayList<ThirdPartySmsApiRequest>(
                                    Arrays.asList(thirdPartySmsApiRequest)
                            )
                    );
                    /**
                     * Handle all the possible errors that might occur from the third party API request.
                     * */
                    if(thirdPartyApiResponseCode == ThirdPartyApiResponseCode.SUCCESS){
                        /**
                         * If success, update state to indicate so in MySQL.
                         * */
                        this.smsServiceImpl.setStatus(smsRequestId, StatusConstants.FINISHED);
                        this.smsServiceImpl.setFailureCode(smsRequestId, FailureCodeConstants.SUCCESS);
                    } else {
                        /**
                         * If failure, update state to indicate so in MySQL.
                         * */
                        this.smsServiceImpl.setStatus(smsRequestId, StatusConstants.FAILED);

                        if(thirdPartyApiResponseCode == ThirdPartyApiResponseCode.TIMEOUT){
                            this.smsServiceImpl.setFailureCode(smsRequestId, FailureCodeConstants.EXTERNAL_API_TIMEOUT);
                        }  else if(thirdPartyApiResponseCode == ThirdPartyApiResponseCode.INVALID_REQUEST_BODY){
                            this.smsServiceImpl.setFailureCode(smsRequestId, FailureCodeConstants.INVALID_REQUEST_BODY);
                        } else if(thirdPartyApiResponseCode == ThirdPartyApiResponseCode.MALFORMED_URL){
                            this.smsServiceImpl.setFailureCode(smsRequestId, FailureCodeConstants.INVALID_URL);
                        } else {
                            this.smsServiceImpl.setFailureCode(smsRequestId, FailureCodeConstants.EXTERNAL_API_ERROR);
                        }
                    }
                    logger.info("SMS request ID: {} sent successfully. Marking as FINISHED", smsRequestId);
                }
            } else {
                logger.error("Invalid SMS Request ID: {}. Unable to find phone number or SMS request details.", smsRequestId);
            }
        } catch (Exception e) {
            logger.error("Error processing SMS request ID: {}. Exception: {}", smsRequestId, e.getMessage());
        }
    }
}