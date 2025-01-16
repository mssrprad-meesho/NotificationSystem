package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class GetAllBlacklistedNumbersResponse {
    List<String> data;
}