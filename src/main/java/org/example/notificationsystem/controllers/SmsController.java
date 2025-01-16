package org.example.notificationsystem.controllers;

import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.response.*;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.repositories.SmsRequestElasticsearchRepository;
import org.example.notificationsystem.services.SmsService;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@RestController
public class SmsController {
    private final SmsService smsService;
    private final SmsRequestElasticsearchRepository smsRequestElasticsearchRepository;

    @Autowired
    public SmsController(SmsService smsService, SmsRequestElasticsearchRepository smsRequestElasticsearchRepository) {
        this.smsService = smsService;
        this.smsRequestElasticsearchRepository = smsRequestElasticsearchRepository;
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

    @GetMapping("/v1/sms/pageable/elasticsearch")
    public ResponseEntity<?> getSmsByPhoneNumberAndTimeRangePageable(
            @Valid @RequestBody ElasticSearchRequest query) {
//        List<SmsRequestElasticsearch> result = smsService.getAllSmsRequestsElasticsearch();
        System.out.println("Reached Here!");
        System.out.println(query);
        // Validate query parameters
        NotificationSystemUtils.validateQueryParameters(query);

        // Determine pagination settings
        boolean isPageable = NotificationSystemUtils.isValidPageRequest(query);

        // Set time range boundaries
        LocalDateTime effectiveStartTime = query.getStartTime() != null ? query.getStartTime() : LocalDateTime.MIN;
        LocalDateTime effectiveEndTime = query.getEndTime() != null ? query.getEndTime() : LocalDateTime.MAX;

        // Query based on pagination preference
        List<SmsRequestElasticsearch> result = isPageable ?
                smsService.getAllSmsRequestsElasticSearchFromToPageSize(
                        effectiveStartTime,
                        effectiveEndTime,
                        query.getPage(),
                        query.getSize())
                :
                smsService.getAllSmsRequestsElasticSearchFromTo(
                        effectiveStartTime,
                        effectiveEndTime);

        System.out.println(result);
        System.out.println("Reached Here!");

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
