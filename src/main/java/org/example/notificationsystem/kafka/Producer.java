package org.example.notificationsystem.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.springframework.kafka.support.SendResult;

@Service
public class Producer {

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName;

    public Producer(KafkaTemplate<String, String> kafkaTemplate,
                    @Value("${spring.kafka.topic_name}") String topicName){
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public boolean sendMessage(String smsRequestId) {
        try {
            SendResult<String, String> sendResult = kafkaTemplate.send(topicName, smsRequestId).get();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        System.out.println("Sent sms: " + smsRequestId);
        return true;
    }
}

