package org.example.notificationsystem.controllers;

import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.response.*;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.services.SmsService;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.example.notificationsystem.utils.NotificationSystemUtils.isValidPageRequest;

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

    @GetMapping("/v1/sms/pageable/elasticsearch/all")
    public ResponseEntity<?> getAllSmsRequestElasticsearch() {
        System.out.println("Reached ElasticSearchRequest");
        return ResponseEntity.ok(smsService.getAllSmsRequestsElasticsearch());
    }

    @GetMapping("/v1/sms/pageable/elasticsearch")
    public ResponseEntity<?> getSmsByPhoneNumberAndTimeRangePageable(
            @Valid @RequestBody ElasticSearchRequest query) {
        // Determine pagination
        boolean isPageable = isValidPageRequest(query);

        // Determine message containing flag
        boolean messageContaining = query.getMessageContaining() != null;

        // Set time range boundaries
        Date effectiveStartTime = query.getStartTime() != null
                ? query.getStartTime()
                : Date.from(Instant.EPOCH);

        Date effectiveEndTime = query.getEndTime() != null
                ? query.getEndTime()
                : Date.from(java.time.Instant.ofEpochMilli(Long.MAX_VALUE));

        // Query based on pagination preference
        List<SmsRequestElasticsearch> result;
        if (isPageable) {
            if (messageContaining) {
                result = smsService.getAllSmsRequestsElasticSearchContainingFromToPageSize(
                        query.getMessageContaining(),
                        effectiveStartTime,
                        effectiveEndTime,
                        query.getPage(),
                        query.getSize());
            } else {
                result = smsService.getAllSmsRequestsElasticSearchFromToPageSize(
                        effectiveStartTime,
                        effectiveEndTime,
                        query.getPage(),
                        query.getSize());
            }
        } else {
            if (messageContaining) {
                result = smsService.getAllSmsRequestsElasticSearchContainingFromTo(
                        query.getMessageContaining(),
                        effectiveStartTime,
                        effectiveEndTime);
            } else {
                result = smsService.getAllSmsRequestsElasticSearchFromTo(
                        effectiveStartTime,
                        effectiveEndTime);
            }
        }

        return ResponseEntity.ok(
                ElasticSearchResponse
                        .builder()
                        .data(result)
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
            return ResponseEntity.ok(SmsRequestResponse
                    .builder()
                    .data(
                            DataResponse
                                    .builder()
                                    .requestId(
                                            this.smsService.createSmsRequest(smsRequest.getPhoneNumber(), smsRequest.getMessage()).getId().toString()
                                    )
                                    .comments("Successfully Sent")
                                    .build()
                    )
                    .build()
            );
        } catch (Exception e) {
            throw new ValidationException("INVALID_REQUEST", e);
        }
    }
}
