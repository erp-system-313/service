package com.erp.sales.service;

import com.erp.admin.entity.User;
import com.erp.admin.repository.UserRepository;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.sales.dto.CreateSalesTeamRequest;
import com.erp.sales.dto.SalesTeamDto;
import com.erp.sales.entity.SalesTeam;
import com.erp.sales.repository.SalesTeamRepository;
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
public class SalesTeamService {

    private final SalesTeamRepository salesTeamRepository;
    private final UserRepository userRepository;

    public PageResponse<SalesTeamDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<SalesTeam> teams = salesTeamRepository.findByIsActiveTrue(pageable);
        return PageResponse.from(teams.map(SalesTeamDto::fromEntity));
    }

    public SalesTeamDto findById(Long id) {
        SalesTeam team = salesTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesTeam", id));
        return SalesTeamDto.fromEntity(team);
    }

    @Transactional
    public SalesTeamDto create(CreateSalesTeamRequest request) {
        Set<User> members = new HashSet<>();
        if (request.getMemberIds() != null) {
            members = request.getMemberIds().stream()
                    .map(uid -> userRepository.findById(uid)
                            .orElseThrow(() -> new ResourceNotFoundException("User", uid)))
                    .collect(Collectors.toSet());
        }

        SalesTeam team = SalesTeam.builder()
                .name(request.getName())
                .description(request.getDescription())
                .members(members)
                .targetRevenue(request.getTargetRevenue() != null ? request.getTargetRevenue() : java.math.BigDecimal.ZERO)
                .build();

        team = salesTeamRepository.save(team);
        log.info("Created sales team with id: {}", team.getId());
        return SalesTeamDto.fromEntity(team);
    }

    @Transactional
    public SalesTeamDto update(Long id, CreateSalesTeamRequest request) {
        SalesTeam team = salesTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesTeam", id));

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setTargetRevenue(request.getTargetRevenue() != null ? request.getTargetRevenue() : java.math.BigDecimal.ZERO);

        if (request.getMemberIds() != null) {
            Set<User> members = request.getMemberIds().stream()
                    .map(uid -> userRepository.findById(uid)
                            .orElseThrow(() -> new ResourceNotFoundException("User", uid)))
                    .collect(Collectors.toSet());
            team.setMembers(members);
        }

        team = salesTeamRepository.save(team);
        log.info("Updated sales team with id: {}", id);
        return SalesTeamDto.fromEntity(team);
    }

    @Transactional
    public void delete(Long id) {
        SalesTeam team = salesTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesTeam", id));
        team.setIsActive(false);
        salesTeamRepository.save(team);
        log.info("Soft-deleted sales team with id: {}", id);
    }
}
