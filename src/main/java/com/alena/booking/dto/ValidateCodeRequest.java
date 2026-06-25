package com.alena.booking.dto;

import lombok.Data;

@Data
public class ValidateCodeRequest {

    private String phone;
    private String code;
}