package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;


/**
 * Object to represent a custom error response.
 * */
@Data
@Builder
public class ErrorResponse {
    /**
     * The error code.
     * */
    private String code;
    /**
     * The error message.s
     * */
    private String message;
};