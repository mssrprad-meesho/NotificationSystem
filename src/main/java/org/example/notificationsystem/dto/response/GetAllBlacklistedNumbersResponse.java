package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * Object to represent the response body of the GetAllBlacklistedNumbersResponse
 * */
@Data
@Builder
public class GetAllBlacklistedNumbersResponse {
    /**
     * The blacklisted numbers.
     * */
    List<String> data;
}