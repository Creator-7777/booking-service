package com.alena.booking.service;

import com.alena.booking.dto.VerifiedCustomerRequest;
import com.alena.booking.entity.Appointment;
import com.alena.booking.entity.VerifiedCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public List<String> getBookedTimes(LocalDate date) {

        String url = appsScriptUrl + "?action=bookedTimes&date=" + date;

        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public void saveVerifiedCustomer(String name , String phone) {

        Map<String, Object> payload = Map.of(
                "name", name,
                "phone", phone,
                "date", Instant.now().toString()

        );
        try {
            restTemplate.postForEntity(
                    appsScriptUrl,
                    payload,
                    String.class);

            log.info("Google Sheet - Verified Customer has been saved to Verified Phones");

        } catch (Exception e) {
            log.error("Google Sheet - Verified Phones insert FAILED", e);
        }
    }
}
