package org.example.notificationsystem.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "phone_number")
@Getter
@Setter
public class PhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min = 4, max = 17, message = "{validation.phone.number.size}")
    private String phoneNumber;

    private Boolean blackListed = Boolean.FALSE;

    @JsonBackReference
    @OneToMany(mappedBy = "number", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SmsRequest> smsRequests;
}