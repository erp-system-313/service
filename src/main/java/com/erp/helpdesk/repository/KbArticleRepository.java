package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.KbArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {

    Page<KbArticle> findByIsPublishedTrue(Pageable pageable);

    Page<KbArticle> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);

    @Query("SELECT a FROM KbArticle a WHERE a.isPublished = true AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KbArticle> searchPublished(@Param("query") String query, Pageable pageable);
}
