package com.alena.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_days")
@Getter
@NoArgsConstructor
public class BlockedDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocked_date", nullable = false, unique = true)
    private LocalDate blockedDate;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public BlockedDay(LocalDate blockedDate, String reason) {
        this.blockedDate = blockedDate;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
}