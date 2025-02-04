package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChannelsThirdPartySmsApiRequest{
    private SmsThirdPartySmsApiRequest sms;
}
