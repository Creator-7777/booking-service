package com.alena.booking.repository;

import com.alena.booking.entity.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsMessageRepository
        extends JpaRepository<SmsMessage, Long> {

    Optional<SmsMessage> findByUserMsgId(String userMsgId);

    Optional<SmsMessage> findTopByPhoneOrderByCreatedAtDesc(String phone);
}