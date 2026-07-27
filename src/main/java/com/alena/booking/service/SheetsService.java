package com.alena.booking.service;

import com.alena.booking.dto.BookingSheetDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.alena.booking.entity.Appointment;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private String bookingsRange = "Bookings_New!A:H";

    @PostConstruct
    public void init() throws Exception {

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        sheets = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Booking Service")
                        .build();

        log.info("Google Sheets initialized.");

        //testConnection();
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
                    appointment.getCreatedAt(),
                    //Instant.now().toString(),
                    "ACTIVE"
            );

            ValueRange body = new ValueRange().setValues(List.of(row));

            sheets.spreadsheets().values().append(spreadsheetId, bookingsRange, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            log.info("Booking appended to Google Sheet");

        } catch (Exception ex) {
            log.error("Cannot append booking", ex);
        }
    }

    public List<BookingSheetDto> loadBookings() throws IOException {

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, bookingsRange).execute();
        List<List<Object>> rows = response.getValues();
        List<BookingSheetDto> result = new ArrayList<>();
        if (rows == null || rows.size() <= 1) {
            return result;
        }
        for (int i = 1; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            BookingSheetDto dto =
                    BookingSheetDto.builder()
                            .id(Long.parseLong(row.get(0).toString()))
                            .name(row.get(1).toString())
                            .phone(row.get(2).toString())
                            .service(row.get(3).toString())
                            .date(LocalDate.parse(row.get(4).toString()))
                            .time(row.get(5).toString())
                            .createdAt(LocalDate.parse(row.get(6).toString()))
                            .status(row.get(7).toString())
                            .build();
            result.add(dto);
        }
        return result;
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
