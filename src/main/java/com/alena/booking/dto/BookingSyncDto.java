package com.alena.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSyncDto {

    private String name;
    private String phone;
    private String service;
    private String date;
    private String time;

}