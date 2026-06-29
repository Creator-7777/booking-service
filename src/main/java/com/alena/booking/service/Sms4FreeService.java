package com.alena.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    //private final RestTemplate restTemplate = new RestTemplate();

    private final WebClient webClient = WebClient.create();

    public void sendSms(String phone, String message) {

        Map<String, String> payload =
                new HashMap<>();

        payload.put("key", apiKey);
        payload.put("user", smsUser);
        payload.put("pass", smsPassword);
        payload.put("sender", smsSender);
        payload.put("recipient", phone);
        payload.put("msg", message);

        //ResponseEntity<String> response = restTemplate.postForEntity("https://api.sms4free.co.il/ApiSMS/v2/SendSMS", payload, String.class);

        try {

            System.out.println("=== SMS REQUEST ===");
            System.out.println("Recipient = " + phone);
            System.out.println("Sender = " + smsSender);
            System.out.println("User = " + smsUser);

            String response =
                    webClient.post()
                            .uri("https://api.sms4free.co.il/ApiSMS/v2/SendSMS", payload, String.class)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            System.out.println("SMS RESPONSE = " + response);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
