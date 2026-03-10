package com.example.bloatedshelf.web;

import com.example.bloatedshelf.dto.AuthorDetailDto;
import com.example.bloatedshelf.dto.AuthorWithBooksDto;
import com.example.bloatedshelf.dto.BookSummaryDto;
import com.example.bloatedshelf.service.AuthorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@PreAuthorize("hasRole('VIEWER')")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorWithBooksDto> getAuthors() {
        return authorService.getAllAuthors();
    }

    @GetMapping("/{id}")
    public AuthorDetailDto getAuthorById(@PathVariable Long id) {
        return authorService.getAuthorById(id);
    }

    @GetMapping("/{id}/books")
    public List<BookSummaryDto> getAuthorBooks(@PathVariable Long id) {
        return authorService.getBooksByAuthorId(id);
    }
}
