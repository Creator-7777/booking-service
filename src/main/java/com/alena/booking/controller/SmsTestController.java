package com.alena.booking.controller;

import com.alena.booking.service.Sms4FreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SmsTestController {

    private final Sms4FreeService sms4FreeService;

    @GetMapping("/sms-test")
    public String test() {
        sms4FreeService.sendSms("0542265193", "Spring Boot SMS Test");

        return "OK";
    }
}
