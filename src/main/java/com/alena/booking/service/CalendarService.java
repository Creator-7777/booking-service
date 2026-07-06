package com.alena.booking.service;

import com.alena.booking.entity.Appointment;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Service
public class CalendarService {

    @Value("${google.calendar-id}")
    private String calendarId;

    @Value("${google.credentials-json}")
    private String credentialsJson;

    private Calendar calendar;

/*    @PostConstruct
    public void init() throws Exception {

        InputStream credentialsStream =
                new ClassPathResource("google/elena-booking.json")
                        .getInputStream();

        GoogleCredentials credentials =
                GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        calendar =
                new Calendar.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Booking Service")
                        .build();

        log.info("Google Calendar initialized.");
    }*/

    @PostConstruct
    public void init() throws Exception {

        GoogleCredentials credentials =
                GoogleCredentials.fromStream(
                                new ByteArrayInputStream(
                                        credentialsJson.getBytes(StandardCharsets.UTF_8)))
                        .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        ServiceAccountCredentials sa = (ServiceAccountCredentials) credentials;

        log.info("Email: {}", sa.getClientEmail());
        log.info("KeyId: {}", sa.getPrivateKeyId());

        log.info("Credentials JSON: {}", credentialsJson);

        credentials = sa.createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        calendar =
                new Calendar.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Booking Service")
                        .build();

        log.info("Google Calendar initialized.");
    }

    public void createEvent(Appointment booking) {

        try {

            LocalDate date = booking.getAppointmentDate();
//            String[] parts = booking.getAppointmentDate().split("-");
            String timeSlot = booking.getAppointmentTime();
            //String[] parts = booking.getAppointmentTime().split("-");
            String[] parts = booking.getAppointmentTime().split("[\\-–]");  // (длинное тире – )
            LocalTime startTime = LocalTime.parse(parts[0].trim());
            LocalTime endTime = LocalTime.parse(parts[1].trim());

            LocalDateTime start = LocalDateTime.of(date, startTime);
            LocalDateTime end = LocalDateTime.of(date, endTime);
            Event event = new Event();
            event.setSummary(booking.getServices());
            event.setDescription(
                    "Client: " + booking.getCustomerName() +
                            "\nPhone: " + booking.getPhone()
            );
            ZoneId zone = ZoneId.of("Asia/Jerusalem");
            EventDateTime startEvent =
                    new EventDateTime()
                            .setDateTime(
                                    new com.google.api.client.util.DateTime(
                                            Date.from(start.atZone(zone).toInstant())))
                            .setTimeZone(zone.getId());

            EventDateTime endEvent =
                    new EventDateTime()
                            .setDateTime(
                                    new com.google.api.client.util.DateTime(
                                            Date.from(end.atZone(zone).toInstant())))
                            .setTimeZone(zone.getId());

            event.setStart(startEvent);
            event.setEnd(endEvent);
            calendar.events()
                    .insert(calendarId, event)
                    .execute();

            log.info("Calendar event created.");
        } catch (Exception ex) {
            log.error("Cannot create calendar event", ex);
        }
    }
}