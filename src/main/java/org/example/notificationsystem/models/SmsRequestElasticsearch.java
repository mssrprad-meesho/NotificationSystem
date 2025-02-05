package org.example.notificationsystem.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "sms_request", shards = 1, replicas = 0, refreshInterval = "-1")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsRequestElasticsearch {
    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("sms_request_id")
    @Field(type = FieldType.Keyword, name = "sms_request_id")
    private String smsRequestId;

    @JsonProperty("phone_number")
    @Field(type = FieldType.Keyword, name = "phone_number")
    private String phoneNumber;

    @JsonProperty("message")
    @Field(type = FieldType.Text, name = "message")
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING,
    pattern = "yyyyMMdd'T'HHmmss.SSSX",
            timezone = "UTC")
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyyMMdd'T'HHmmss.SSSX",
            timezone = "UTC")
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = NotificationSystemUtils.getNowAsDateIST();
        updatedAt = NotificationSystemUtils.getNowAsDateIST();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = NotificationSystemUtils.getNowAsDateIST();
    }
}