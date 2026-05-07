package com.erp.project.repository;

import com.erp.project.entity.Project;
import com.erp.project.entity.ProjectState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByState(ProjectState state, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE " +
           "(:state IS NULL OR p.state = :state) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> findWithFilters(
            @Param("state") ProjectState state,
            @Param("search") String search,
            Pageable pageable);
}
