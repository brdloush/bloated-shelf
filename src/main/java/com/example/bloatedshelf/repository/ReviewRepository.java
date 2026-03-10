package com.example.bloatedshelf.repository;

import com.example.bloatedshelf.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId ORDER BY r.reviewedAt DESC")
    List<Review> findByBookId(@Param("bookId") Long bookId);
}
