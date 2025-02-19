package org.example.notificationsystem.controllers;

import org.example.notificationsystem.dto.request.BlackListRequest;
import org.example.notificationsystem.dto.response.BlackListNumbersResponse;
import org.example.notificationsystem.dto.response.GetAllBlacklistedNumbersResponse;
import org.example.notificationsystem.services.impl.BlacklistServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Set;


/**
 * The Blacklist Controller handles all the /v1/* endpoints related to the blacklisting functionality.
 * It handles the following endpoints:
 * <ul>
 *     <li><b>GET /v1/blacklist</b>: Get all Blacklisted Phone Numbers</li>
 *     <li><b>POST /v1/blacklist</b>: Blacklist a list of Phone Numbers</li>
 *     <li><b>DELETE /v1/blacklist</b>: Whitelist a list of Phone Numbers</li>
 * </ul>
 *
 * @author Malladi Pradyumna
 */

@RestController
public class BlacklistController {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistController.class);

    private final BlacklistServiceImpl blacklistServiceImpl;

    public BlacklistController(BlacklistServiceImpl blacklistServiceImpl) {
        this.blacklistServiceImpl = blacklistServiceImpl;
    }

    /**
     * Get all blacklisted phone numbers
     */
    @GetMapping("/v1/blacklist")
    public ResponseEntity<?> getBlacklistedPhoneNumbers() {
        logger.info("GET /v1/blacklist called");
        try {
            Set<String> blacklistedNumbers = this.blacklistServiceImpl.getAllBlacklistedNumbers();
            logger.info("Fetched {} blacklisted numbers", blacklistedNumbers.size());
            return ResponseEntity.ok(
                    GetAllBlacklistedNumbersResponse
                            .builder()
                            .data(new ArrayList<>(blacklistedNumbers))
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching blacklisted numbers", e);
            return ResponseEntity.status(500).body("Error fetching blacklisted numbers");
        }
    }

    /**
     * Handle the BlackList Request and blacklist a list of phone numbers.
     */
    @PostMapping("/v1/blacklist")
    public ResponseEntity<?> addNumbersToBlacklist(@Valid @RequestBody BlackListRequest blackListRequest) {
        logger.info("POST /v1/blacklist called with request: {}", blackListRequest);
        try {
            boolean result = this.blacklistServiceImpl.addNumbersToBlacklist(blackListRequest.getPhoneNumbers().toArray(new String[0]));
            if (result) {
                logger.info("Successfully blacklisted {} numbers", blackListRequest.getPhoneNumbers().size());
                return ResponseEntity.ok(
                        BlackListNumbersResponse
                                .builder()
                                .data("Successfully Blacklisted")
                                .build()
                );
            } else {
                throw new Exception("Error adding numbers to blacklist");
            }
        } catch (Exception e) {
            logger.error("Error blacklisting numbers", e);
            return ResponseEntity.status(500).body("Error blacklisting numbers");
        }
    }

    /**
     * Handle the BlacklistRequest and whitelist a list of phone numbers.
     */
    @DeleteMapping("/v1/blacklist")
    public ResponseEntity<?> removeNumbersFromBlacklist(@Valid @RequestBody BlackListRequest blackListRequest) {
        logger.info("DELETE /v1/blacklist called with request: {}", blackListRequest);
        try {
            boolean res = this.blacklistServiceImpl.removeNumbersFromBlacklist(blackListRequest.getPhoneNumbers().toArray(new String[0]));
            if (res) {
                logger.info("Successfully whitelisted {} numbers", blackListRequest.getPhoneNumbers().size());
                return ResponseEntity.ok(
                        BlackListNumbersResponse
                                .builder()
                                .data("Successfully whitelisted")
                                .build()
                );
            } else {
                throw new Exception("Error removing numbers from blacklist");
            }
        } catch (Exception e) {
            logger.error("Error removing numbers from blacklist", e);
            return ResponseEntity.status(500).body("Error removing numbers from blacklist");
        }
    }
}
