package com.example.bloatedshelf.service;

import com.example.bloatedshelf.domain.Book;
import com.example.bloatedshelf.dto.BookDetailDto;
import com.example.bloatedshelf.dto.BookWithReviewsDto;
import com.example.bloatedshelf.dto.ReviewDto;
import com.example.bloatedshelf.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // N+1 DEMO:
    // 1 SELECT for all books
    // + 1 SELECT per book to load genres
    // + 1 SELECT per book to load reviews
    // + 1 SELECT per review to load the member who wrote it
    // + 1 SELECT per book to load the author (if not in L1 cache)
    // Massive query explosion!
    public List<BookWithReviewsDto> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookWithReviewsDto::from)
                .toList();
    }

    // Cascading N+1
    public BookDetailDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        return BookDetailDto.from(book);
    }

    // N+1 DEMO:
    // 1 SELECT for book
    // + 1 SELECT for all reviews of that book
    // + 1 SELECT per review to load the member
    public List<ReviewDto> getReviewsForBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        return book.getReviews().stream()
                .map(ReviewDto::from) // Triggers loading of member for each review
                .toList();
    }

    @Transactional
    public void archiveBook(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));
        book.setArchived(true);
        book.setArchivedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    // N+1 DEMO:
    // 1 SELECT for books by genre (join)
    // + 1 SELECT per book to load other genres
    // + 1 SELECT per book to load reviews
    // + 1 SELECT per review to load the member
    public List<BookWithReviewsDto> getBooksByGenreId(Long genreId) {
        return bookRepository.findByGenreId(genreId)
                .stream()
                .map(BookWithReviewsDto::from)
                .toList();
    }
}
