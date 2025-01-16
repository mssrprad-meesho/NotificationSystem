package org.example.notificationsystem.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class BlackListRequest {
    @NotNull
    private List<String> phoneNumbers;

    public BlackListRequest(List<String> numbers) {
        this.phoneNumbers = numbers;
    }

    public BlackListRequest() {
    }
}