package com.alena.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.alena.booking.entity.Appointment;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.time.Instant;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Service
public class SheetsService {

    @Value("${google.sheet.id}")
    private String spreadsheetId;

    @Value("${google.credentials-json}")
    private String credentialsJson;

//    @Value("${google.sheet.bookings.tab}")
//    private String tab;

    private Sheets sheets;

    @PostConstruct
    public void init() throws Exception {

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        sheets = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Booking Service")
                        .build();

        log.info("Google Sheets initialized.");

        testConnection();
    }

    public void appendBooking(Appointment appointment) {

        try {
            List<Object> row = List.of(
                    appointment.getId(),                 // we'll use this later
                    appointment.getCustomerName(),
                    appointment.getPhone(),
                    appointment.getServices(),
                    appointment.getAppointmentDate().toString(),
                    appointment.getAppointmentTime(),
                    Instant.now().toString(),
                    "ACTIVE"
            );

            ValueRange body = new ValueRange().setValues(List.of(row));

            sheets.spreadsheets().values().append(spreadsheetId, "Bookings_New!A:H", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            log.info("Booking appended to Google Sheet");

        } catch (Exception ex) {
            log.error("Cannot append booking", ex);
        }
    }

    public void testConnection() {

        try {
            var response = sheets.spreadsheets()
                            .values()
                            .get(spreadsheetId, "Bookings!A1")
                            .execute();

            log.info("Cell A1 = {}", response.getValues());
        } catch (Exception ex) {
            log.error("Cannot read Google Sheet", ex);
        }
    }
}
