package org.example.notificationsystem.controllers;

import org.example.notificationsystem.NotificationSystemApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/")
    public String index() {
        logger.info("PINGED Index!");
        logger.debug("Debug level - Hello");
        logger.info("Info level - Hello");
        logger.error("Error level - Hello");
        return "Notification System is running....";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

}
