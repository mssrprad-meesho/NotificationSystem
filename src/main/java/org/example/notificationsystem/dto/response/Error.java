package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Error {
    private ErrorResponse error;
}
