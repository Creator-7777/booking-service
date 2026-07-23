package com.alena.booking.controller;

import com.alena.booking.service.GoogleSheetSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSyncController {

    private final GoogleSheetSyncService syncService;

    @PostMapping("/rebuild-db")
    public ResponseEntity<String> rebuildDatabase(@RequestHeader("X-CONFIRM") String confirm) {

        if (!"YES".equals(confirm)) {
            return ResponseEntity.badRequest()
                    .body("Confirmation header missing.");
        }
        syncService.rebuildDatabase();
        return ResponseEntity.ok("Database successfully rebuilt from Google Sheet.");
    }

}