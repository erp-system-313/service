package com.erp.sales.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.sales.dto.IncotermDto;
import com.erp.sales.entity.Incoterm;
import com.erp.sales.repository.IncotermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("salesIncotermService")
@RequiredArgsConstructor
@Slf4j
public class IncotermService {

    private final IncotermRepository incotermRepository;

    public List<IncotermDto> findAll() {
        return incotermRepository.findAll().stream()
                .map(IncotermDto::fromEntity)
                .toList();
    }

    public IncotermDto findById(Long id) {
        Incoterm incoterm = incotermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incoterm", id));
        return IncotermDto.fromEntity(incoterm);
    }

    public IncotermDto create(String code, String name, String description) {
        Incoterm incoterm = Incoterm.builder()
                .code(code)
                .name(name)
                .description(description)
                .build();
        incoterm = incotermRepository.save(incoterm);
        log.info("Created incoterm: {} ({})", code, name);
        return IncotermDto.fromEntity(incoterm);
    }
}
