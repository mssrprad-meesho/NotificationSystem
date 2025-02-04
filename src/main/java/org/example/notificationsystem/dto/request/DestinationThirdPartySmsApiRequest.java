package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DestinationThirdPartySmsApiRequest{
    private List<String> msisdn;
    private String correlationId;
}
