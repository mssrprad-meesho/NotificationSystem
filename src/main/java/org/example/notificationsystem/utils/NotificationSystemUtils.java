package org.example.notificationsystem.utils;

import org.example.notificationsystem.dto.request.ElasticSearchRequest;
import org.example.notificationsystem.models.SmsRequest;
import org.example.notificationsystem.models.SmsRequestElasticsearch;

import java.time.Instant;
import java.util.Date;

import static org.example.notificationsystem.constants.Time.INDIA_ZONE_ID;

public class NotificationSystemUtils {
    public static void validateQueryParameters(ElasticSearchRequest query) {

    }

    public static boolean isValidPageRequest(ElasticSearchRequest query) {
        return true;
    }

    public static SmsRequestElasticsearch getSmsRequestElasticsearchFromSmsRequest(SmsRequest smsRequest) {
        SmsRequestElasticsearch smsRequestElasticsearch = new SmsRequestElasticsearch();
        smsRequestElasticsearch.setCreatedAt(
                smsRequest.getCreatedAt()
        );
        smsRequestElasticsearch.setUpdatedAt(
                smsRequest.getUpdatedAt()
        );
        smsRequestElasticsearch.setPhoneNumber(smsRequest.getPhoneNumber());
        smsRequestElasticsearch.setMessage(smsRequest.getMessage());
        smsRequestElasticsearch.setSmsRequestId(smsRequest.getId().toString());
        return smsRequestElasticsearch;
    }

    public static Date getNowAsDateIST(){
        return Date.from(Instant.now().atZone(INDIA_ZONE_ID).toInstant());
    }
}
