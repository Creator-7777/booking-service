package com.alena.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistoryDto {

    private Long id;

    private String service;

    private LocalDate date;

    private String time;

    private BookingStatus status;

}