package org.example.notificationsystem.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.example.notificationsystem.constants.Time.INDIA_ZONE_ID;

@Configuration
public class KafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value="${spring.kafka.topic_name}")
    private String topicName;

    @Value(value="${spring.kafka.num_partitions}")
    private int numPartitions;

    @Value(value="${spring.kafka.num_replicas}")
    private int numReplicas;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        // Kafka Admin Config
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        KafkaAdmin admin = new KafkaAdmin(configs);
        AdminClient client = AdminClient.create(admin.getConfigurationProperties());

        // Delete existing topic
        ArrayList<String> topicsToDelete = new ArrayList<>();
        topicsToDelete.add(topicName);
        client.deleteTopics(topicsToDelete);
        client.close();

        return admin;
    }

    @Bean
    public NewTopic notificationsystemTopic() {
        return TopicBuilder.name(topicName)
                .partitions(numPartitions)
                .replicas(numReplicas)
                .compact()
                .build();
    }

}
