package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Object to represent the response body of a Blacklist request response.
 */
@Data
@Builder
public class BlackListNumbersResponse {
    /**
     * response message.
     */
    private String data;
}
