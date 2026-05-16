package com.erp.project.service;

import com.erp.common.annotation.Auditable;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.project.dto.CreateTaskRequest;
import com.erp.project.dto.TaskDto;
import com.erp.project.dto.UpdateTaskRequest;
import com.erp.project.entity.Task;
import com.erp.project.repository.ProjectRepository;
import com.erp.project.repository.TaskRepository;
import com.erp.project.repository.TaskStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskStageRepository taskStageRepository;

    @Transactional
    @Auditable(action = "CREATE", entityType = "TASK")
    public TaskDto create(Long projectId, CreateTaskRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }

        if (request.getStageId() != null && !taskStageRepository.existsById(request.getStageId())) {
            throw new ResourceNotFoundException("TaskStage", request.getStageId());
        }

        Task task = Task.builder()
                .projectId(projectId)
                .name(request.getName())
                .description(request.getDescription())
                .assignedTo(request.getAssignedTo())
                .stageId(request.getStageId())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .actualHours(request.getActualHours())
                .build();

        task = taskRepository.save(task);
        log.info("Created task with id: {} in project: {}", task.getId(), projectId);

        TaskDto dto = TaskDto.fromEntity(task);
        taskStageRepository.findById(task.getStageId()).ifPresent(stage ->
                dto.setStageName(stage.getName()));
        return dto;
    }

    @Transactional
    @Auditable(action = "UPDATE", entityType = "TASK")
    public TaskDto update(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (request.getName() != null) {
            task.setName(request.getName());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }
        if (request.getStageId() != null) {
            if (!taskStageRepository.existsById(request.getStageId())) {
                throw new ResourceNotFoundException("TaskStage", request.getStageId());
            }
            task.setStageId(request.getStageId());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }

        task = taskRepository.save(task);
        log.info("Updated task with id: {}", id);

        TaskDto dto = TaskDto.fromEntity(task);
        taskStageRepository.findById(task.getStageId()).ifPresent(stage ->
                dto.setStageName(stage.getName()));
        return dto;
    }
}
