package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import javax.validation.constraints.Pattern;

/**
 * An object to represent the Elastic Search Request.
 * All the fields are optional (if all are optional, the first 10,000 hits are returned without any matching or filtering.
 * */
@Data
@Builder
public class ElasticSearchRequest {

    /**
     * The (optional) createdAfter time (IST) written in readable format.
     * Ex: "05-01-2025 21:19:00"
     * */
    @Pattern(regexp = "^(\\d{2})-(\\d{2})-(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})$")
    private String startTime;

    /**
     * The (optional) createdAfter time (IST) written in readable format.
     * Ex: "05-01-2025 21:19:00"
     * */
    @Pattern(regexp = "^(\\d{2})-(\\d{2})-(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})$")
    private String endTime;

    /**
     * Page number if pagination request.
     * Can be null.
     * */
    private Integer page;

    /**
     * Page size if pagination request.
     * Can be null.
     * */
    private Integer size;

    /**
     * For term based filtering.
     * Only documents having each of these terms in message field will be returned.
     * Can be empty or null.
     * */
    private List<String> messageContaining;

    /**
     * If we want to filter by phone number.
     * (can be null).
     * */
    @Pattern(regexp = "^\\+[1-9]\\d{10,14}$")
    private String phoneNumber;
}