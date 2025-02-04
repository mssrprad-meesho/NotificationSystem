package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SmsThirdPartySmsApiRequest{
    private String text;
}
