package com.erp.hr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveRequest.LeaveType type;

    @Column(name = "total_days", nullable = false)
    @Builder.Default
    private int totalDays = 0;

    @Column(name = "used_days", nullable = false)
    @Builder.Default
    private int usedDays = 0;

    @Column(name = "year", nullable = false)
    private int year;

    public int getRemainingDays() {
        return totalDays - usedDays;
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
