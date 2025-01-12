package org.example.notificationsystem.controllers;

import org.example.notificationsystem.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Null;
import java.util.List;

@RestController
public class SmsController {
    private final SmsService smsService;

    @Autowired
    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @GetMapping("/v1/sms/all")
    public List<org.example.notificationsystem.models.SmsRequest> getAllSmsRequests() {
        return this.smsService.getAllSmsRequests();
    }

    @PostMapping("/v1/sms/send")
    public org.example.notificationsystem.models.SmsRequest sendSmsRequest(
            @Valid @RequestBody org.example.notificationsystem.dto.request.SmsRequest smsRequest
    ) {
        try {
            return this.smsService.createSmsRequest(smsRequest.getPhoneNumber(), smsRequest.getMessage());
        } catch (Exception e) {
            throw new ValidationException("INVALID_REQUEST", e);
        }
    }

    @GetMapping("/v1/sms/{request_id}")
    public org.example.notificationsystem.models.SmsRequest getSmsRequestById(@PathVariable Long request_id) {
        try {
            return this.smsService.getSmsRequest(request_id);
        } catch (Exception e) {
            throw new ValidationException("INVALID_REQUEST", e);
        }
    }

}
