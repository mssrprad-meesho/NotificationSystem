package org.example.notificationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

@SpringBootApplication
public class NotificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationSystemApplication.class, args);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        Objects.requireNonNull(template.getConnectionFactory()).getConnection().serverCommands().flushAll();
        return template;
    }
}