package com.alena.booking.service;

import com.alena.booking.dto.BookingSyncDto;
import com.alena.booking.dto.VerifiedCustomerSyncDto;
import com.alena.booking.entity.Appointment;
import com.alena.booking.entity.VerifiedCustomer;
import com.alena.booking.repository.AppointmentRepository;
import com.alena.booking.repository.VerifiedCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetSyncService {

    private final GoogleSheetService googleSheetService;

    private final AppointmentRepository appointmentRepository;

    private final VerifiedCustomerRepository verifiedCustomerRepository;

    @Transactional
    public void syncBookings() {

        List<BookingSyncDto> bookings = googleSheetService.loadBookings();
        int inserted = 0;
        for (BookingSyncDto dto : bookings) {

            boolean exists = appointmentRepository.existsByAppointmentDateAndAppointmentTime(
                            LocalDate.parse(dto.getDate()),
                            dto.getTime());

            if (exists) {
                continue;
            }

            Appointment appointment = Appointment.builder()
                            .customerName(dto.getName())
                            .phone(dto.getPhone())
                            .services(dto.getService())
                            .appointmentDate(LocalDate.parse(dto.getDate()))
                            .appointmentTime(dto.getTime())
                            .createdAt(LocalDateTime.now())
                            .build();

            appointmentRepository.save(appointment);

            inserted++;
        }

        log.info("Booking Sync finished. Inserted {}", inserted);
    }

    @Transactional
    public void syncVerifiedCustomers() {

        List<VerifiedCustomerSyncDto> customers = googleSheetService.loadVerifiedPhones();
        int inserted = 0;
        for (VerifiedCustomerSyncDto dto : customers) {
            if (verifiedCustomerRepository.existsByPhone(dto.getPhone())) {
                continue;
            }

            VerifiedCustomer customer = new VerifiedCustomer();
            customer.setPhone(dto.getPhone());
            customer.setName(dto.getName());
            verifiedCustomerRepository.save(customer);
            inserted++;
        }

        log.info("Verified Sync finished. Inserted {}", inserted);
    }

    @Transactional
    public void syncAll() {
        syncVerifiedCustomers();
        syncBookings();
    }

    @Transactional
    public void rebuildDatabase() {
        log.info("========== DATABASE REBUILD STARTED ==========");
        //---------------------------------------------------
        // Delete existing data
        //---------------------------------------------------
        log.info("Removing appointments...");
        appointmentRepository.deleteAllInBatch();

        log.info("Removing verified customers...");
        verifiedCustomerRepository.deleteAllInBatch();

        appointmentRepository.flush();
        verifiedCustomerRepository.flush();
        log.info("Database cleaned.");
        //---------------------------------------------------
        // Restore verified customers
        //---------------------------------------------------
        log.info("Synchronizing verified customers...");
        syncVerifiedCustomers();
        //---------------------------------------------------
        // Restore bookings
        //---------------------------------------------------
        log.info("Synchronizing appointments...");
        syncBookings();
        log.info("========== DATABASE REBUILD FINISHED ==========");
    }
}