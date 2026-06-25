package com.alena.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
public class TelegramTestController {

    @Value("${telegram.bot-token}")
    private String token;

    @GetMapping("/telegram-test")
    public String test() {

        try {
            return WebClient.create().get().uri("https://api.telegram.org/bot" + token + "/getMe")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof WebClientResponseException e) {
                return """
                        STATUS:
                        %s
                        BODY:
                        %s
                        """
                        .formatted(e.getStatusCode(), e.getResponseBodyAsString());
            }

            return ex.toString();
        }
    }
}