package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Author;

public record AuthorSummaryDto(Long id, String firstName, String lastName) {
    public static AuthorSummaryDto from(Author author) {
        return new AuthorSummaryDto(author.getId(), author.getFirstName(), author.getLastName());
    }
}
