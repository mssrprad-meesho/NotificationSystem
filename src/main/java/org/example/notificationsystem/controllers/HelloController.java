package org.example.notificationsystem.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Notification System is running....";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

}
