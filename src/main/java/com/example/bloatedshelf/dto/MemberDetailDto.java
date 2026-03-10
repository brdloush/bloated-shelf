package com.example.bloatedshelf.dto;

import com.example.bloatedshelf.domain.LibraryMember;
import java.time.LocalDate;
import java.util.List;

public record MemberDetailDto(Long id, String username, String fullName, String email, LocalDate memberSince, List<LoanDto> loans) {
    public static MemberDetailDto from(LibraryMember member) {
        List<LoanDto> loanDtos = member.getLoanRecords().stream()
                .map(LoanDto::from)
                .toList();
        return new MemberDetailDto(
                member.getId(),
                member.getUsername(),
                member.getFullName(),
                member.getEmail(),
                member.getMemberSince(),
                loanDtos
        );
    }
}
