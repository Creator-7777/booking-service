package com.alena.booking.controller;

import com.alena.booking.dto.SmsRequest;
import com.alena.booking.dto.VerifiedCustomerRequest;
import com.alena.booking.service.VerifiedCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class VerifiedCustomerController {

    private final VerifiedCustomerService service;

    @GetMapping("/verified")
    public boolean verified(@RequestBody VerifiedCustomerRequest request) {
        return service.isVerified(request.getPhone());
    }
}