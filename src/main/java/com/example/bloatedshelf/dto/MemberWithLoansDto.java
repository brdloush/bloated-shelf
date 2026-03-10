package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.LibraryMember;
import java.util.List;

public record MemberWithLoansDto(Long id, String username, String fullName, List<LoanDto> loans) {
    public static MemberWithLoansDto from(LibraryMember member) {
        List<LoanDto> loanDtos = member.getLoanRecords().stream()
                .map(LoanDto::from)
                .toList();
        return new MemberWithLoansDto(
                member.getId(),
                member.getUsername(),
                member.getFullName(),
                loanDtos
        );
    }
}
