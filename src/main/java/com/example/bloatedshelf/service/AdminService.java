package com.example.bloatedshelf.service;

import com.example.bloatedshelf.dto.StatsDto;
import com.example.bloatedshelf.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final LibraryMemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final LoanRecordRepository loanRepository;

    public AdminService(AuthorRepository authorRepository, BookRepository bookRepository,
                        LibraryMemberRepository memberRepository, ReviewRepository reviewRepository,
                        LoanRecordRepository loanRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.reviewRepository = reviewRepository;
        this.loanRepository = loanRepository;
    }

    // Efficient: 5 simple count queries
    public StatsDto getSystemStats() {
        return new StatsDto(
                authorRepository.count(),
                bookRepository.count(),
                memberRepository.count(),
                reviewRepository.count(),
                loanRepository.count()
        );
    }

    // Efficient: Single JOIN query
    public List<Map<String, Object>> getTop10MostLoanedBooks() {
        List<Object[]> results = bookRepository.findTop10MostLoaned(PageRequest.of(0, 10));
        return results.stream()
                .map(row -> Map.of(
                        "title", row[0],
                        "authorLastName", row[1],
                        "loanCount", row[2]
                ))
                .collect(Collectors.toList());
    }
}
