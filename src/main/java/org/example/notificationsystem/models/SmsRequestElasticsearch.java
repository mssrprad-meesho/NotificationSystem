package org.example.notificationsystem.models;

import lombok.*;
import org.example.notificationsystem.utils.NotificationSystemUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    private Date createdAt;

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