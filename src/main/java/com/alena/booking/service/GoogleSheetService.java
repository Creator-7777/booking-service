package com.alena.booking.service;

import com.alena.booking.dto.BookingSyncDto;
import com.alena.booking.dto.VerifiedCustomerSyncDto;
import com.alena.booking.entity.Appointment;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GoogleSheetService {

    @Value("${google.apps-script-url}")
    private String appsScriptUrl;

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final CloseableHttpClient httpClient;

    public void saveBooking(Appointment appointment) {

        Map<String, Object> payload = Map.of(
                "name", appointment.getCustomerName(),
                "phone", appointment.getPhone(),
                "service", appointment.getServices(),
                "date", appointment.getAppointmentDate().toString(),
                "time", appointment.getAppointmentTime()
        );

/*        try {
            postToGoogle(payload);
            log.info("Google Sheet SUCCESS");

        } catch (Exception e) {
            log.error("Google Sheet FAILED", e);
        }*/
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    appsScriptUrl,
                    payload,
                    String.class);

            log.info("STATUS={}", response.getStatusCode());
            log.info("BODY={}", response.getBody());
            log.info("Google Sheet SUCCESS");

        } catch (Exception e) {
            log.error("Google Sheet FAILED", e);
        }
    }

    public List<String> getBookedTimes(LocalDate date) {

        String url = appsScriptUrl + "?action=bookedTimes&date=" + date;

        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public void saveVerifiedPhone(String name , String phone) {

        Map<String, Object> payload = Map.of(
                "action","saveVerifiedPhone",
                "name", name,
                "phone", phone,
                "date", Instant.now().toString()

        );

        log.info("Sending VERIFIED PHONE payload: {}", payload);

        try {
            postToGoogle(payload);
            log.info("Verified phone SUCCESS");
        } catch(Exception e){
            log.error("Verified phone FAILED",e);
        }

        /*try {
            ResponseEntity<String> response = restTemplate.postForEntity(appsScriptUrl, payload, String.class);
            log.info("Status = {}", response.getStatusCode());
            log.info("Body   = {}", response.getBody());
            log.info("Google Sheet - Verified Customer has been saved to Verified Phones");

        } catch (Exception e) {
            log.error("Google Sheet - Verified Phones insert FAILED", e);
        }*/
    }

    public List<BookingSyncDto> loadBookings() {

        String url = appsScriptUrl + "?action=allBookings";
        ResponseEntity<BookingSyncDto[]> response = restTemplate.getForEntity(url, BookingSyncDto[].class);
        return Arrays.asList(response.getBody());
    }

    public List<VerifiedCustomerSyncDto> loadVerifiedPhones() {

        String url = appsScriptUrl + "?action=allVerifiedPhones";
        ResponseEntity<VerifiedCustomerSyncDto[]> response = restTemplate.getForEntity(url, VerifiedCustomerSyncDto[].class);
        return Arrays.asList(response.getBody());
    }

    private String postToGoogle(Map<String, Object> payload) throws Exception {

        HttpPost post = new HttpPost(appsScriptUrl);
        post.setHeader("Content-Type", "application/json");
        String json = new ObjectMapper().writeValueAsString(payload);
        log.info("POST {}", json);
        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String body = EntityUtils.toString(response.getEntity());
            //log.info("STATUS = {}", response. getCode());
            log.info("STATUS = {}", response.getStatusLine());
            log.info("BODY = {}", body);
            return body;
        }
    }

}
