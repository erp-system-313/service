package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelpdeskTeamRepository extends JpaRepository<HelpdeskTeam, Long> {

    // Existing methods
    Page<HelpdeskTeam> findByIsActiveTrue(Pageable pageable);

    // New email gateway methods
    List<HelpdeskTeam> findByIsActiveTrue();

    Optional<HelpdeskTeam> findByAliasName(String aliasName);

    Optional<HelpdeskTeam> findByAliasNameAndAliasDomain(String aliasName, String aliasDomain);

    List<HelpdeskTeam> findByUseAliasTrueAndIsActiveTrue();
}
