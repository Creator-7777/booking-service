package com.alena.booking.service;

import com.alena.booking.config.TimeConfig;
import com.alena.booking.dto.BookingRequest;
import com.alena.booking.dto.CancelBookingRequest;
import com.alena.booking.dto.CancelOldBookingRequest;
import com.alena.booking.entity.Appointment;
import com.alena.booking.entity.VerifiedCustomer;
import com.alena.booking.exception.BookingAlreadyExistsException;
import com.alena.booking.repository.AppointmentRepository;
import com.alena.booking.repository.VerifiedCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final AppointmentRepository repository;
    private final VerifiedCustomerRepository verifiedCustomerRepository;
    private final TelegramService telegramService;
    //private final TelegramServiceGoogleAppScript telegramService;
    //private final GoogleSheetService googleSheetService;
    private final SheetsService sheetsService;
    private final CalendarService calendarService;
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public void createBooking(BookingRequest request) throws IOException {

        Appointment appointment = Appointment.builder()
                        .customerName(request.getName())
                        .phone(request.getPhone())
                        .services(request.getService())
                        .appointmentDate(LocalDate.parse(request.getDate()))
                        .appointmentTime(request.getTime())
                        .createdAt(LocalDateTime.now(TimeConfig.ZONE))
                        //.createdAt(LocalDateTime.now())
                        .build();

        LocalTime start =
                LocalTime.parse(
                        appointment.getAppointmentTime().split("[\\-–]")[0].trim());

        LocalTime end =
                LocalTime.parse(
                        appointment.getAppointmentTime().split("[\\-–]")[1].trim());

        // Verify free time slots in Calendar
        boolean available = calendarService.isTimeSlotAvailable(
                        LocalDate.parse(request.getDate()),
                        //appointment.getAppointmentDate(),
                        start,
                        end);

        // Verify free time slots in Google Sheet
        //List<String> booked = googleSheetService.getBookedTimes(appointment.getAppointmentDate());
        List<String> booked = sheetsService.getBookedTimes(appointment.getAppointmentDate());

        if (!available || booked.contains(appointment.getAppointmentTime())) {
            throw new BookingAlreadyExistsException(
                    "Selected time slot is already booked - Выбранное время уже забронировано.");
        }


        try {
            log.info("Sending to DB...");
            repository.save(appointment);
            VerifiedCustomer customer = verifiedCustomerRepository.findByPhone(appointment.getPhone()).orElse(null);
            if (customer != null && (customer.getName() == null || customer.getName().isBlank())) {
                customer.setName(appointment.getCustomerName());
                verifiedCustomerRepository.save(customer);
            }
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

       /* try {
            log.info("Sending to Google Sheet...");
            googleSheetService.saveBooking(appointment);
        } catch (Exception e) {
            log.error("Google Sheet save failed", e);
        }*/

        try {
            log.info("Sending to New Google Sheet...");
            sheetsService.appendBooking(appointment);
        } catch (Exception e) {
            log.error("New Sheet save failed", e);
        }

        try {
            log.info("Sending Notification to Telegram...");
            telegramService.sendBooking(appointment);
        } catch (Exception e) {
            log.error("Notification to Telegram failed", e);
        }

    }

    @Transactional
    public void cancel(CancelBookingRequest request) throws IOException {

        Appointment appointment = repository.findById(request.id()).orElseThrow(() -> new RuntimeException("Booking not found"));

        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Past bookings cannot be cancelled");
        }

        calendarService.deleteEvent(appointment);
        sheetsService.deleteBooking(appointment);
        telegramService.sendCancelNotification(appointment);
        repository.delete(appointment);
    }



    @Transactional
    public void cancelOld(CancelOldBookingRequest request) throws IOException {

        Appointment appointment = repository.findByPhoneAndAppointmentDateAndAppointmentTime(request.phone(), LocalDate.parse(request.date()), request.time()).orElseThrow();

        if(appointment.getAppointmentDate().isBefore(LocalDate.now())){
            throw new RuntimeException("Past bookings cannot be cancelled");
        }
        else{
            sheetsService.deleteOldTabBooking(appointment);
        }
    }
}