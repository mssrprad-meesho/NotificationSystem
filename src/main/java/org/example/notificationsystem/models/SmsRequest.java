package org.example.notificationsystem.models;

import lombok.Getter;
import lombok.Setter;
import org.example.notificationsystem.utils.NotificationSystemUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "sms_request")
@Getter
@Setter
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = NotificationSystemUtils.getNowAsDateIST();
    }
}