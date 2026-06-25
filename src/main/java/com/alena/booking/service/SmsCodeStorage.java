package com.alena.booking.service;

import com.alena.booking.dto.SmsCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeStorage {

    private final Map<String, SmsCode> codes =
            new ConcurrentHashMap<>();

    public void save(String phone, String code) {
        codes.put(phone,
                new SmsCode(
                        code,
                        LocalDateTime.now().plusMinutes(5)));
    }

    public boolean validate(String phone, String code) {

        SmsCode smsCode = codes.get(phone);

        if (smsCode == null) {
            return false;
        }

        if (smsCode.expireAt().isBefore(LocalDateTime.now())) {
            codes.remove(phone);
            return false;
        }

        return smsCode.code().equals(code);
    }
}
