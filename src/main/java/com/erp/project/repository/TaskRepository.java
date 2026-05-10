package com.erp.project.repository;

import com.erp.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId ORDER BY t.createdAt DESC")
    List<Task> findByProjectIdOrderByCreatedAtDesc(@Param("projectId") Long projectId);
}
