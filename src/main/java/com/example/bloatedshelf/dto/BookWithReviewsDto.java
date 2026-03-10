package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Book;
import com.example.bloatedshelf.domain.Genre;

import java.util.List;

public record BookWithReviewsDto(Long id, String title, AuthorSummaryDto author, List<String> genreNames, List<ReviewDto> reviews) {
    public static BookWithReviewsDto from(Book book) {
        List<String> genreNames = book.getGenres().stream()
                .map(Genre::getName)
                .toList();
        List<ReviewDto> reviewDtos = book.getReviews().stream()
                .map(ReviewDto::from)
                .toList();
        return new BookWithReviewsDto(
                book.getId(),
                book.getTitle(),
                AuthorSummaryDto.from(book.getAuthor()),
                genreNames,
                reviewDtos
        );
    }
}
