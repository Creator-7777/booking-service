package com.alena.booking.repository;

import com.alena.booking.entity.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface SmsMessageRepository
        extends JpaRepository<SmsMessage, Long> {

    Optional<SmsMessage> findByUserMsgId(String userMsgId);

    Optional<SmsMessage> findTopByPhoneOrderByCreatedAtDesc(String phone);

    // SELECT EXISTS ( SELECT 1 FROM sms_messages WHERE phone = ? AND status IN (5, 7) )
    boolean existsByPhoneAndStatusIn(String phone, Collection<Integer> statuses);
}