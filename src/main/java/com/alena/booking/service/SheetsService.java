package com.alena.booking.service;

import com.alena.booking.dto.BookingSheetDto;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.alena.booking.entity.Appointment;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

@Slf4j
@Service
public class SheetsService {

    @Value("${google.sheet.id}")
    private String spreadsheetId;

    @Value("${google.credentials-json}")
    private String credentialsJson;

    @Value("${google.bookings-sheet-id}")
    private Integer bookingsSheetId;

    @Value("${google.booking-sheet-old-id}")
    private Integer bookingOldSheetId;

    private Sheets sheets;

    private final String bookingsRange = "Bookings_New!A:H";

    private final String bookingsOldTabRange = "Bookings!A:F";

    @PostConstruct
    public void init() throws Exception {

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        sheets = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Booking Service")
                        .build();

        log.info("Google Sheets initialized.");

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
                    appointment.getCreatedAt().toString(),
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
                            .createdAt(LocalDateTime.parse(row.get(6).toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .status(row.get(7).toString())
                            .build();
            result.add(dto);
        }
        return result;
    }

    public void deleteBooking(Appointment appointment) throws IOException {

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, bookingsRange).execute();

        List<List<Object>> rows = response.getValues();

        if (rows == null || rows.size() <= 1) {
            return;
        }

        for (int i = 1; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            String id = row.get(0).toString();
            String phone = row.get(3).toString();
            String date  = row.get(5).toString();
            String time  = row.get(6).toString();

            if (id.equals(appointment.getId().toString())){

                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest().setRange(
                                        new DimensionRange()
                                                .setSheetId(bookingsSheetId)
                                                .setDimension("ROWS")
                                                .setStartIndex(i)
                                                .setEndIndex(i + 1));

                BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest()
                        .setRequests(List.of(new Request().setDeleteDimension(deleteRequest)));

                sheets.spreadsheets().batchUpdate(spreadsheetId, request).execute();
                log.info("Booking removed from Bookings_New Google Sheets");
                return;
            }
        }

        log.warn("Booking not found in Bookings_New Google Sheets");
    }

    public void deleteOldTabBooking(Appointment appointment) throws IOException {

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, bookingsOldTabRange).execute();

        List<List<Object>> rows = response.getValues();

        if (rows == null || rows.size() <= 1) {
            return;
        }

        for (int i = 1; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            String phone = row.get(3).toString();
            String date  = row.get(5).toString();
            String time  = row.get(6).toString();

            if (phone.equals(appointment.getPhone())
                    && date.equals(appointment.getAppointmentDate().toString())
                    && time.equals(appointment.getAppointmentTime())) {

                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest().setRange(
                        new DimensionRange()
                                .setSheetId(bookingOldSheetId)
                                .setDimension("ROWS")
                                .setStartIndex(i)
                                .setEndIndex(i + 1));

                BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest()
                        .setRequests(List.of(new Request().setDeleteDimension(deleteRequest)));

                sheets.spreadsheets().batchUpdate(spreadsheetId, request).execute();
                log.info("Booking removed from Bookings Google Sheets");
                return;
            }
        }

        log.warn("Booking not found in Bookings Google Sheets");
    }

    // Tab Bookings_New
    private List<String> getBookedTimesNew(LocalDate date) throws IOException {

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, "Bookings_New!A2:H").execute();

        List<List<Object>> rows = response.getValues();

        List<String> bookedTimes = new ArrayList<>();

        if (rows == null) {
            return bookedTimes;
        }

        for (List<Object> row : rows) {
            if (row.size() < 6) {
                continue;
            }
            LocalDate bookingDate = LocalDate.parse(row.get(4).toString());
            if (bookingDate.equals(date)) {
                bookedTimes.add(row.get(5).toString());
            }
        }
        return bookedTimes;
    }

    // Tab Bookings
    private List<String> getBookedTimesOld(LocalDate date) throws IOException {

        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, "Bookings!A2:F").execute();

        List<List<Object>> rows = response.getValues();

        List<String> bookedTimes = new ArrayList<>();

        if (rows == null) {
            return bookedTimes;
        }

        for (List<Object> row : rows) {
            if (row.size() < 6) {
                continue;
            }
            LocalDate bookingDate = LocalDate.parse(row.get(3).toString());
            if (bookingDate.equals(date)) {
                bookedTimes.add(row.get(4).toString());
            }
        }
        return bookedTimes;
    }

    public List<String> getBookedTimes(LocalDate date) throws IOException {

        Set<String> result = new HashSet<>();
        result.addAll(getBookedTimesOld(date));
        result.addAll(getBookedTimesNew(date));
        return new ArrayList<>(result);
    }
}
