package com.alena.booking.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final WebClient webClient;

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    public void send(String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", message);

        webClient.post()
                .uri("https://api.telegram.org/bot" + botToken + "/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("Telegram response: " + response),
                        error -> log.error("Telegram error: " + error.getMessage())
                );
    }
}