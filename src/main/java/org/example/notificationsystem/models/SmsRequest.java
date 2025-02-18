package org.example.notificationsystem.models;

import lombok.*;
import org.example.notificationsystem.constants.FailureCodeConstants;
import org.example.notificationsystem.constants.StatusConstants;
import org.example.notificationsystem.utils.NotificationSystemUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;


/**
 * Represents an SMS request entity that maps to the "sms_request" table in the MySQL database.
 * <ul>
 *   <li><b>id</b>: The unique id (primary key of the table) for the SMS request row (auto-generated).</li>
 *   <li><b>phoneNumber</b>: The recipient's phone number, which must be between 4 and 17 characters long.</li>
 *   <li><b>message</b>: The text content of the SMS message.</li>
 *   <li><b>status</b>: The current status of the request, represented by an integer.Refer to the StatusConstants.java file for the values it can take.
 *   <li><b>failureCode</b>: Indicates the failure code if an error occurs. Refer to the FailureCodeConstants.java file for the values it can take.
 *   <li><b>failureComments</b>: Additional details or comments regarding any failure that may occur.</li>
 *   <li><b>createdAt</b>: The timestamp when the SMS request was created.</li>
 *   <li><b>updatedAt</b>: The timestamp when the SMS request was last updated.</li>
 * </ul>
 * <p>
 * Lifecycle callbacks are used to manage timestamps and default values:
 * <ul>
 *   <li>{@link #onCreate()} - Called before the entity is persisted. It initializes both the
 *       {@code createdAt} and {@code updatedAt} fields using the current time
 *       and also initializes the {@code status} and {@code failureCode} fields.</li>
 *   <li>{@link #onUpdate()} - Called before the entity is updated. It refreshes the {@code updatedAt} timestamp
 *       to the current time.</li>
 * </ul>
 * <p>
 * <p>
 * This class is annotated with: {@code @Getter}, {@code @Setter}, {@code @NoArgsConstructor}, {@code @AllArgsConstructor}, {@code @Builder})
 *
 * @author Malladi Pradyumna
 */
@Entity
@Table(name = "sms_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    @Size(min = 4, max = 17, message = "{validation.phone.number.size}")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private Integer status;

    private Integer failureCode;

    @Column(columnDefinition = "TEXT")
    private String failureComments;

    private Date createdAt;

    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = NotificationSystemUtils.getNowAsDateIST();
        updatedAt = NotificationSystemUtils.getNowAsDateIST();
        status = StatusConstants.IN_PROGRESS.ordinal();
        failureCode = FailureCodeConstants.IN_PROGRESS.ordinal();
        failureComments = null;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = NotificationSystemUtils.getNowAsDateIST();
    }

    public void setFailureCode(Integer failureCode, String failureComments) {
        this.failureCode = failureCode;
        this.failureComments = failureComments;
    }
}