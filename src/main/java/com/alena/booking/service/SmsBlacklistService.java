package com.alena.booking.service;

import com.alena.booking.entity.SmsBlacklist;
import com.alena.booking.repository.SmsBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SmsBlacklistService {

    private final SmsBlacklistRepository repository;

    public boolean isBlocked(String phone) {
        return repository.existsByPhoneAndActiveTrue(phone);
    }

    @Transactional
    public void block(
            String phone,
            Integer status,
            String msgId,
            String reason) {

        SmsBlacklist entry = repository.findByPhone(phone).orElseGet(SmsBlacklist::new);

        entry.setPhone(phone);
        entry.setSms4freeStatus(status);
        entry.setMsgId(msgId);
        entry.setReason(reason);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setActive(true);

        repository.save(entry);
    }

    @Transactional
    public void unblock(String phone) {
        repository.findByPhone(phone).ifPresent(entry -> {
                    entry.setActive(false);
                    repository.save(entry);
                });
    }
}