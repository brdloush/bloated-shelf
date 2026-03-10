package com.example.bloatedshelf.web;

import com.example.bloatedshelf.dto.BookDetailDto;
import com.example.bloatedshelf.dto.BookWithReviewsDto;
import com.example.bloatedshelf.dto.ReviewDto;
import com.example.bloatedshelf.service.BookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@PreAuthorize("hasRole('MEMBER')")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookWithReviewsDto> getBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookDetailDto getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewDto> getBookReviews(@PathVariable Long id) {
        return bookService.getReviewsForBook(id);
    }

    @GetMapping("/by-genre/{genreId}")
    public List<BookWithReviewsDto> getBooksByGenre(@PathVariable Long genreId) {
        return bookService.getBooksByGenreId(genreId);
    }
}
