
package com.alena.booking.controller;

import com.alena.booking.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramLinkService telegramLinkService;

    @Value("${telegram.webhook-secret}")
    private String webhookSecret;

    /**
     * Telegram sends updates to this endpoint.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveUpdate(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String receivedSecret,
            @RequestBody TelegramUpdate update) {

        // Verify that request really comes from our configured webhook.
        if (!isValidSecret(receivedSecret)) {
            log.warn("Rejected Telegram webhook: invalid secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (update == null || update.message() == null || update.message().text() == null || update.message().chat() == null) {
            return ResponseEntity.ok().build();
        }

        String text = update.message().text().trim();
        Long chatId = update.message().chat().id();

        // Only handle /start TOKEN.
        if (!text.startsWith("/start")) {
            return ResponseEntity.ok().build();
        }

        String[] parts = text.split("\\s+", 2);

        if (parts.length != 2 || parts[1].isBlank()) {
            log.info("Telegram /start received without linking token");
            return ResponseEntity.ok().build();
        }

        String token = parts[1].trim();
        boolean linked = telegramLinkService.completeLink(token, chatId);

        if (linked) {
            log.info("Telegram account successfully linked");
        } else {
            log.info("Telegram linking attempt failed");
        }

        return ResponseEntity.ok().build();
    }

    private boolean isValidSecret(String receivedSecret) {

        if (receivedSecret == null || webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                receivedSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Minimal Telegram Update DTO.
     */
    public record TelegramUpdate(TelegramMessage message) {}

    public record TelegramMessage(TelegramChat chat, String text) {}

    public record TelegramChat(Long id) {}
}
