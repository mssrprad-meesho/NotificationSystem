package org.example.notificationsystem.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@Document(indexName = "sms_request", shards = 1, replicas = 0, refreshInterval = "-1")
public class SmsRequestElasticsearch {
    @Id
    private String id;

    @Field(type = FieldType.Text, name = "sms_request_id")
    private String smsRequestId;

    @Field(type = FieldType.Text, name = "phone_number")
    private String phoneNumber;

    @Field(type = FieldType.Text, name = "message")
    private String message;

    private Long createdAt;

    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        updatedAt = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}