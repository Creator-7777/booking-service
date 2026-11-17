
package com.alena.booking.controller;

import com.alena.booking.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramLinkController {

    private final TelegramLinkService telegramLinkService;

    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> createLink(
            @RequestBody LinkRequest request) {

        if (request == null || request.phone() == null || !request.phone().matches("^05\\d{8}$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid phone number"));
        }

        try {
            String link = telegramLinkService.createLink(request.phone());
            return ResponseEntity.ok(Map.of("link", link, "message", "Telegram link created"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "This phone number is not registered as a verified customer"));
        }
    }

    public record LinkRequest(String phone) {
    }
}
