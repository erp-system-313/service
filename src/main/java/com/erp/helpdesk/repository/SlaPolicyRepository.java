package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {

    List<SlaPolicy> findByTeamId(Long teamId);

    Optional<SlaPolicy> findByTeamIdAndPriority(Long teamId, String priority);
}
