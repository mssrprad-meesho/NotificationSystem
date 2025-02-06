package org.example.notificationsystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Object to represent a BlackListRequest.
 * Has the phone numbers to be blacklisted/whitelisted.
 * */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlackListRequest {
    /**
     * The phone numbers to be blacklisted or whitelisted.
     * */
    @NotNull
    private List<String> phoneNumbers;
}