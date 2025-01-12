package org.example.notificationsystem.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_request")
@Getter
@Setter
public class SmsRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_number_id")
    @JsonBackReference
    private PhoneNumber number;

    @Column(name = "phone_number", nullable = false)
    @Size(min = 4, max = 17, message = "{validation.phone.number.size}")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private Integer status;

    private Integer failureCode;

    @Column(columnDefinition = "TEXT")
    private String failureComments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}