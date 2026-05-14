package com.erp.finance.repository;

import com.erp.finance.entity.Journal;
import com.erp.finance.entity.JournalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {

    List<Journal> findByType(JournalType type);

    Page<Journal> findByType(JournalType type, Pageable pageable);

    List<Journal> findByActiveTrue();

    Optional<Journal> findByCode(String code);

    boolean existsByCode(String code);
}
