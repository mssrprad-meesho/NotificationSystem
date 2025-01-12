package org.example.notificationsystem.controllers;

import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.repositories.SmsRequestRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BlacklistController {
    private SmsRequestRepository smsRequestRepository;

    @GetMapping("/v1/sms/finished")
    public List<SmsRequest> getFinishedSmsRequests() {
        return this.smsRequestRepository.findByStatus(StatusConstants.FINISHED.ordinal());
    }

    @GetMapping("/v1/sms/in_progress")
    public List<SmsRequest> getInProgressSmsRequests() {
        return this.smsRequestRepository.findByStatus(StatusConstants.IN_PROGRESS.ordinal());
    }

    @GetMapping("/v1/sms/failed")
    public List<SmsRequest> getFailedSmsRequests() {
        return this.smsRequestRepository.findByStatus(StatusConstants.FAILED.ordinal());
    }
}
