package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.helpdesk.entity.SlaPolicy;
import com.erp.helpdesk.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/sla-policies")
@RequiredArgsConstructor
public class SlaPolicyController {

    private final SlaPolicyRepository slaPolicyRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlaPolicy>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(slaPolicyRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaPolicy>> getById(@PathVariable Long id) {
        return slaPolicyRepository.findById(id)
                .map(policy -> ResponseEntity.ok(ApiResponse.success(policy)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<SlaPolicy>>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(slaPolicyRepository.findByTeamId(teamId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SlaPolicy>> create(@RequestBody SlaPolicy policy) {
        SlaPolicy created = slaPolicyRepository.save(policy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaPolicy>> update(@PathVariable Long id, @RequestBody SlaPolicy policy) {
        return slaPolicyRepository.findById(id)
                .map(existing -> {
                    existing.setName(policy.getName());
                    existing.setTargetStageId(policy.getTargetStageId());
                    existing.setPriority(policy.getPriority());
                    existing.setDeadlineMinutes(policy.getDeadlineMinutes());
                    existing.setTeamId(policy.getTeamId());
                    return ResponseEntity.ok(ApiResponse.success(slaPolicyRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (slaPolicyRepository.existsById(id)) {
            slaPolicyRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
