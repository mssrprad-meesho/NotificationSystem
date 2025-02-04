package org.example.notificationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

@SpringBootApplication
public class NotificationSystemApplication {
    // Entrypoint
    public static void main(String[] args) {
        SpringApplication.run(NotificationSystemApplication.class, args);
    }
}