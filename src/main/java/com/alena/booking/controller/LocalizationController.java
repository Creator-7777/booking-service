package com.alena.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
public class LocalizationController {

    private final MessageSource messageSource;

    @GetMapping("/{lang}")
    public Map<String, String> getTranslations(@PathVariable String lang) {

        Locale locale = Locale.forLanguageTag(lang);

        String[] keys = {
                "subtitle",
                "name",
                "phone",
                "service",
                "date",
                "time",
                "submit",
                "sendCode",
                "verifyCode",
                "chooseTime",
                "loading",
                "noSlots",
                "phoneInvalid",
                "bookingSuccess",
                "smsSent",
                "chooseDate",
                "enterCode",
                "wrongCode",
                "errorLoadingTimeSlots",
                "numberIsVerified",
                "errorSendSMS",
                "bookingImposible",
                "namePlaceholder",
                "phonePlaceholder",
                "verifyCodePlaceholder"
        };

        Map<String, String> result = new LinkedHashMap<>();

        for (String key : keys) {
            result.put(key, messageSource.getMessage(key, null, locale));
        }

        return result;
    }
}
