package com.alena.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_blacklist")
@Getter
@Setter
@NoArgsConstructor
public class SmsBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phone;

    private String reason;

    @Column(name = "sms4free_status")
    private Integer sms4freeStatus;

    @Column(name = "msg_id")
    private String msgId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active = true;
}