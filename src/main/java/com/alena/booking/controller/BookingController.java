package com.alena.booking.controller;

import com.alena.booking.dto.BookingRequest;
import com.alena.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin
public class BookingController {

    private final BookingService service;

    @GetMapping("/booked-times")
    public List<String> bookedTimes(
            @RequestParam LocalDate date) {

        return service.getBookedTimes(date);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody BookingRequest request) {

        service.createBooking(request);

        return ResponseEntity.ok().build();
    }
}
