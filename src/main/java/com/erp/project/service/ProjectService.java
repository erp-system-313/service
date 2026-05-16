package com.erp.project.service;

import com.erp.common.annotation.Auditable;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.project.dto.CreateProjectRequest;
import com.erp.project.dto.GanttTaskDto;
import com.erp.project.dto.ProjectDto;
import com.erp.project.dto.TaskDto;
import com.erp.project.dto.TaskStageDto;
import com.erp.project.entity.Project;
import com.erp.project.entity.ProjectState;
import com.erp.project.entity.TaskStage;
import com.erp.project.repository.ProjectRepository;
import com.erp.project.repository.TaskRepository;
import com.erp.project.repository.TaskStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskStageRepository taskStageRepository;

    public PageResponse<ProjectDto> findAll(int page, int size, ProjectState state, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String searchPattern = search != null ? "%" + search + "%" : "";
        Page<Project> projects = projectRepository.findWithFilters(state, searchPattern, pageable);
        return PageResponse.from(projects.map(ProjectDto::fromEntity));
    }

    public ProjectDto findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return ProjectDto.fromEntity(project);
    }

    @Transactional
    @Auditable(action = "CREATE", entityType = "PROJECT")
    public ProjectDto create(CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .customerId(request.getCustomerId())
                .dateStart(request.getDateStart())
                .dateEnd(request.getDateEnd())
                .budget(request.getBudget())
                .state(ProjectState.PLANNING)
                .build();

        project = projectRepository.save(project);

        createDefaultStages(project.getId());

        log.info("Created project with id: {}", project.getId());
        return ProjectDto.fromEntity(project);
    }

    @Transactional
    @Auditable(action = "UPDATE", entityType = "PROJECT")
    public ProjectDto updateState(Long id, ProjectState state) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        project.setState(state);
        project = projectRepository.save(project);
        log.info("Updated project {} state to {}", id, state);
        return ProjectDto.fromEntity(project);
    }

    @Transactional
    @Auditable(action = "DELETE", entityType = "PROJECT")
    public void delete(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        taskRepository.findByProjectId(id).forEach(taskRepository::delete);
        taskStageRepository.findByProjectIdOrderBySequence(id).forEach(taskStageRepository::delete);
        projectRepository.delete(project);
        log.info("Deleted project with id: {}", id);
    }

    public List<TaskDto> getTasks(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(task -> {
                    TaskDto dto = TaskDto.fromEntity(task);
                    taskStageRepository.findById(task.getStageId()).ifPresent(stage ->
                            dto.setStageName(stage.getName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TaskStageDto> getStages(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return taskStageRepository.findByProjectIdOrderBySequence(projectId).stream()
                .map(TaskStageDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GanttTaskDto> getGanttData(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return taskRepository.findByProjectId(projectId).stream()
                .map(task -> {
                    String stageName = taskStageRepository.findById(task.getStageId())
                            .map(TaskStage::getName)
                            .orElse(null);
                    return GanttTaskDto.builder()
                            .id(task.getId())
                            .name(task.getName())
                            .stageName(stageName)
                            .assignedTo(task.getAssignedTo())
                            .startDate(task.getStartDate())
                            .dueDate(task.getDueDate())
                            .estimatedHours(task.getEstimatedHours())
                            .actualHours(task.getActualHours())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void createDefaultStages(Long projectId) {
        List<TaskStage> defaultStages = Arrays.asList(
                TaskStage.builder().projectId(projectId).name("To Do").sequence(0).isDefault(true).build(),
                TaskStage.builder().projectId(projectId).name("In Progress").sequence(1).isDefault(false).build(),
                TaskStage.builder().projectId(projectId).name("Review").sequence(2).isDefault(false).build(),
                TaskStage.builder().projectId(projectId).name("Done").sequence(3).isDefault(false).build()
        );
        taskStageRepository.saveAll(defaultStages);
    }
}
