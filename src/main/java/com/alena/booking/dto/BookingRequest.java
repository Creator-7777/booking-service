package com.alena.booking.dto;

import lombok.Data;

@Data
public class BookingRequest {

    private String name;

    private String phone;

    private String service;

    private String date;

    private String time;
}