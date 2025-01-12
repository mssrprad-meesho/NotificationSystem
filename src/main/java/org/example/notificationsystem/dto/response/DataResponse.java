package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataResponse {
    private String requestId;
    private String comments;
}
