package com.alena.booking.service;

import com.alena.booking.dto.SmsCode;
import com.alena.booking.repository.VerifiedCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SmsCodeStorage {

    private final VerifiedCustomerService verifiedCustomerService;
    private final VerifiedCustomerRepository verifiedPhoneRepository;
    private final GoogleSheetService googleSheetService;


    private final Map<String, SmsCode> codes =
            new ConcurrentHashMap<>();

    public void save(String phone, String code) {
        codes.put(phone,
                new SmsCode(
                        code,
                        LocalDateTime.now().plusMinutes(5)));
    }

    public boolean validate(String phone, String code, String name) {

        SmsCode smsCode = codes.get(phone);

        if (smsCode == null) {
            return false;
        }

        if (smsCode.expireAt().isBefore(LocalDateTime.now())) {
            codes.remove(phone);
            return false;
        }

        if(smsCode.code().equals(code) ){
            verifiedCustomerService.saveVerified( name, phone);
            googleSheetService.saveVerifiedPhone(name, phone);

        }

        return smsCode.code().equals(code);
    }

    public boolean isVerified(String phone) {
        return verifiedPhoneRepository.existsByPhone(phone);
    }
}
