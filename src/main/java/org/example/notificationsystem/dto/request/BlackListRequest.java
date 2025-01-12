package org.example.notificationsystem.dto.request;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlackListRequest {
    private List<String> phoneNumbers;
}