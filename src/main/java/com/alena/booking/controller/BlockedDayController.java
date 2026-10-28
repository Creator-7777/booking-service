package com.alena.booking.controller;

import com.alena.booking.entity.BlockedDay;
import com.alena.booking.repository.BlockedDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/blocked-days")
@RequiredArgsConstructor
public class BlockedDayController {

    private final BlockedDayRepository repository;

    public record BlockDayRequest(
            LocalDate date,
            String reason
    ) {}

    @PostMapping
    public ResponseEntity<?> blockDay(
            @RequestBody BlockDayRequest request) {

        if (request.date() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Date is required"
            ));
        }

        if (request.date().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Past dates cannot be blocked"
            ));
        }

        if (repository.existsByBlockedDate(request.date())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "Day is already blocked"
            ));
        }

        repository.save(new BlockedDay(request.date(), request.reason()));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "date", request.date().toString(),
                "message", "Day blocked successfully"
        ));
    }

    @Transactional
    @DeleteMapping("/{date}")
    public ResponseEntity<Map<String, String>> unblockDay(
            @PathVariable LocalDate date) {

        if (!repository.existsByBlockedDate(date)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteByBlockedDate(date);

        return ResponseEntity.ok(Map.of(
                "message", "Day successfully unblocked",
                "date", date.toString()
        ));
    }
}