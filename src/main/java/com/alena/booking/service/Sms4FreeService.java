package com.alena.booking.service;

import com.alena.booking.dto.Sms4FreeStatuses;
import com.alena.booking.entity.SmsMessage;
import com.alena.booking.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Sms4FreeService {

    @Value("${sms.api-key}")
    private String apiKey;

    @Value("${sms.user}")
    private String smsUser;

    @Value("${sms.password}")
    private String smsPassword;

    @Value("${sms.sender}")
    private String smsSender;

    //private final RestTemplate restTemplate = new RestTemplate();

    private final WebClient webClient = WebClient.create();

    private final SmsBlacklistService smsBlacklistService;
    private final SmsMessageRepository smsMessageRepository;
    private static final Logger log = LoggerFactory.getLogger(Sms4FreeService.class);

    private static final Set<Integer> BLOCKING_STATUSES = Set.of(
            Sms4FreeStatuses.NOT_DELIVERED,
            Sms4FreeStatuses.NUMBER_IS_BLOCKED
    );

    private boolean isBlockedBySmsHistory(String phone) {
        return smsMessageRepository.existsByPhoneAndStatusIn(
                phone,
                BLOCKING_STATUSES
        );
    }

    public void sendSms(String phone, String message) {


        // ==========================================
        // BLACKLIST CHECK
        // ==========================================
        if (smsBlacklistService.isBlocked(phone) ) {
            log.info("SMS BLOCKED - phone is in blacklist: " + phone);
            throw new IllegalStateException("This phone number is blocked");
            //return;
        }

        // ==========================================
        // SMS4FREE HISTORY CHECK
        // ==========================================
        if (isBlockedBySmsHistory(phone)) {
            log.warn("SMS BLOCKED - phone has previous SMS4Free status 5 or 7: {}", phone);
            throw new IllegalStateException("SMS cannot be sent to this phone number");
        }



        // ==========================================
        // Unique ID for SMS4Free
        // ==========================================
        String userMsgId = UUID.randomUUID().toString();

        //ResponseEntity<String> response = restTemplate.postForEntity("https://api.sms4free.co.il/ApiSMS/v2/SendSMS", payload, String.class);

        SmsMessage sms = SmsMessage.builder()
                .phone(phone)
                .message(message)
                .userMsgId(userMsgId)
                .build();

        Map<String, String> payload = new HashMap<>();
        payload.put("key", apiKey);
        payload.put("user", smsUser);
        payload.put("pass", smsPassword);
        payload.put("sender", smsSender);
        payload.put("recipient", phone);
        payload.put("msg", message);
        // NEW
        payload.put("usermsgid", userMsgId);
        // NEW
        payload.put("returnUrl", "https://booking-service-production-4dd0.up.railway.app/api/sms4free/callback");

        // NEW //
        payload.put("status", String.valueOf(sms.getStatus()));
        // NEW //
        payload.put("statusMessage", sms.getStatusMessage());
        // NEW //
        payload.put("providerMessageId", sms.getProviderMsgId());

        smsMessageRepository.save(sms);

        try {

            log.info("=== SMS REQUEST ===");
            log.info("Recipient = " + phone);
            log.info("Sender = " + smsSender);
            log.info("User = " + smsUser);

            String response =
                    webClient.post()
                            .uri("https://api.sms4free.co.il/ApiSMS/v2/SendSMS", payload, String.class)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            log.info("SMS RESPONSE = " + response);

        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
