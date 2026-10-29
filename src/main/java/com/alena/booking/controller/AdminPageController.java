
package com.alena.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public String redirectToAdmin() {
        return "redirect:/admin/";
    }

    @GetMapping("/admin/")
    public String adminPage() {
        return "admin";
    }
}
