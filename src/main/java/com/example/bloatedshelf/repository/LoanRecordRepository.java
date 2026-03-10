package com.example.bloatedshelf.repository;

import com.example.bloatedshelf.domain.LoanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {

    @Query("""
        SELECT lr FROM LoanRecord lr
        WHERE lr.member.id = :memberId
        AND lr.returnedAt IS NULL
        """)
    List<LoanRecord> findActiveLoansByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT lr FROM LoanRecord lr
        JOIN FETCH lr.book b
        JOIN FETCH b.author
        JOIN FETCH lr.member
        WHERE lr.member.id = :memberId
        """)
    List<LoanRecord> findLoansByMemberIdWithDetails(@Param("memberId") Long memberId);
}
