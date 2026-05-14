package com.erp.helpdesk.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.HelpdeskCategoryDto;
import com.erp.helpdesk.entity.HelpdeskCategory;
import com.erp.helpdesk.repository.HelpdeskCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpdeskCategoryService {

    private final HelpdeskCategoryRepository helpdeskCategoryRepository;

    public List<HelpdeskCategoryDto> findAll() {
        return helpdeskCategoryRepository.findAll().stream()
                .map(HelpdeskCategoryDto::fromEntity)
                .toList();
    }

    public List<HelpdeskCategoryDto> findByTeamId(Long teamId) {
        return helpdeskCategoryRepository.findByTeamId(teamId).stream()
                .map(HelpdeskCategoryDto::fromEntity)
                .toList();
    }

    @Transactional
    public HelpdeskCategoryDto create(String name, Long teamId) {
        HelpdeskCategory category = HelpdeskCategory.builder()
                .name(name)
                .teamId(teamId)
                .build();

        category = helpdeskCategoryRepository.save(category);
        log.info("Created helpdesk category: {}", name);
        return HelpdeskCategoryDto.fromEntity(category);
    }

    @Transactional
    public HelpdeskCategoryDto update(Long id, String name, Long teamId) {
        HelpdeskCategory category = helpdeskCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskCategory", id));

        if (name != null) category.setName(name);
        if (teamId != null) category.setTeamId(teamId);

        category = helpdeskCategoryRepository.save(category);
        log.info("Updated helpdesk category with id: {}", id);
        return HelpdeskCategoryDto.fromEntity(category);
    }

    @Transactional
    public void delete(Long id) {
        helpdeskCategoryRepository.deleteById(id);
        log.info("Deleted helpdesk category with id: {}", id);
    }
}
