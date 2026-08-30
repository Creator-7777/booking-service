package com.alena.booking.service;

import com.alena.booking.dto.Sms4FreeStatusRequest;
import com.alena.booking.entity.SmsMessage;
import com.alena.booking.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SmsDeliveryService {

    private final SmsBlacklistService smsBlacklistService;
    private static final Logger log = LoggerFactory.getLogger(SmsDeliveryService.class);

    private final SmsMessageRepository repository;
    private final TelegramNotificationService telegram;

    public void process(
            Integer status,
            String msgId,
            String phone) {

        log.info("SMS4Free CALLBACK: status={}, msgId={}, phone={}", status, msgId, phone);

        // ==========================================
        // Find SMS record
        // ==========================================
        SmsMessage sms = repository.findByUserMsgId(msgId).orElse(null);
        if (sms != null) {
            sms.setProviderMsgId(msgId);
            sms.setStatus(status);
            String statusMessage = switch (status) {
                case 1 -> "SMS_DELIVERED";
                case 5 -> "SMS_NOT_DELIVERED";
                case 6 -> "KOSHER_NUMBER";
                case 7 -> "NUMBER_IS_BLOCKED";
                default -> "UNKNOWN_STATUS";
            };
            sms.setStatusMessage(statusMessage);
            sms.setUpdatedAt(LocalDateTime.now());
            repository.save(sms);
            log.info("SMS MESSAGE UPDATED: phone={}, status={}, message={}", phone, status, statusMessage);
        }

        // ==========================================
        // 1 = SMS DELIVERED
        // ==========================================
        if (status == 1) {
            log.info("SMS DELIVERED: {} ", phone);
            return;
        }

        // ==========================================
        // 5 = SMS NOT DELIVERED
        // ==========================================
        if (status == 5 || status == 7) {
            smsBlacklistService.block(
                    phone,
                    status,
                    msgId,
                    "SMS_NOT_DELIVERED"
            );
            log.info("PHONE BLOCKED: {} because SMS was not delivered. status={} ", phone, status);

            telegram.send(
                    """
                    🚨 SMS NOT DELIVERED
                    📱 Phone: %s
                    ❌ Status: %d
                    🆔 Message ID: %s
                    ⚠️ SMS не дошла до номера.
                    Номер автоматически добавлен в blacklist.
                    """.formatted(
                            phone,
                            status,
                            msgId
                    )
            );
            return;
        }

        // ==========================================
        // 6 = KOSHER NUMBER
        // ==========================================
        if (status == 6) {
            log.info("KOSHER NUMBER: {} " , phone);
            return;
        }

        // ==========================================
        // UNKNOWN
        // ==========================================
        log.warn("UNKNOWN SMS4FREE STATUS: {} for phone {}", status, phone);
    }


    public void processStatus(Sms4FreeStatusRequest request) {
        SmsMessage sms = repository.findByUserMsgId(request.getMsgId()).orElse(null);
        if (sms == null) {
            return;
        }
        sms.setStatus(request.getStatus());
        sms.setUpdatedAt(LocalDateTime.now());
        repository.save(sms);
        String phone = request.getTo();
        if (request.getStatus() == 5) {
            telegram.send(
                    """
                    🚨 SMS NOT DELIVERED
                    📱 Phone: %s
                    ❌ Status: 5
                    ⚠️ SMS не дошла до номера
                    Возможно, номер не существует.
                    """.formatted(phone)
            );
        }
        if (request.getStatus() == 6) {
            telegram.send(
                    """
                    ⚠️ SMS DELIVERY WARNING
                    📱 Phone: %s
                    ⚠️ Status: 6
                    ℹ️ Kosher number
                    """.formatted(phone)
            );
        }
    }
}