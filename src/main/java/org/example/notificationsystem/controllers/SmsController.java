package org.example.notificationsystem.controllers;

import org.example.notificationsystem.constants.ErrorCodeConstants;
import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.dto.response.*;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;
import org.example.notificationsystem.services.impl.SmsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.example.notificationsystem.constants.Time.MAX_DATE;
import static org.example.notificationsystem.utils.NotificationSystemUtils.parseIstToUtcDate;


/**
 * The Sms Controller handles all the /v1/sms/** endpoint queries related to SmsRequests.
 * <p>
 * It handles the following endpoints:
 * <ul>
 *     <li><b>GET /v1/sms/all</b>: Get all Sms Requests data from MySQL</li>
 *     <li><b>GET /v1/sms/finished</b>: Get all finished Sms Requests data from MySQL</li>
 *     <li><b>GET /v1/sms/in_progress</b>: Get all in progress Sms Requests data from MySQL</li>
 *     <li><b>GET /v1/sms/failed</b>: Get all failed Sms Requests data from MySQL</li>
 *     <li><b>GET /v1/sms/elasticsearch/all</b>: Get all Sms Requests data from Elastic Search</li>
 *     <li><b>GET /v1/sms/pageable/elasticsearch</b>: Get requested Sms Requested data from Elastic Search</li>
 *     <li><b>GET /v1/sms/{request_id}</b>: Get the requested Sms Request from MySQL</li>
 *     <li><b>POST /v1/sms/send</b>: Initiate an Sms Request</li>
 *     <li><b></b></li>
 * </ul>
 */
@RestController
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);
    private final SmsServiceImpl smsServiceImpl;

    @Autowired
    public SmsController(SmsServiceImpl smsServiceImpl) {
        this.smsServiceImpl = smsServiceImpl;
    }

    /**
     * Return all the Sms Request data in MySQL using smsServiceImpl.
     *
     * @return ResponseEntity<GetAllSmsResponse> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @GetMapping("/v1/sms/all")
    public ResponseEntity<?> getAllSmsRequests() {
        logger.info("GET /v1/sms/all called");
        try {
            List<SmsRequest> smsRequests = this.smsServiceImpl.getAllSmsRequests();
            logger.info("Fetched {} SMS requests", smsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(smsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching all SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }

    /**
     * Return all the Sms Request data of finished requests in MySQL using smsServiceImpl.
     *
     * @return ResponseEntity<GetAllSmsResponse> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @GetMapping("/v1/sms/finished")
    public ResponseEntity<?> getFinishedSmsRequests() {
        logger.info("GET /v1/sms/finished called");
        try {
            List<SmsRequest> finishedSmsRequests = this.smsServiceImpl.getFinishedSmsRequests();
            logger.info("Fetched {} finished SMS requests", finishedSmsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(finishedSmsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching finished SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }

    /**
     * Return all the Sms Request data of in progress requests in MySQL using smsServiceImpl.
     *
     * @return ResponseEntity<GetAllSmsResponse> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @GetMapping("/v1/sms/in_progress")
    public ResponseEntity<?> getInProgressSmsRequests() {
        logger.info("GET /v1/sms/in_progress called");
        try {
            List<SmsRequest> inProgressSmsRequests = this.smsServiceImpl.getInProgressSmsRequests();
            logger.info("Fetched {} in-progress SMS requests", inProgressSmsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(inProgressSmsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching in-progress SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }

    /**
     * Return all the Sms Request data of failed requests in MySQL using smsServiceImpl.
     *
     * @return ResponseEntity<GetAllSmsResponse> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @GetMapping("/v1/sms/failed")
    public ResponseEntity<?> getFailedSmsRequests() {
        logger.info("GET /v1/sms/failed called");
        try {
            List<SmsRequest> failedSmsRequests = this.smsServiceImpl.getFailedSmsRequests();
            logger.info("Fetched {} failed SMS requests", failedSmsRequests.size());
            return ResponseEntity.ok(
                    GetAllSmsResponse
                            .builder()
                            .data(failedSmsRequests)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching failed SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }

    /**
     * Return all the Sms Request data in Elastic Search of the as per the query in ElasticSearchRequest query using smsServiceImpl.
     * Checks if the
     *
     * @return ResponseEntity<GetAllSmsResponse> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @GetMapping("/v1/sms/pageable/elasticsearch")
    public ResponseEntity<?> getSmsByPhoneNumberAndTimeRangePageable(
            @Valid @RequestBody ElasticSearchRequest query) {
        logger.info("GET /v1/sms/pageable/elasticsearch called with query: {}", query);
        try {
            return smsServiceImpl.getAllSmsRequestElasticsearchFromQuery(query);
        } catch (Exception e) {
            logger.error("Error querying Elasticsearch for SMS requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }

    /**
     * Return the Sms Request in MySQL having the request_id smsServiceImpl.
     * Checks if the
     *
     * @return ResponseEntity<SmsRequest> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @GetMapping("/v1/sms/{request_id}")
    public ResponseEntity<?> getSmsRequestById(@PathVariable Long request_id) {
        logger.info("GET /v1/sms/{} called", request_id);
        try {
            Optional<SmsRequest> optionalSmsRequest = this.smsServiceImpl.getSmsRequest(request_id);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }

    /**
     * Initiates an Sms Request using smsServiceImpl.
     *
     * @return ResponseEntity<SmsRequestResponse> if success
     * @return ResponseEntity<ErrorResponse> otherwise
     */
    @PostMapping("/v1/sms/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> sendSmsRequest(
            @Valid @RequestBody org.example.notificationsystem.dto.request.SmsRequest smsRequest
    ) {
        logger.info("POST /v1/sms/send called with request: {}", smsRequest);
        try {
            String requestId = this.smsServiceImpl.createSmsRequest(smsRequest.getPhoneNumber(), smsRequest.getMessage()).getId().toString();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code(ErrorCodeConstants.INVALID_REQUEST.toString()).message("SERVER ERROR").build());
        }
    }
}
