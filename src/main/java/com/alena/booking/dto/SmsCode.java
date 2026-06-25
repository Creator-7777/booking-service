package com.alena.booking.dto;

import java.time.LocalDateTime;

public record SmsCode(String code, LocalDateTime expireAt) {
}
