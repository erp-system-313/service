package com.erp.finance.dto;

import com.erp.finance.entity.JournalEntryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.erp.finance.entity.JournalEntry;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntryDto {
    private Long id;
    private String entryNumber;
    private LocalDate date;
    private String description;
    private String reference;
    private JournalEntryStatus status;
    private Long createdById;
    private String createdByName;
    private List<JournalEntryLineDto> lines;
    private LocalDateTime createdAt;
    private LocalDateTime postedAt;

    public static JournalEntryDto fromEntity(JournalEntry entry) {
        return JournalEntryDto.builder()
                .id(entry.getId())
                .entryNumber(entry.getEntryNumber())
                .date(entry.getDate())
                .description(entry.getDescription())
                .reference(entry.getReference())
                .status(entry.getStatus())
                .createdById(entry.getCreatedBy() != null ? entry.getCreatedBy().getId() : null)
                .createdByName(entry.getCreatedBy() != null ? entry.getCreatedBy().getEmail() : null)
                .createdAt(entry.getCreatedAt())
                .postedAt(entry.getPostedAt())
                .build();
    }
}