package com.example.bloatedshelf.web;

import com.example.bloatedshelf.dto.StatsDto;
import com.example.bloatedshelf.service.AdminService;
import com.example.bloatedshelf.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final BookService bookService;

    public AdminController(AdminService adminService, BookService bookService) {
        this.adminService = adminService;
        this.bookService = bookService;
    }

    @GetMapping("/stats")
    public StatsDto getStats() {
        return adminService.getSystemStats();
    }

    @GetMapping("/most-loaned")
    public List<Map<String, Object>> getMostLoaned() {
        return adminService.getTop10MostLoanedBooks();
    }

    @PutMapping("/books/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveBook(@PathVariable Long id) {
        bookService.archiveBook(id);
    }
}
