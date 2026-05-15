package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpdeskTagRepository extends JpaRepository<HelpdeskTag, Long> {
}
