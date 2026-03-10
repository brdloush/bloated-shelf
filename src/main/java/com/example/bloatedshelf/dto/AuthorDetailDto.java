package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Author;

import java.util.List;

public record AuthorDetailDto(Long id, String firstName, String lastName, int birthYear, String nationality, List<BookSummaryDto> books) {
    public static AuthorDetailDto from(Author author) {
        List<BookSummaryDto> bookDtos = author.getBooks().stream()
                .map(BookSummaryDto::from)
                .toList();
        return new AuthorDetailDto(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getBirthYear() != null ? author.getBirthYear() : 0,
                author.getNationality(),
                bookDtos
        );
    }
}
