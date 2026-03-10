package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.Review;
import java.time.LocalDateTime;

public record ReviewDto(Long id, int rating, String comment, String reviewerName, LocalDateTime reviewedAt) {
    public static ReviewDto from(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getMember().getFullName(),
                review.getReviewedAt()
        );
    }
}
