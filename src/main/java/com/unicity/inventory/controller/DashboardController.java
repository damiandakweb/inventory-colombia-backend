package com.unicity.inventory.controller;

import com.unicity.inventory.Mapping.DashboardSummaryDto;
import com.unicity.inventory.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDto getSummary() {
        return dashboardService.getDashboardSummary();
    }
}