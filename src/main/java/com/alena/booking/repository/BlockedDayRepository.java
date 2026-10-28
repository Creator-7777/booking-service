package com.alena.booking.repository;

import com.alena.booking.entity.BlockedDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface BlockedDayRepository
        extends JpaRepository<BlockedDay, Long> {

    boolean existsByBlockedDate(LocalDate date);

    Optional<BlockedDay> findByBlockedDate(LocalDate date);

    void deleteByBlockedDate(LocalDate date);
}