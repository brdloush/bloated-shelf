package com.example.bloatedshelf.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @Column(name = "published_year")
    private Short publishedYear;

    @Column(name = "available_copies", nullable = false)
    private Short availableCopies = 1;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_genre",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<LoanRecord> loanRecords = new ArrayList<>();

    public Book() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Short getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Short publishedYear) { this.publishedYear = publishedYear; }
    public Short getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Short availableCopies) { this.availableCopies = availableCopies; }
    public Set<Genre> getGenres() { return genres; }
    public void setGenres(Set<Genre> genres) { this.genres = genres; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public List<LoanRecord> getLoanRecords() { return loanRecords; }
    public void setLoanRecords(List<LoanRecord> loanRecords) { this.loanRecords = loanRecords; }
}
