package com.alena.booking.controller;


import com.alena.booking.dto.SmsRequest;
import com.alena.booking.service.Sms4FreeService;
import com.alena.booking.service.SmsCodeStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsCodeStorage storage;
    private final Sms4FreeService sms4FreeService;

    @PostMapping("/send-code")
    public ResponseEntity<Void> sendCode(@RequestBody SmsRequest request) {

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));

        storage.save(request.phone(), code);
        System.out.println("PHONE=" + request.phone()+ " CODE=" + code);
        sms4FreeService.sendSms(request.phone(), "Ваш код подтверждения: " + code);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public Map<String, Boolean> validate(@RequestBody SmsRequest request) {
        return Map.of("valid", storage.validate(request.phone(), request.code()));
    }


}