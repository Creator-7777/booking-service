package com.alena.booking.service;

import com.alena.booking.dto.BookingHistoryDto;
import com.alena.booking.dto.BookingStatus;
import com.alena.booking.entity.Appointment;
import com.alena.booking.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CabinetService {

    private final AppointmentRepository repository;
    public List<BookingHistoryDto> getHistory(String phone){
        return repository
                .findByPhoneOrderByAppointmentDateDescAppointmentTimeDesc(phone)
                .stream()
                .map(a-> BookingHistoryDto.builder()
                                .id(a.getId())
                                .service(a.getServices())
                                .date(a.getAppointmentDate())
                                .time(a.getAppointmentTime())
                                .status(calculateStatus(a))
                                .build()
                ).toList();
    }

    private BookingStatus calculateStatus(Appointment a){
        if(a.getAppointmentDate().isBefore(LocalDate.now()))
            return BookingStatus.COMPLETED;
        return BookingStatus.UPCOMING;
    }
}
