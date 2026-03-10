package com.example.bloatedshelf.repository;

import com.example.bloatedshelf.domain.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Naive - triggers N+1 later
    List<Book> findAll();

    @Query("""
        SELECT DISTINCT b FROM Book b
        JOIN FETCH b.author
        LEFT JOIN FETCH b.genres
        """)
    List<Book> findAllWithAuthorAndGenres();

    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.id = :genreId")
    List<Book> findByGenreId(@Param("genreId") Long genreId);

    @Query("""
        SELECT b.title, a.lastName, COUNT(lr)
        FROM Book b
        JOIN b.author a
        JOIN b.loanRecords lr
        GROUP BY b.id, a.id
        ORDER BY COUNT(lr) DESC
        """)
    List<Object[]> findTop10MostLoaned(Pageable pageable);
}
