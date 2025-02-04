package org.example.notificationsystem.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    private final KafkaProducer<Long, Long> producer;
    private final String topicName;

    @Autowired
    public Producer(KafkaProducer<Long, Long> producer,
                    @Value("${spring.kafka.topic_name}") String topicName) {
        this.producer = producer;
        this.topicName = topicName;
    }

    public boolean publishSync(Long smsRequestId) {
        logger.info("Attempting to send SMS request ID: {} to Kafka topic: {}", smsRequestId, topicName);

        Long key = smsRequestId;
        Long value = smsRequestId;
        ProducerRecord<Long, Long> producerRecord = new ProducerRecord<>(topicName, key, value);

        RecordMetadata recordMetadata;
        try {
            recordMetadata = producer.send(producerRecord).get();
            logger.info("Successfully sent SMS request ID: {}, to Kafka topic: {}, to partition: {}", smsRequestId, recordMetadata.topic(), recordMetadata.partition());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.info("Failed to send SMS request ID: {} to Kafka topic: {}", smsRequestId, topicName);
            logger.error(e.getMessage(), e);
            logger.error(Arrays.toString(e.getStackTrace()));
            return false;
        }
    }
}