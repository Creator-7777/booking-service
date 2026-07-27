package com.alena.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSheetDto {

    private Long id;

    private String name;

    private String phone;

    private String service;

    private LocalDate date;

    private String time;

    private LocalDateTime createdAt;

    private String status;

}