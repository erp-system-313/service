package com.erp.helpdesk.service;

import com.erp.admin.entity.User;
import com.erp.admin.repository.UserRepository;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.CreateHelpdeskTeamRequest;
import com.erp.helpdesk.dto.HelpdeskTeamDto;
import com.erp.helpdesk.entity.HelpdeskTeam;
import com.erp.helpdesk.repository.HelpdeskTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpdeskTeamService {

    private final HelpdeskTeamRepository helpdeskTeamRepository;
    private final UserRepository userRepository;

    public PageResponse<HelpdeskTeamDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<HelpdeskTeam> teams = helpdeskTeamRepository.findByIsActiveTrue(pageable);
        return PageResponse.from(teams.map(HelpdeskTeamDto::fromEntity));
    }

    public HelpdeskTeamDto findById(Long id) {
        HelpdeskTeam team = helpdeskTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTeam", id));
        return HelpdeskTeamDto.fromEntity(team);
    }

    @Transactional
    public HelpdeskTeamDto create(CreateHelpdeskTeamRequest request) {
        Set<User> members = new HashSet<>();
        if (request.getMemberIds() != null) {
            members = request.getMemberIds().stream()
                    .map(uid -> userRepository.findById(uid)
                            .orElseThrow(() -> new ResourceNotFoundException("User", uid)))
                    .collect(Collectors.toSet());
        }

        HelpdeskTeam team = HelpdeskTeam.builder()
                .name(request.getName())
                .description(request.getDescription())
                .members(members)
                .aliasName(request.getAliasName())
                .aliasDomain(request.getAliasDomain())
                .useAlias(request.getUseAlias() != null ? request.getUseAlias() : false)
                .defaultStage(request.getDefaultStage())
                .teamLeadId(request.getTeamLeadId())
                .teamLeadName(request.getTeamLeadName())
                .defaultPriority(request.getDefaultPriority())
                .autoAssign(request.getAutoAssign() != null ? request.getAutoAssign() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        team = helpdeskTeamRepository.save(team);
        log.info("Created helpdesk team with id: {}", team.getId());
        return HelpdeskTeamDto.fromEntity(team);
    }

    @Transactional
    public HelpdeskTeamDto update(Long id, CreateHelpdeskTeamRequest request) {
        HelpdeskTeam team = helpdeskTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTeam", id));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        if (request.getAliasName() != null) team.setAliasName(request.getAliasName());
        if (request.getAliasDomain() != null) team.setAliasDomain(request.getAliasDomain());
        if (request.getUseAlias() != null) team.setUseAlias(request.getUseAlias());
        if (request.getDefaultStage() != null) team.setDefaultStage(request.getDefaultStage());
        if (request.getTeamLeadId() != null) team.setTeamLeadId(request.getTeamLeadId());
        if (request.getTeamLeadName() != null) team.setTeamLeadName(request.getTeamLeadName());
        if (request.getDefaultPriority() != null) team.setDefaultPriority(request.getDefaultPriority());
        if (request.getAutoAssign() != null) team.setAutoAssign(request.getAutoAssign());
        if (request.getIsActive() != null) team.setIsActive(request.getIsActive());

        if (request.getMemberIds() != null) {
            Set<User> members = request.getMemberIds().stream()
                    .map(uid -> userRepository.findById(uid)
                            .orElseThrow(() -> new ResourceNotFoundException("User", uid)))
                    .collect(Collectors.toSet());
            team.setMembers(members);
        }

        team = helpdeskTeamRepository.save(team);
        log.info("Updated helpdesk team with id: {}", id);
        return HelpdeskTeamDto.fromEntity(team);
    }

    @Transactional
    public void delete(Long id) {
        HelpdeskTeam team = helpdeskTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTeam", id));
        team.setIsActive(false);
        helpdeskTeamRepository.save(team);
        log.info("Soft-deleted helpdesk team with id: {}", id);
    }
}
