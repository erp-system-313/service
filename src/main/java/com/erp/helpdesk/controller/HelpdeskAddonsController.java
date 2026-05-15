package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.helpdesk.dto.*;
import com.erp.helpdesk.service.HelpdeskAddonsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class HelpdeskAddonsController {

    private final HelpdeskAddonsService addonsService;

    // ---- Ticket Ratings ----

    @GetMapping("/tickets/{ticketId}/ratings")
    public ResponseEntity<ApiResponse<List<TicketRatingDto>>> getRatings(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getRatingsByTicket(ticketId)));
    }

    @GetMapping("/tickets/{ticketId}/ratings/average")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getAverageRating(ticketId)));
    }

    @GetMapping("/ratings/overall-average")
    public ResponseEntity<ApiResponse<Double>> getOverallAverageRating() {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getOverallAverageRating()));
    }

    @GetMapping("/ratings/distribution")
    public ResponseEntity<ApiResponse<Map<Integer, Long>>> getRatingDistribution() {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getRatingDistribution()));
    }

    @PostMapping("/tickets/{ticketId}/ratings")
    public ResponseEntity<ApiResponse<TicketRatingDto>> rateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateTicketRatingRequest request) {
        request.setTicketId(ticketId);
        TicketRatingDto rating = addonsService.rateTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rating, "Rating submitted"));
    }

    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRating(@PathVariable Long id) {
        addonsService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Helpdesk Timesheets ----

    @GetMapping("/tickets/{ticketId}/timesheets")
    public ResponseEntity<ApiResponse<List<HelpdeskTimesheetDto>>> getTimesheets(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getTimesheetsByTicket(ticketId)));
    }

    @GetMapping("/timesheets/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<HelpdeskTimesheetDto>>> getEmployeeTimesheets(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                addonsService.getTimesheetsByEmployee(employeeId, from, to)));
    }

    @GetMapping("/tickets/{ticketId}/timesheets/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalHours(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getTotalHours(ticketId)));
    }

    @GetMapping("/tickets/{ticketId}/timesheets/billable")
    public ResponseEntity<ApiResponse<BigDecimal>> getBillableHours(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(addonsService.getBillableHours(ticketId)));
    }

    @PostMapping("/tickets/{ticketId}/timesheets")
    public ResponseEntity<ApiResponse<HelpdeskTimesheetDto>> logTime(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateHelpdeskTimesheetRequest request) {
        request.setTicketId(ticketId);
        HelpdeskTimesheetDto timesheet = addonsService.logTime(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(timesheet, "Time logged"));
    }

    @PutMapping("/timesheets/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTimesheetDto>> updateTime(
            @PathVariable Long id,
            @Valid @RequestBody CreateHelpdeskTimesheetRequest request) {
        HelpdeskTimesheetDto timesheet = addonsService.updateTime(id, request);
        return ResponseEntity.ok(ApiResponse.success(timesheet, "Timesheet updated"));
    }

    @DeleteMapping("/timesheets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimesheet(@PathVariable Long id) {
        addonsService.deleteTimesheet(id);
        return ResponseEntity.noContent().build();
    }
}
