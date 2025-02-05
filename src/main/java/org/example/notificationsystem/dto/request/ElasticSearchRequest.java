package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
@Builder
public class ElasticSearchRequest {

    @Pattern(regexp = "^(\\d{2})-(\\d{2})-(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})$")
    private String startTime;

    @Pattern(regexp = "^(\\d{2})-(\\d{2})-(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})$")
    private String endTime;

    private Integer page;
    private Integer size;
    private String messageContaining;

    @Pattern(regexp = "^\\+[1-9]\\d{10,14}$")
    private String phoneNumber;
}