package com.alena.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleSheetSyncScheduler {

    private final GoogleSheetSyncService syncService;

    @Scheduled(fixedDelay = 3600000) // every hour
    public void synchronize() {
        syncService.syncAll();

    }

}