package com.alena.booking.controller;

import com.alena.booking.dto.ServiceDto;
import com.alena.booking.service.NailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final MessageSource messageSource;

    @GetMapping("/{lang}")
    public List<ServiceDto> getServices(@PathVariable String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        return Arrays.stream(NailService.values()).map(service -> new ServiceDto(
                service.name(),
                messageSource.getMessage("service." + service.name(),
                        null, locale),
                service.getPrice())).toList();
    }

}
