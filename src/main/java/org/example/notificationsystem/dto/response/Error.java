package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Object to represent any custom error response.
 * */
@Data
@Builder
public class Error {
    /**
     * The error response.
     * */
    private ErrorResponse error;
}
