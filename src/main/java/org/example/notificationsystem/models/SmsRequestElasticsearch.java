package org.example.notificationsystem.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

/**
 * Represents an SMS request entity that maps to the "sms_request" index in ElasticSearch.
 * <ul>
 *   <li><b>id</b>: The unique id (primary key of the document) for the SMS request (auto-generated).</li>
 *   <li><b>smsRequestId</b>: The unique identifier for the SMS request.</li>
 *   <li><b>phoneNumber</b>: The recipient's phone number, which must be between 4 and 17 characters long.</li>
 *   <li><b>message</b>: The text content of the SMS message.</li>
 *   <li><b>createdAt</b>: The timestamp when the SMS request was created.</li>
 *   <li><b>updatedAt</b>: The timestamp when the SMS request was last updated.</li>
 * </ul>
 * <p>
 * Lifecycle callbacks are used to manage timestamps and default values:
 * <ul>
 *   <li>{@link #onCreate()} - Called before the document is persisted. It initializes both the
 *       {@code createdAt} and {@code updatedAt} fields using the current time.</li>
 *   <li>{@link #onUpdate()} - Called before the document is updated. It refreshes the {@code updatedAt} timestamp
 *       to the current time.</li>
 * </ul>
 * <p>
 * <p>
 * This class is annotated with: {@code @Getter}, {@code @Setter}, {@code @NoArgsConstructor}, {@code @AllArgsConstructor}, {@code @Builder})
 *
 * @author Malladi Pradyumna
 */
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