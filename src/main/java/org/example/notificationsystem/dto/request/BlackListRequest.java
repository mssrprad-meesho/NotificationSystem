package org.example.notificationsystem.dto.request;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class BlackListRequest {
    @NotNull
    private List<String> phoneNumbers;

    public BlackListRequest(List<String> numbers) {
        this.phoneNumbers = numbers;
    }
    public BlackListRequest() {}
}