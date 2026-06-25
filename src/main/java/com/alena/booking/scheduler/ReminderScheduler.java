package com.alena.booking.scheduler;

import com.alena.booking.entity.Appointment;
import com.alena.booking.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AppointmentRepository repository;

    @Scheduled(cron = "0 0 10 * * *")
    public void sendReminders() {

        LocalDate tomorrow =
                LocalDate.now().plusDays(1);

        List<Appointment> appointments =
                repository.findByAppointmentDate(
                        tomorrow);

        appointments.forEach(a -> {

            // SMS reminder

        });
    }
}
