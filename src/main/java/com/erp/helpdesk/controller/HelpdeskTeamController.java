package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.helpdesk.dto.CreateHelpdeskTeamRequest;
import com.erp.helpdesk.dto.HelpdeskTeamDto;
import com.erp.helpdesk.service.HelpdeskTeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support/teams")
@RequiredArgsConstructor
public class HelpdeskTeamController {

    private final HelpdeskTeamService helpdeskTeamService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HelpdeskTeamDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<HelpdeskTeamDto> teams = helpdeskTeamService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTeamDto>> getById(@PathVariable Long id) {
        HelpdeskTeamDto team = helpdeskTeamService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(team));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelpdeskTeamDto>> create(
            @Valid @RequestBody CreateHelpdeskTeamRequest request) {

        HelpdeskTeamDto team = helpdeskTeamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(team, "Team created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTeamDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateHelpdeskTeamRequest request) {

        HelpdeskTeamDto team = helpdeskTeamService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(team, "Team updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        helpdeskTeamService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
