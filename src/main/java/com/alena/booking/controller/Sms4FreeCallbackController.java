package com.alena.booking.controller;

import com.alena.booking.dto.Sms4FreeStatusRequest;
import com.alena.booking.service.SmsDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sms4free")
public class Sms4FreeCallbackController {

    private final SmsDeliveryService smsDeliveryService;

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam Integer status,
            @RequestParam String msgId,
            @RequestParam String to) {

        smsDeliveryService.process(
                status,
                msgId,
                to
        );

        return ResponseEntity.ok().build();
    }


    @PostMapping("/status")
    public ResponseEntity<Void> receiveStatus(@RequestBody Sms4FreeStatusRequest request) {
        smsDeliveryService.processStatus(request);
        return ResponseEntity.ok().build();
    }
}