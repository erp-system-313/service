package com.erp.project.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.project.dto.TaskDto;
import com.erp.project.dto.UpdateTaskRequest;
import com.erp.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskDto task = taskService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(task, "Task updated successfully"));
    }
}
