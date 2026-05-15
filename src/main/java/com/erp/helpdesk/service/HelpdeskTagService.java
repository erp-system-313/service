package com.erp.helpdesk.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.HelpdeskTagDto;
import com.erp.helpdesk.entity.HelpdeskTag;
import com.erp.helpdesk.repository.HelpdeskTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpdeskTagService {

    private final HelpdeskTagRepository helpdeskTagRepository;

    public List<HelpdeskTagDto> findAll() {
        return helpdeskTagRepository.findAll().stream()
                .map(HelpdeskTagDto::fromEntity)
                .toList();
    }

    @Transactional
    public HelpdeskTagDto create(String name, String color) {
        HelpdeskTag tag = HelpdeskTag.builder()
                .name(name)
                .color(color)
                .build();

        tag = helpdeskTagRepository.save(tag);
        log.info("Created helpdesk tag: {}", name);
        return HelpdeskTagDto.fromEntity(tag);
    }

    @Transactional
    public void delete(Long id) {
        helpdeskTagRepository.deleteById(id);
        log.info("Deleted helpdesk tag with id: {}", id);
    }
}
