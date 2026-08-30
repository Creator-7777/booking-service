package com.alena.booking.controller;

import com.alena.booking.dto.SmsStatusCallback;
import com.alena.booking.repository.SmsMessageRepository;
import com.alena.booking.service.BookingService;
import com.alena.booking.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsStatusController {

    private final SmsMessageRepository smsMessageRepository;
    private final TelegramNotificationService telegramService;
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @PostMapping("/status")
    public ResponseEntity<Void> receiveStatus(
            @RequestBody SmsStatusCallback callback) {

        log.info("=== SMS4FREE CALLBACK ===");
        log.info("status = " + callback.getStatus());
        log.info("msgId  = " + callback.getMsgId());
        log.info("to     = " + callback.getTo());

        smsMessageRepository.findByUserMsgId(callback.getMsgId()).ifPresent(sms -> {
                    sms.setProviderMsgId(callback.getMsgId());
                    sms.setStatus(callback.getStatus());
                    smsMessageRepository.save(sms);
                    if (callback.getStatus() == 5 || callback.getStatus() == 7) {
                        telegramService.send(
                                "⚠️ SMS НЕ ДОСТАВЛЕН\n\n"
                                        + "📱 Номер: " + callback.getTo() + "\n"
                                        + "📨 Message ID: " + callback.getMsgId()
                        );
                    }

                    if (callback.getStatus() == 6) {
                        telegramService.send(
                                "⚠️ SMS НОМЕР כשר\n\n"
                                        + "📱 Номер: " + callback.getTo() + "\n"
                                        + "📨 Message ID: " + callback.getMsgId()
                        );
                    }
                });

        return ResponseEntity.ok().build();
    }
}