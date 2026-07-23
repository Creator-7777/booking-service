package com.alena.booking.controller;

import com.alena.booking.service.GoogleSheetSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final GoogleSheetSyncService service;

    @PostMapping("/all")
    public void sync() {
        service.syncAll();
    }

}