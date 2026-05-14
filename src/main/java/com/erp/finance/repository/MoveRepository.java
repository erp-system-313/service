package com.erp.finance.repository;

import com.erp.finance.entity.Move;
import com.erp.finance.entity.MoveState;
import com.erp.finance.entity.MoveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {

    @Query("SELECT m FROM Move m WHERE " +
           "(:state IS NULL OR m.state = :state) AND " +
           "(:moveType IS NULL OR m.moveType = :moveType) AND " +
           "(:dateFrom IS NULL OR m.date >= :dateFrom) AND " +
           "(:dateTo IS NULL OR m.date <= :dateTo)")
    Page<Move> findWithFilters(
            @Param("state") MoveState state,
            @Param("moveType") MoveType moveType,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);

    @Query("SELECT m FROM Move m LEFT JOIN FETCH m.lines WHERE m.id = :id")
    Optional<Move> findByIdWithLines(@Param("id") Long id);

    Optional<Move> findByName(String name);

    boolean existsByName(String name);

    long countByState(MoveState state);

    @Query("SELECT COALESCE(MAX(m.secureSequenceNumber), 0) FROM Move m WHERE m.journal.id = :journalId AND m.state = 'POSTED'")
    Integer getMaxSecureSequenceNumber(@Param("journalId") Long journalId);

    @Query("SELECT m.inalterableHash FROM Move m WHERE m.journal.id = :journalId AND m.state = 'POSTED' ORDER BY m.secureSequenceNumber DESC")
    java.util.List<String> findLastPostedHash(@Param("journalId") Long journalId, Pageable pageable);
}
