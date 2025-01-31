package org.example.notificationsystem.services;

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

    Optional<String> getPhoneNumber(Long smsRequestId);
    void setStatus(Long smsRequestId, Integer smsStatus);

    List<SmsRequestElasticsearch> getAllSmsRequestsElasticsearch();
    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromToPageSize(Date from, Date to, int page, int size);
    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchFromTo(Date from, Date to);
    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromToPageSize(String substr, Date from, Date to, int page, int size);
    List<SmsRequestElasticsearch> getAllSmsRequestsElasticSearchContainingFromTo(String substr, Date from, Date to);

}
