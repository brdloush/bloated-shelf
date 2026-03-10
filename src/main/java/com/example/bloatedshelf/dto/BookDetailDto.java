package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Book;
import com.example.bloatedshelf.domain.Genre;

import java.util.List;

public record BookDetailDto(Long id, String title, String isbn, int publishedYear, int availableCopies, AuthorSummaryDto author, List<String> genres, List<ReviewDto> reviews) {
    public static BookDetailDto from(Book book) {
        List<String> genreNames = book.getGenres().stream()
                .map(Genre::getName)
                .toList();
        List<ReviewDto> reviewDtos = book.getReviews().stream()
                .map(ReviewDto::from)
                .toList();
        return new BookDetailDto(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPublishedYear() != null ? book.getPublishedYear() : 0,
                book.getAvailableCopies(),
                AuthorSummaryDto.from(book.getAuthor()),
                genreNames,
                reviewDtos
        );
    }
}
