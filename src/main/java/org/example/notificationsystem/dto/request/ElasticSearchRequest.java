package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
@Builder
public class ElasticSearchRequest {
    private Date startTime;

    private Date endTime;

    private int page = -1;

    private int size = -1;
}
