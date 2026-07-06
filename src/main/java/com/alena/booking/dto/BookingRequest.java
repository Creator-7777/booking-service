package com.alena.booking.dto;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class BookingRequest {

    private String name;

    private String phone;

    private String service;

    private String date;

    private String time;
}