package org.example.notificationsystem.controllers;

import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.ElasticSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The HelloController is used only for testing purposes.
 * It is to be ignored and not used.
 *
 * @author Malladi Pradyumna
 */
@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    ElasticSearchRepository elasticSearchRepository;

    @GetMapping("/")
    public ResponseEntity<List<SmsRequestElasticsearch>> index() {
        logger.info("PINGED Index!");
        logger.debug("Debug level - Hello");
        logger.info("Info level - Hello");
        logger.error("Error level - Hello");
        return ResponseEntity.ok(elasticSearchRepository.getAllSmsRequestsElasticsearch());
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

}
