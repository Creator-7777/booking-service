package com.alena.booking.service;

import com.alena.booking.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleSheetService {

    @Value("${google.apps-script-url}")
    private String appsScriptUrl;

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetService.class);

    private final RestTemplate restTemplate =
            new RestTemplate();

    public void saveBooking(Appointment appointment) {

        Map<String, Object> payload = Map.of(
                "name", appointment.getCustomerName(),
                "phone", appointment.getPhone(),
                "service", appointment.getServices(),
                "date", appointment.getAppointmentDate().toString(),
                "time", appointment.getAppointmentTime()
        );
        try {
            restTemplate.postForEntity(
                    appsScriptUrl,
                    payload,
                    String.class);

            log.info("Google Sheet SUCCESS");

        } catch (Exception e) {
            log.error("Google Sheet FAILED", e);
        }
    }
}
