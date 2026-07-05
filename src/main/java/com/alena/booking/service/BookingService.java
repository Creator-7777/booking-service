package com.alena.booking.service;

import com.alena.booking.dto.BookingRequest;
import com.alena.booking.entity.Appointment;
import com.alena.booking.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final AppointmentRepository repository;
    //private final TelegramService telegramService;
    private final TelegramServiceGoogleAppScript telegramService;
    private final GoogleSheetService googleSheetService;
    private final CalendarService calendarService;
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public void createBooking(
            BookingRequest request) {

        Appointment appointment = Appointment.builder()
                        .customerName(request.getName())
                        .phone(request.getPhone())
                        .services(request.getService())
                        .appointmentDate(LocalDate.parse(request.getDate()))
                        .appointmentTime(request.getTime())
                        .createdAt(LocalDateTime.now())
                        .build();


        try {
            log.info("Sending to DB...");
            repository.save(appointment);
            log.info("Booking saved to DB");
        } catch (Exception e) {
            log.error("Booking save to DB failed", e);
        }

        try {
            log.info("Sending to Calendar...");
            calendarService.createEvent(appointment);
            log.info("Calendar SUCCESS");
        } catch (Exception e) {
            log.error("Calendar event failed", e);
        }

        try {
            log.info("Sending to Google Sheet...");
            googleSheetService.saveBooking(appointment);
        } catch (Exception e) {
            log.error("Google Sheet save failed", e);
        }



    }

    public List<String> getBookedTimes(
            LocalDate date) {

        return repository
                .findByAppointmentDate(date)
                .stream()
                .map(Appointment::getAppointmentTime)
                .toList();
    }
}