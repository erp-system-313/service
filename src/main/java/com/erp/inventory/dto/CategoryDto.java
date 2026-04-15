package com.erp.inventory.dto;

import com.erp.inventory.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Category.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
