package com.alena.booking.repository;

import com.alena.booking.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

    List<Appointment> findByPhoneOrderByAppointmentDateDescAppointmentTimeDesc(String phone);

    boolean existsByPhoneAndAppointmentDateAndAppointmentTime(String phone, LocalDate appointmentDate, String appointmentTime);

    boolean existsByAppointmentDateAndAppointmentTime(LocalDate appointmentDate, String appointmentTime);

    Optional<Appointment> findByPhoneAndAppointmentDateAndAppointmentTime(String phone, LocalDate appointmentDate, String appointmentTime

    );

}
