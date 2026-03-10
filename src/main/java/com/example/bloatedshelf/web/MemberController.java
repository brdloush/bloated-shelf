package com.example.bloatedshelf.web;

import com.example.bloatedshelf.dto.LoanDto;
import com.example.bloatedshelf.dto.MemberDetailDto;
import com.example.bloatedshelf.dto.MemberWithLoansDto;
import com.example.bloatedshelf.service.MemberService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@PreAuthorize("hasRole('LIBRARIAN')")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberWithLoansDto> getMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public MemberDetailDto getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    @GetMapping("/{id}/active-loans")
    public List<LoanDto> getActiveLoans(@PathVariable Long id) {
        return memberService.getActiveLoansForMember(id);
    }
}
