package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskCategoryRepository extends JpaRepository<HelpdeskCategory, Long> {

    List<HelpdeskCategory> findByTeamId(Long teamId);
}
