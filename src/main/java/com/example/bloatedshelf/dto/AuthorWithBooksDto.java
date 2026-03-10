package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Author;
import com.example.bloatedshelf.domain.Book;

import java.util.List;

public record AuthorWithBooksDto(Long id, String firstName, String lastName, List<BookSummaryDto> books) {
    public static AuthorWithBooksDto from(Author author) {
        List<BookSummaryDto> bookDtos = author.getBooks().stream()
                .map(BookSummaryDto::from)
                .toList();
        return new AuthorWithBooksDto(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                bookDtos
        );
    }
}
