package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Book;
import com.example.bloatedshelf.domain.Genre;

import java.util.List;

public record BookSummaryDto(Long id, String title, int publishedYear, AuthorSummaryDto author, List<String> genres) {
    public static BookSummaryDto from(Book book) {
        List<String> genreNames = book.getGenres().stream()
                .map(Genre::getName)
                .toList();
        return new BookSummaryDto(
                book.getId(),
                book.getTitle(),
                book.getPublishedYear() != null ? book.getPublishedYear() : 0,
                AuthorSummaryDto.from(book.getAuthor()),
                genreNames
        );
    }
}
