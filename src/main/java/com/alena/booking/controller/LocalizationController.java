package com.alena.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String,String> getTranslations(
            @PathVariable String lang) {

        Locale locale = Locale.forLanguageTag(lang);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", locale);

        return bundle.keySet()
                .stream()
                .collect(Collectors.toMap(
                        key -> key,
                        bundle::getString));

    }

}
