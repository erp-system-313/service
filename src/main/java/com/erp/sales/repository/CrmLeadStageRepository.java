package com.erp.sales.repository;

import com.erp.sales.entity.CrmLeadStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmLeadStageRepository extends JpaRepository<CrmLeadStage, Long> {

    List<CrmLeadStage> findByTeamIdOrderBySequence(Long teamId);

    List<CrmLeadStage> findByTeamIdIsNullOrderBySequence();

    List<CrmLeadStage> findByIsWonTrue();
}
