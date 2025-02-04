package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.notificationsystem.models.SmsRequest;

import java.util.List;

@Data
@Builder
public class GetAllSmsResponse {
    List<SmsRequest> data;
}
