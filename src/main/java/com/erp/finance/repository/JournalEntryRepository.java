package com.erp.finance.repository;

import com.erp.finance.entity.JournalEntry;
import com.erp.finance.entity.JournalEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query("SELECT j FROM JournalEntry j WHERE " +
           "(:status IS NULL OR j.status = :status) AND " +
           "(:dateFrom IS NULL OR j.date >= :dateFrom) AND " +
           "(:dateTo IS NULL OR j.date <= :dateTo)")
    Page<JournalEntry> findWithFilters(
            @Param("status") JournalEntryStatus status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);

    Optional<JournalEntry> findByIdWithLines(Long id);

    Optional<JournalEntry> findByEntryNumber(String entryNumber);

    boolean existsByEntryNumber(String entryNumber);
}