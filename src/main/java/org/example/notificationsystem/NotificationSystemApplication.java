package org.example.notificationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//@EnableCaching
public class NotificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationSystemApplication.class, args);
    }

}