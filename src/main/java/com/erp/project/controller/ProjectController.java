package com.erp.project.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.project.dto.*;
import com.erp.project.entity.ProjectState;
import com.erp.project.service.ProjectService;
import com.erp.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ProjectState state,
            @RequestParam(required = false) String search) {

        PageResponse<ProjectDto> projects = projectService.findAll(page, size, state, search);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getById(@PathVariable Long id) {
        ProjectDto project = projectService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto>> create(
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectDto project = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(project, "Project created successfully"));
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<ApiResponse<ProjectDto>> updateState(
            @PathVariable Long id,
            @RequestBody ProjectState state) {
        ProjectDto project = projectService.updateState(id, state);
        return ResponseEntity.ok(ApiResponse.success(project, "Project state updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stages")
    public ResponseEntity<ApiResponse<List<TaskStageDto>>> getStages(@PathVariable Long id) {
        List<TaskStageDto> stages = projectService.getStages(id);
        return ResponseEntity.ok(ApiResponse.success(stages));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getTasks(@PathVariable Long id) {
        List<TaskDto> tasks = projectService.getTasks(id);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskDto task = taskService.create(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(task, "Task created successfully"));
    }

    @GetMapping("/{id}/gantt")
    public ResponseEntity<ApiResponse<List<GanttTaskDto>>> getGantt(@PathVariable Long id) {
        List<GanttTaskDto> ganttData = projectService.getGanttData(id);
        return ResponseEntity.ok(ApiResponse.success(ganttData));
    }
}
