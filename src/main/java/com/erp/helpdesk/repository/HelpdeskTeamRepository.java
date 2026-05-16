package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskTeamRepository extends JpaRepository<HelpdeskTeam, Long> {
    Page<HelpdeskTeam> findByIsActiveTrue(Pageable pageable);

    List<HelpdeskTeam> findByIsActiveTrue();
}
