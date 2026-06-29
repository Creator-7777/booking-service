package com.alena.booking.controller;


import com.alena.booking.dto.SmsRequest;
import com.alena.booking.service.Sms4FreeService;
import com.alena.booking.service.SmsCodeStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
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
        log.info("PHONE={} CODE={}", request.phone(), code);
        sms4FreeService.sendSms(request.phone(), "Ваш код подтверждения: " + code);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public Map<String, Boolean> validate(@RequestBody SmsRequest request) {
        return Map.of("valid", storage.validate(request.phone(), request.code()));
    }


}