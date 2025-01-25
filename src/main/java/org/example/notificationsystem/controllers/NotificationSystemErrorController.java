package org.example.notificationsystem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class NotificationSystemErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSystemErrorController.class);
    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        // Log error details
        logger.error("Error occurred. Status Code: {}, Exception: {}", statusCode, exception == null ? "N/A" : exception.getMessage());

        return String.format("<html><body><h2>Error Page</h2><div>Status code: <b>%s</b></div>" +
                        "<div>Exception Message: <b>%s</b></div><body></html>",
                statusCode, exception == null ? "N/A" : exception.getMessage());
    }
}
