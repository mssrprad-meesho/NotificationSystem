package org.example.notificationsystem.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    @Autowired
    public Producer(KafkaTemplate<String, String> kafkaTemplate,
                    @Value("${spring.kafka.topic_name}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public boolean sendMessage(String smsRequestId) {
        try {
            logger.info("Attempting to send SMS request ID: {} to Kafka topic: {}", smsRequestId, topicName);
            SendResult<String, String> sendResult = kafkaTemplate.send(topicName, smsRequestId).get();
            logger.info("Successfully sent SMS request ID: {} to Kafka topic: {} with result: {}", smsRequestId, topicName, sendResult.getRecordMetadata());
        } catch (Exception e) {
            logger.error("Failed to send SMS request ID: {} to Kafka topic: {}. Exception: {}", smsRequestId, topicName, e.getMessage(), e);
            return false;
        }
        return true;
    }
}