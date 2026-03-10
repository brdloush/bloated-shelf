package com.example.bloatedshelf.service;

import com.example.bloatedshelf.domain.Author;
import com.example.bloatedshelf.dto.AuthorDetailDto;
import com.example.bloatedshelf.dto.AuthorWithBooksDto;
import com.example.bloatedshelf.dto.BookSummaryDto;
import com.example.bloatedshelf.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // N+1 DEMO:
    // 1 SELECT for all authors
    // + 1 SELECT per author to load their books (N queries)
    // + 1 SELECT per book to load its genres (N*M queries)
    // Total: 1 + N + N*M queries
    public List<AuthorWithBooksDto> getAllAuthors() {
        return authorRepository.findAllByOrderByLastNameAsc()
                .stream()
                .map(AuthorWithBooksDto::from) // Triggers loading of books, and then genres for each book
                .toList();
    }

    // N+1 DEMO:
    // 1 SELECT for the author
    // + 1 SELECT to load their books
    // + 1 SELECT per book to load its genres
    public AuthorDetailDto getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        return AuthorDetailDto.from(author); // Triggers lazy loading
    }
    
    // N+1 DEMO:
    // 1 SELECT for the author
    // + 1 SELECT to load their books
    // + 1 SELECT per book to load its genres
    public List<BookSummaryDto> getBooksByAuthorId(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        return author.getBooks().stream()
                .map(BookSummaryDto::from) // Triggers loading of genres per book
                .toList();
    }
}
