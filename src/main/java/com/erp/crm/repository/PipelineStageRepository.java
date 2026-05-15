package com.erp.crm.repository;

import com.erp.crm.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {

    Optional<PipelineStage> findByName(String name);

    List<PipelineStage> findAllByOrderBySequenceAsc();

    Optional<PipelineStage> findByIsDefaultTrue();
}
