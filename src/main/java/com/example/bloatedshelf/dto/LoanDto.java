package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.LoanRecord;
import java.time.LocalDate;

public record LoanDto(Long id, BookSummaryDto book, LocalDate loanedAt, LocalDate dueDate, LocalDate returnedAt) {
    public static LoanDto from(LoanRecord loan) {
        return new LoanDto(
                loan.getId(),
                BookSummaryDto.from(loan.getBook()),
                loan.getLoanedAt(),
                loan.getDueDate(),
                loan.getReturnedAt()
        );
    }
}
