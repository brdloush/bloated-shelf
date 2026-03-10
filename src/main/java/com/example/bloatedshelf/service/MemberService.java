package com.example.bloatedshelf.service;

import com.example.bloatedshelf.domain.LibraryMember;
import com.example.bloatedshelf.dto.LoanDto;
import com.example.bloatedshelf.dto.MemberDetailDto;
import com.example.bloatedshelf.dto.MemberWithLoansDto;
import com.example.bloatedshelf.repository.LibraryMemberRepository;
import com.example.bloatedshelf.repository.LoanRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final LibraryMemberRepository memberRepository;
    private final LoanRecordRepository loanRepository;

    public MemberService(LibraryMemberRepository memberRepository, LoanRecordRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    // N+1 DEMO:
    // 1 SELECT for all members
    // + 1 SELECT per member to load loan records
    // + 1 SELECT per loan to load the book
    // + 1 SELECT per book to load the author
    // + 1 SELECT per book to load the genres
    public List<MemberWithLoansDto> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberWithLoansDto::from)
                .toList();
    }

    // Cascading N+1 for a single member
    public MemberDetailDto getMemberById(Long id) {
        LibraryMember member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return MemberDetailDto.from(member);
    }

    // N+1 DEMO:
    // 1 SELECT to find active loans for member
    // + 1 SELECT per loan to load the book
    // + 1 SELECT per book to load the author
    // + 1 SELECT per book to load genres
    public List<LoanDto> getActiveLoansForMember(Long id) {
        return loanRepository.findActiveLoansByMemberId(id)
                .stream()
                .map(LoanDto::from)
                .toList();
    }
}
