package com.alena.booking.dto;

import lombok.Data;

@Data
public class Sms4FreeStatusRequest {

    private Integer status;

    private String msgId;

    private String to;
}