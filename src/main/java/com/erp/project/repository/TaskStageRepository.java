package com.erp.project.repository;

import com.erp.project.entity.TaskStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStageRepository extends JpaRepository<TaskStage, Long> {

    List<TaskStage> findByProjectIdOrderBySequence(Long projectId);
}
