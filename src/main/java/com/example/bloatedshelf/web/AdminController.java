package com.example.bloatedshelf.web;

import com.example.bloatedshelf.dto.StatsDto;
import com.example.bloatedshelf.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public StatsDto getStats() {
        return adminService.getSystemStats();
    }

    @GetMapping("/most-loaned")
    public List<Map<String, Object>> getMostLoaned() {
        return adminService.getTop10MostLoanedBooks();
    }
}
