package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ThirdPartyApiResponse
{
    private String message;
    private String status;
}
