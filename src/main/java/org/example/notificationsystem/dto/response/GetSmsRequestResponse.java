package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.notificationsystem.models.SmsRequest;

@Data
@Builder
public class GetSmsRequestResponse {
    SmsRequest data;
}