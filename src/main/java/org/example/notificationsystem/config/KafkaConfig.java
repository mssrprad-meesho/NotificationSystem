package org.example.notificationsystem.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import java.util.*;

@Configuration
public class KafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value="${spring.kafka.topic_name}")
    private String topicName;

    /* https://www.confluent.io/blog/how-choose-number-topics-partitions-kafka-cluster/
    A rough formula for picking the number of partitions is based on throughput.
    You measure the throughout that you can achieve on a single partition for production (call it p) and consumption (call it c).
    Let’s say your target throughput is t. Then you need to have at least max(t/p, t/c) partitions.
    */
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
