package org.example.notificationsystem.controllers;

import org.example.notificationsystem.dto.request.BlackListRequest;
import org.example.notificationsystem.dto.response.BlackListNumbersResponse;
import org.example.notificationsystem.dto.response.GetAllBlacklistedNumbersResponse;
import org.example.notificationsystem.services.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    @GetMapping("/v1/blacklist")
    public ResponseEntity<?> getBlacklistedPhoneNumbers() {
        return ResponseEntity.ok(
                GetAllBlacklistedNumbersResponse
                        .builder()
                        .data(this.blacklistService.getAllBlacklistedNumbers())
                        .build()
        );
    }

    @PostMapping("/v1/blacklist")
    public ResponseEntity<?> addNumbersToBlacklist(@Valid @RequestBody BlackListRequest blackListRequest) {
        this.blacklistService.addNumbersToBlacklist(blackListRequest.getPhoneNumbers());
        return ResponseEntity.ok(
                BlackListNumbersResponse
                        .builder()
                        .data("Successfully Blacklisted")
                        .build()
                );
    }

    @DeleteMapping("/v1/blacklist")
    public ResponseEntity<?> removeNumbersFromBlacklist(@Valid @RequestBody BlackListRequest blackListRequest) {
        this.blacklistService.removeNumbersFromBlacklist(blackListRequest.getPhoneNumbers());
        return ResponseEntity.ok(
                BlackListNumbersResponse
                        .builder()
                        .data("Successfully whitelisted")
                        .build()
        );
    }
}