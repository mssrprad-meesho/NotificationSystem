package org.example.notificationsystem.controllers;

import org.example.notificationsystem.dto.response.*;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Optional;

@RestController
public class SmsController {
    private final SmsService smsService;

    @Autowired
    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @GetMapping("/v1/sms/all")
    public ResponseEntity<?> getAllSmsRequests() {
        return ResponseEntity.ok(
                GetAllSmsResponse
                        .builder()
                        .data(this.smsService.getAllSmsRequests())
                        .build()
        );
    }

    @GetMapping("/v1/sms/finished")
    public ResponseEntity<?> getFinishedSmsRequests() {
        return ResponseEntity.ok(
                GetAllSmsResponse
                        .builder()
                        .data(this.smsService.getFinishedSmsRequests())
                        .build()
        );
    }

    @GetMapping("/v1/sms/in_progress")
    public ResponseEntity<?> getInProgressSmsRequests() {
        return ResponseEntity.ok(
                GetAllSmsResponse
                        .builder()
                        .data(this.smsService.getInProgressSmsRequests())
                        .build()
        );
    }

    @GetMapping("/v1/sms/failed")
    public ResponseEntity<?> getFailedSmsRequests() {
        return ResponseEntity.ok(
                GetAllSmsResponse
                        .builder()
                        .data(this.smsService.getFailedSmsRequests())
                        .build()
        );
    }

    @GetMapping("/v1/sms/{request_id}")
    public ResponseEntity<?> getSmsRequestById(@PathVariable Long request_id) {
        // DB Query for the SMS Request
        Optional<SmsRequest> optionalSmsRequest = this.smsService.getSmsRequest(request_id);

        // If the SmsRequest is present in DB, return it
        // Else return an Error Message
        if (optionalSmsRequest.isPresent()) {
            return ResponseEntity.ok(
                    GetSmsRequestResponse
                            .builder()
                            .data(optionalSmsRequest.get())
                            .build()
            );
        } else {
            ErrorResponse errorResponse = ErrorResponse
                    .builder()
                    .code("INVALID_REQUEST")
                    .message("Invalid request")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/v1/sms/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> sendSmsRequest(
            @Valid @RequestBody org.example.notificationsystem.dto.request.SmsRequest smsRequest
    ) {
        try {
            return ResponseEntity.ok(GetSmsRequestResponse
                    .builder()
                    .data(
                        this.smsService.createSmsRequest(smsRequest.getPhoneNumber(), smsRequest.getMessage())
                    )
                            .build()
                    );
        } catch (Exception e) {
            throw new ValidationException("INVALID_REQUEST", e);
        }
    }

}
