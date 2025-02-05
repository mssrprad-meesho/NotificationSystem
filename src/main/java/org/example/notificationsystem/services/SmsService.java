package org.example.notificationsystem.services;

import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface SmsService {
    SmsRequest createSmsRequest(String number, String message);

    Optional<SmsRequest> getSmsRequest(Long Id);
    List<SmsRequest> getFinishedSmsRequests();
    List<SmsRequest> getInProgressSmsRequests();
    List<SmsRequest> getFailedSmsRequests();
    List<SmsRequest> getAllSmsRequests();

    Optional<SmsRequest> setStatus(Long smsRequestId, StatusConstants smsStatus);
    Optional<SmsRequest> setFailureCode(Long smsRequestId, FailureCodeConstants smsFailureCode);

    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToAndPhoneNumber(Date from, Date to, Optional<String> number, List<String> substr);
}
