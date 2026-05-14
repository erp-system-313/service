package com.erp.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateKbArticleRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    private Long categoryId;

    private String tags;

    private Boolean isPublished;
}
