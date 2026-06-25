package com.alena.booking.service;

public interface SmsServiceInterface {

    void sendVerificationCode(
            String phone);

    boolean validateCode(
            String phone,
            String code);
}
