package com.alena.booking.service;

import com.alena.booking.entity.Appointment;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.bot-token}")
    private String token;

    @Value("${telegram.chat-id}")
    private String chatId;

    private final WebClient webClient = WebClient.create();

    public void sendBooking(Appointment appointment) {

        log.info("TOKEN = {}", token);
        log.info("CHAT_ID = {}", chatId);
        log.info("URL = https://api.telegram.org/bot{}/sendMessage", token);

        String message =
                """
                📥 Новая запись

                👤 %s
                📞 %s
                💅 %s
                📅 %s
                ⏰ %s
                """.formatted(
                    appointment.getCustomerName(),
                    appointment.getPhone(),
                    appointment.getServices(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime());

        try {
            String response = webClient.post().uri("https://api.telegram.org/bot" + token + "/sendMessage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("chat_id", chatId, "text", message))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
            log.info("Telegram response={}", response);

        } catch (Exception ex) {
            log.error("Telegram failed", ex);
            if (ex instanceof WebClientResponseException e) {
                log.error("Telegram body={}", e.getResponseBodyAsString());
            }
        }

    }
}
