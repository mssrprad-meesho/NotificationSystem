package org.example.notificationsystem.dto.request;

import lombok.Data;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
public class SmsRequest {
    @NotNull
    @Pattern(regexp = "^\\+[1-9]\\d{10,14}$")
    private String phoneNumber;

    @NotBlank
    @Size(max = 10000)
    private String message;
}