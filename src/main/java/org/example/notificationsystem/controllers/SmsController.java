package org.example.notificationsystem.controllers;

import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.response.*;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);
    private final SmsService smsService;

    @Autowired
    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @GetMapping("/v1/sms/all")
    public ResponseEntity<?> getAllSmsRequests() {
        logger.info("GET /v1/sms/all called");
        try {
            List<SmsRequest> smsRequests = this.smsService.getAllSmsRequests();
            logger.info("Fetched {} SMS requests", smsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(smsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching all SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching SMS requests");
        }
    }

    @GetMapping("/v1/sms/finished")
    public ResponseEntity<?> getFinishedSmsRequests() {
        logger.info("GET /v1/sms/finished called");
        try {
            List<SmsRequest> finishedSmsRequests = this.smsService.getFinishedSmsRequests();
            logger.info("Fetched {} finished SMS requests", finishedSmsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(finishedSmsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching finished SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching finished SMS requests");
        }
    }

    @GetMapping("/v1/sms/in_progress")
    public ResponseEntity<?> getInProgressSmsRequests() {
        logger.info("GET /v1/sms/in_progress called");
        try {
            List<SmsRequest> inProgressSmsRequests = this.smsService.getInProgressSmsRequests();
            logger.info("Fetched {} in-progress SMS requests", inProgressSmsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(inProgressSmsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching in-progress SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching in-progress SMS requests");
        }
    }

    @GetMapping("/v1/sms/failed")
    public ResponseEntity<?> getFailedSmsRequests() {
        logger.info("GET /v1/sms/failed called");
        try {
            List<SmsRequest> failedSmsRequests = this.smsService.getFailedSmsRequests();
            logger.info("Fetched {} failed SMS requests", failedSmsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(failedSmsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching failed SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching failed SMS requests");
        }
    }

    @GetMapping("/v1/sms/pageable/elasticsearch/all")
    public ResponseEntity<?> getAllSmsRequestElasticsearch() {
        logger.info("GET /v1/sms/pageable/elasticsearch/all called");
        try {
            List<SmsRequestElasticsearch> result = smsService.getAllSmsRequestsElasticsearch();
            logger.info("Fetched {} SMS requests from Elasticsearch", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching SMS requests from Elasticsearch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching SMS requests from Elasticsearch");
        }
    }

    @GetMapping("/v1/sms/pageable/elasticsearch")
    public ResponseEntity<?> getSmsByPhoneNumberAndTimeRangePageable(
            @Valid @RequestBody ElasticSearchRequest query) {
        logger.info("GET /v1/sms/pageable/elasticsearch called with query: {}", query);
        try {
            boolean isPageable = isValidPageRequest(query);
            boolean messageContaining = query.getMessageContaining() != null;
            Date effectiveStartTime = query.getStartTime() != null ? query.getStartTime() : Date.from(Instant.EPOCH);
            Date effectiveEndTime = query.getEndTime() != null ? query.getEndTime() : Date.from(Instant.MAX);

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
            logger.info("Fetched {} SMS requests from Elasticsearch", result.size());
            return ResponseEntity.ok(
                    ElasticSearchResponse
                            .builder()
                            .data(result)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error querying Elasticsearch for SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error querying Elasticsearch");
        }
    }

    @GetMapping("/v1/sms/{request_id}")
    public ResponseEntity<?> getSmsRequestById(@PathVariable Long request_id) {
        logger.info("GET /v1/sms/{} called", request_id);
        try {
            Optional<SmsRequest> optionalSmsRequest = this.smsService.getSmsRequest(request_id);
            if (optionalSmsRequest.isPresent()) {
                logger.info("Found SMS request with ID: {}", request_id);
                return ResponseEntity.ok(
                        GetSmsRequestResponse
                                .builder()
                                .data(optionalSmsRequest.get())
                                .build()
                );
            } else {
                logger.warn("SMS request with ID: {} not found", request_id);
                ErrorResponse errorResponse = ErrorResponse
                        .builder()
                        .code("INVALID_REQUEST")
                        .message("Invalid request")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("Error fetching SMS request with ID: {}", request_id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching SMS request");
        }
    }

    @PostMapping("/v1/sms/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> sendSmsRequest(
            @Valid @RequestBody org.example.notificationsystem.dto.request.SmsRequest smsRequest
    ) {
        logger.info("POST /v1/sms/send called with request: {}", smsRequest);
        try {
            String requestId = this.smsService.createSmsRequest(smsRequest.getPhoneNumber(), smsRequest.getMessage()).getId().toString();
            logger.info("Successfully created SMS request with ID: {}", requestId);
            return ResponseEntity.ok(SmsRequestResponse
                    .builder()
                    .data(
                            DataResponse
                                    .builder()
                                    .requestId(requestId)
                                    .comments("Successfully Sent")
                                    .build()
                    )
                    .build()
            );
        } catch (Exception e) {
            logger.error("Error sending SMS request", e);
            throw new ValidationException("INVALID_REQUEST", e);
        }
    }
}
