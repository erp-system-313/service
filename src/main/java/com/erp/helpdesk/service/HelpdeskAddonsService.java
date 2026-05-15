package com.erp.helpdesk.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.*;
import com.erp.helpdesk.entity.HelpdeskTimesheet;
import com.erp.helpdesk.entity.Ticket;
import com.erp.helpdesk.entity.TicketRating;
import com.erp.helpdesk.repository.HelpdeskTimesheetRepository;
import com.erp.helpdesk.repository.TicketRatingRepository;
import com.erp.helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpdeskAddonsService {

    private final TicketRatingRepository ticketRatingRepository;
    private final HelpdeskTimesheetRepository timesheetRepository;
    private final TicketRepository ticketRepository;

    // ---- Ticket Ratings ----

    @Transactional(readOnly = true)
    public List<TicketRatingDto> getRatingsByTicket(Long ticketId) {
        return ticketRatingRepository.findByTicketId(ticketId)
                .stream().map(TicketRatingDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long ticketId) {
        return ticketRatingRepository.averageRatingByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public Double getOverallAverageRating() {
        return ticketRatingRepository.averageRatingOverall();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistribution() {
        return ticketRatingRepository.countByRating().stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Transactional
    public TicketRatingDto rateTicket(CreateTicketRatingRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", request.getTicketId()));

        TicketRating rating = TicketRating.builder()
                .ticket(ticket)
                .rating(request.getRating())
                .comment(request.getComment())
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .viaEmail(request.getViaEmail() != null ? request.getViaEmail() : false)
                .build();

        ticketRatingRepository.save(rating);
        log.info("Ticket {} rated {} stars", request.getTicketId(), request.getRating());
        return TicketRatingDto.fromEntity(rating);
    }

    @Transactional
    public void deleteRating(Long id) {
        ticketRatingRepository.deleteById(id);
        log.info("Deleted rating: {}", id);
    }

    // ---- Helpdesk Timesheets ----

    @Transactional(readOnly = true)
    public List<HelpdeskTimesheetDto> getTimesheetsByTicket(Long ticketId) {
        return timesheetRepository.findByTicketIdOrderByDateDesc(ticketId)
                .stream().map(HelpdeskTimesheetDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<HelpdeskTimesheetDto> getTimesheetsByEmployee(Long employeeId, LocalDate from, LocalDate to) {
        List<HelpdeskTimesheet> timesheets;
        if (from != null && to != null) {
            timesheets = timesheetRepository.findByEmployeeIdAndDateBetween(employeeId, from, to);
        } else {
            timesheets = timesheetRepository.findByEmployeeId(employeeId);
        }
        return timesheets.stream().map(HelpdeskTimesheetDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalHours(Long ticketId) {
        return timesheetRepository.sumHoursByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBillableHours(Long ticketId) {
        return timesheetRepository.sumBillableHoursByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> getHoursByEmployee(LocalDate from, LocalDate to) {
        return timesheetRepository.sumHoursByEmployeeBetween(from, to).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    @Transactional
    public HelpdeskTimesheetDto logTime(CreateHelpdeskTimesheetRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", request.getTicketId()));

        HelpdeskTimesheet timesheet = HelpdeskTimesheet.builder()
                .ticket(ticket)
                .employeeId(request.getEmployeeId())
                .employeeName(request.getEmployeeName())
                .date(request.getDate())
                .description(request.getDescription())
                .unitAmount(request.getUnitAmount())
                .isBillable(request.getIsBillable() != null ? request.getIsBillable() : true)
                .soLineId(request.getSoLineId())
                .build();

        timesheetRepository.save(timesheet);
        log.info("Logged {} hours on ticket {}", request.getUnitAmount(), request.getTicketId());
        return HelpdeskTimesheetDto.fromEntity(timesheet);
    }

    @Transactional
    public HelpdeskTimesheetDto updateTime(Long id, CreateHelpdeskTimesheetRequest request) {
        HelpdeskTimesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTimesheet", id));

        if (request.getDate() != null) timesheet.setDate(request.getDate());
        if (request.getDescription() != null) timesheet.setDescription(request.getDescription());
        if (request.getUnitAmount() != null) timesheet.setUnitAmount(request.getUnitAmount());
        if (request.getIsBillable() != null) timesheet.setIsBillable(request.getIsBillable());
        if (request.getSoLineId() != null) timesheet.setSoLineId(request.getSoLineId());

        timesheetRepository.save(timesheet);
        return HelpdeskTimesheetDto.fromEntity(timesheet);
    }

    @Transactional
    public void deleteTimesheet(Long id) {
        timesheetRepository.deleteById(id);
        log.info("Deleted timesheet: {}", id);
    }
}
