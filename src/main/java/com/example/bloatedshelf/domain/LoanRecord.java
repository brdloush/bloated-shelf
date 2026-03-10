package com.example.bloatedshelf.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loan_record")
public class LoanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private LibraryMember member;

    @Column(name = "loaned_at", nullable = false)
    private LocalDate loanedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    public LoanRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LibraryMember getMember() { return member; }
    public void setMember(LibraryMember member) { this.member = member; }
    public LocalDate getLoanedAt() { return loanedAt; }
    public void setLoanedAt(LocalDate loanedAt) { this.loanedAt = loanedAt; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDate returnedAt) { this.returnedAt = returnedAt; }
}
