package com.alena.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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

    private final RestTemplate restTemplate =
            new RestTemplate();

    public void sendSms(String phone, String message) {

        Map<String, String> payload =
                new HashMap<>();

        payload.put("key", apiKey);
        payload.put("user", smsUser);
        payload.put("pass", smsPassword);
        payload.put("sender", smsSender);
        payload.put("recipient", phone);
        payload.put("msg", message);

        ResponseEntity<String> response = restTemplate.postForEntity("https://api.sms4free.co.il/ApiSMS/v2/SendSMS", payload, String.class);

        System.out.println("SMS RESPONSE = " + response.getBody());
    }
}
