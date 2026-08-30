package com.alena.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String message;

    /**
     * ID, который мы сами передаем SMS4Free
     * через usermsgid.
     */
    @Column(name = "user_msg_id", nullable = false, unique = true)
    private String userMsgId;

    /**
     * ID сообщения, который вернул SMS4Free
     * в callback.
     */
    @Column(name = "provider_msg_id")
    private String providerMsgId;

    /**
     * SMS4Free:
     *
     * 1 = доставлено
     * 5 = не доставлено
     * 6 = כשר
     */
    private Integer status;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}