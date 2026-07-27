package com.alena.booking.controller;

import com.alena.booking.dto.BookingRequest;
import com.alena.booking.dto.BookingSheetDto;
import com.alena.booking.dto.CancelBookingRequest;
import com.alena.booking.dto.CancelOldBookingRequest;
import com.alena.booking.service.BookingService;
import com.alena.booking.service.GoogleSheetService;
import com.alena.booking.service.SheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin
public class BookingController {

    private final BookingService service;
    private final GoogleSheetService googleSheetService;

    private final SheetsService sheetsService;

    @GetMapping("/booked-times")
    public List<String> bookedTimes(@RequestParam LocalDate date) throws IOException {
        return sheetsService.getBookedTimes(date);
        //return googleSheetService.getBookedTimes(date);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody BookingRequest request) {

        service.createBooking(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public void cancel(@RequestBody CancelBookingRequest request) throws IOException {
        service.cancel(request);

    }

    @PostMapping("/cancel/old")
    public void cancelOld(@RequestBody CancelOldBookingRequest request) throws IOException {
        service.cancelOld(request);

    }

    @GetMapping("/test/sheets")
    public List<BookingSheetDto> test() throws Exception {
        return sheetsService.loadBookings();

    }
}
