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
public class TelegramServiceGoogleAppScript {

    @Value("${google.apps-script-url}")
    private String appsScriptUrl;

    private final WebClient webClient =
            WebClient.create();

    public void sendBooking(
            Appointment appointment) {

        Map<String, Object> payload = Map.of(
                "name", appointment.getCustomerName(),
                "phone", appointment.getPhone(),
                "service", appointment.getServices(),
                "date", appointment.getAppointmentDate().toString(),
                "time", appointment.getAppointmentTime()
        );

        String response =
                webClient.post()
                        .uri(appsScriptUrl)
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

        System.out.println(response);
    }
}
