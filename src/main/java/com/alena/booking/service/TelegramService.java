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

import java.util.HashMap;
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

    private void send(String targetChatId, String text) {

        Map<String, Object> body = new HashMap<>();
               body.put("chat_id", targetChatId);
               body.put( "text", text );
               body.put("parse_mode", "HTML");

        webClient.post()
                .uri("https://api.telegram.org/bot" + token + "/sendMessage")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();


    }

    public void sendBooking(Appointment appointment) {

        //log.info("TOKEN = {}", token);
        //log.info("CHAT_ID = {}", chatId);
        //log.info("URL = https://api.telegram.org/bot{}/sendMessage", token);

        String message = """
                📥 <b> Новая запись: </b>
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
            send(chatId, message);
            log.info(" Booking notification sent to Telegram group ");
        } catch (Exception ex) {
            log.error("Telegram failed to send booking notification to the group ", ex);
        }

    }

    public void sendCancelNotification(Appointment appointment) {

        String message = """
            ❌ <b> отмена записи: </b>
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
            send(chatId, message);
            log.info("Cancellation notification sent to Telegram group");
        } catch (Exception ex) {
            log.error("Telegram failed to send cancel notification to the group", ex);
        }
    }

    /**
     * Send a private message to a customer.
     */
    public void sendCustomerMessage(Long customerChatId, String message) {

        if (customerChatId == null) {
            log.debug("Customer Telegram chat ID is missing");
            return;
        }

        try {
            send(customerChatId.toString(), message);
            log.info("Private Telegram notification sent to the client");
        } catch (Exception ex) {
            log.error("Failed to send private Telegram notification to the client", ex);
        }
    }


}
