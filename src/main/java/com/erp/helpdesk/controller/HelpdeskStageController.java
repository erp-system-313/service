package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.helpdesk.dto.HelpdeskStageDto;
import com.erp.helpdesk.service.HelpdeskStageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/support/stages")
@RequiredArgsConstructor
public class HelpdeskStageController {

    private final HelpdeskStageService helpdeskStageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HelpdeskStageDto>>> getAll(
            @RequestParam(required = false) Long teamId) {

        List<HelpdeskStageDto> stages;
        if (teamId != null) {
            stages = helpdeskStageService.findByTeamId(teamId);
        } else {
            stages = helpdeskStageService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(stages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskStageDto>> getById(@PathVariable Long id) {
        HelpdeskStageDto stage = helpdeskStageService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(stage));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelpdeskStageDto>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sequence = body.get("sequence") != null ? (Integer) body.get("sequence") : 0;
        Boolean fold = body.get("fold") != null ? (Boolean) body.get("fold") : false;
        Long teamId = body.get("teamId") != null ? ((Number) body.get("teamId")).longValue() : null;

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Stage name is required"));
        }

        HelpdeskStageDto stage = helpdeskStageService.create(name, sequence, fold, teamId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(stage, "Stage created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        String name = (String) body.get("name");
        Integer sequence = body.get("sequence") != null ? ((Number) body.get("sequence")).intValue() : null;
        Boolean fold = (Boolean) body.get("fold");

        helpdeskStageService.update(id, name, sequence, fold);
        return ResponseEntity.ok(ApiResponse.success(null, "Stage updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        helpdeskStageService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
