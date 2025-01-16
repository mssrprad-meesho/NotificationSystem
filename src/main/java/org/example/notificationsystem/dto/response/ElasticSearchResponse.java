package org.example.notificationsystem.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.notificationsystem.models.SmsRequestElasticsearch;

import java.util.List;

@Builder
@Getter
@Setter
public class ElasticSearchResponse {
    private List<SmsRequestElasticsearch> data;
}
