package com.alena.booking.dto;

import lombok.Data;

@Data
public class SmsStatusCallback {

    private Integer status;

    private String msgId;

    private String to;
}