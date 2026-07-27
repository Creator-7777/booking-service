package com.alena.booking.controller;

import com.alena.booking.dto.BookingHistoryDto;
import com.alena.booking.dto.BookingSheetDto;
import com.alena.booking.service.CabinetService;
import com.alena.booking.service.SheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cabinet")
@RequiredArgsConstructor
public class CabinetController {

    private final CabinetService cabinetService;
    private final SheetsService sheetsService;

    @GetMapping("/history")
    public List<BookingHistoryDto> history(@RequestParam String phone){
        return cabinetService.getHistory(phone);
    }

    @GetMapping("/test/sheets")
    public List<BookingSheetDto> test() throws Exception {
        return sheetsService.loadBookings();

    }

}
