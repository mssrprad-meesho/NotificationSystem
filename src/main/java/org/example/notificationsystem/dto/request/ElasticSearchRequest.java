package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ElasticSearchRequest {
    private Date startTime;
    private Date endTime;
    private Integer page;
    private Integer size;
    private String messageContaining;
    private String phoneNumber;
}