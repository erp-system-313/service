package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.KbArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KbArticleDto {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private String tags;
    private Integer views;
    private Boolean isPublished;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KbArticleDto fromEntity(KbArticle article) {
        return KbArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .categoryId(article.getCategoryId())
                .tags(article.getTags())
                .views(article.getViews())
                .isPublished(article.getIsPublished())
                .createdBy(article.getCreatedBy())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
