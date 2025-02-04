package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ThirdPartySmsApiRequest {
    private String deliveryChannel;
    private ChannelsThirdPartySmsApiRequest channels;
    private List<DestinationThirdPartySmsApiRequest> destination;
}

