package com.alena.booking.repository;

import com.alena.booking.entity.SmsBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsBlacklistRepository extends JpaRepository<SmsBlacklist, Long> {

    boolean existsByPhoneAndActiveTrue(String phone);

    Optional<SmsBlacklist> findByPhone(String phone);
}