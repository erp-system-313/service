package com.erp.finance.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.finance.entity.ScheduledReport;
import com.erp.finance.entity.ScheduledReportOutput;
import com.erp.finance.service.ScheduledReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports/scheduled")
@RequiredArgsConstructor
public class ScheduledReportController {

    private final ScheduledReportService scheduledReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduledReport>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(scheduledReportService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledReport>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scheduledReportService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduledReport>> create(@RequestBody ScheduledReport report) {
        return ResponseEntity.ok(ApiResponse.success(scheduledReportService.create(report)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledReport>> update(
            @PathVariable Long id,
            @RequestBody ScheduledReport report) {
        return ResponseEntity.ok(ApiResponse.success(scheduledReportService.update(id, report)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        scheduledReportService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Scheduled report deleted"));
    }

    /**
     * Manually trigger a scheduled report execution.
     */
    @PostMapping("/{id}/run")
    public ResponseEntity<ApiResponse<ScheduledReportOutput>> run(@PathVariable Long id) {
        ScheduledReportOutput output = scheduledReportService.executeReport(id);
        return ResponseEntity.ok(ApiResponse.success(output, "Report executed: " + output.getStatus()));
    }

    /**
     * List past generated outputs for a scheduled report.
     */
    @GetMapping("/{id}/outputs")
    public ResponseEntity<ApiResponse<List<ScheduledReportOutput>>> getOutputs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scheduledReportService.getOutputs(id)));
    }

    /**
     * Download a previously generated report file.
     */
    @GetMapping("/{reportId}/outputs/{outputId}")
    public ResponseEntity<byte[]> downloadOutput(
            @PathVariable Long reportId,
            @PathVariable Long outputId) {
        ScheduledReportOutput output = scheduledReportService.getOutput(outputId);

        File file = new File(output.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
            String fileName = file.getName();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(content);
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
