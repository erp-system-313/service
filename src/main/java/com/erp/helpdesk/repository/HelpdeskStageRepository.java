package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskStageRepository extends JpaRepository<HelpdeskStage, Long> {

    List<HelpdeskStage> findByTeamIdOrderBySequenceAsc(Long teamId);

    List<HelpdeskStage> findAllByOrderBySequenceAsc();
}
