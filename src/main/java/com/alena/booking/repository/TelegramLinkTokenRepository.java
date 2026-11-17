package com.alena.booking.repository;

import com.alena.booking.entity.TelegramLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TelegramLinkTokenRepository
        extends JpaRepository<TelegramLinkToken, Long> {

    Optional<TelegramLinkToken> findByToken(String token);

    long deleteByPhone(String phone);

    long deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
