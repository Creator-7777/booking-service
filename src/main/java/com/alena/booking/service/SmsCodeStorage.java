package com.alena.booking.service;

import com.alena.booking.dto.SmsCode;
import com.alena.booking.repository.VerifiedCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SmsCodeStorage.class);



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

        if (!smsCode.code().equals(code)) {
            return false;
        }

        else if (smsCode.expireAt().isBefore(LocalDateTime.now())) {
            codes.remove(phone);
            return false;
        }

        verifiedCustomerService.saveVerified(name, phone);
        log.info("New entry was saved to VerifiedCustomers DB table");

        try {
            googleSheetService.saveVerifiedPhone(name, phone);
            log.info("New entry was saved to VerifiedCustomers sheet");
        } catch (Exception ex) {
            log.error("Cannot update VerifiedCustomers sheet", ex);
        }

        codes.remove(phone);
        return true;
    }

    public boolean isVerified(String phone) {
        return verifiedPhoneRepository.existsByPhone(phone);
    }
}
