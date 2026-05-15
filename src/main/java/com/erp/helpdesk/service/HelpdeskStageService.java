package com.erp.helpdesk.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.HelpdeskStageDto;
import com.erp.helpdesk.entity.HelpdeskStage;
import com.erp.helpdesk.repository.HelpdeskStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpdeskStageService {

    private final HelpdeskStageRepository helpdeskStageRepository;

    public List<HelpdeskStageDto> findAll() {
        return helpdeskStageRepository.findAllByOrderBySequenceAsc().stream()
                .map(HelpdeskStageDto::fromEntity)
                .toList();
    }

    public List<HelpdeskStageDto> findByTeamId(Long teamId) {
        return helpdeskStageRepository.findByTeamIdOrderBySequenceAsc(teamId).stream()
                .map(HelpdeskStageDto::fromEntity)
                .toList();
    }

    public HelpdeskStageDto findById(Long id) {
        HelpdeskStage stage = helpdeskStageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskStage", id));
        return HelpdeskStageDto.fromEntity(stage);
    }

    @Transactional
    public HelpdeskStageDto create(String name, Integer sequence, Boolean fold, Long teamId) {
        HelpdeskStage stage = HelpdeskStage.builder()
                .name(name)
                .sequence(sequence != null ? sequence : 0)
                .fold(fold != null ? fold : false)
                .teamId(teamId)
                .build();

        stage = helpdeskStageRepository.save(stage);
        log.info("Created helpdesk stage: {} (team: {})", name, teamId);
        return HelpdeskStageDto.fromEntity(stage);
    }

    @Transactional
    public void update(Long id, String name, Integer sequence, Boolean fold) {
        HelpdeskStage stage = helpdeskStageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskStage", id));

        if (name != null) stage.setName(name);
        if (sequence != null) stage.setSequence(sequence);
        if (fold != null) stage.setFold(fold);

        helpdeskStageRepository.save(stage);
        log.info("Updated helpdesk stage with id: {}", id);
    }

    @Transactional
    public void delete(Long id) {
        helpdeskStageRepository.deleteById(id);
        log.info("Deleted helpdesk stage with id: {}", id);
    }
}
