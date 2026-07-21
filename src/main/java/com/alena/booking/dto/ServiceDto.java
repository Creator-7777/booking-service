package com.alena.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceDto {

    private String id;

    private String name;

    private Integer price;
}