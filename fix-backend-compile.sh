#!/usr/bin/env bash
set -euo pipefail

# Run this from the service repo root:
#   cd ~/Projects/Software\ Eng/service
#   bash fix-backend-compile.sh
#
# It rewrites the mismatched Java files and adds one Flyway migration.

ROOT="${1:-.}"
cd "$ROOT"

mkdir -p src/main/java/com/erp/helpdesk/entity
mkdir -p src/main/java/com/erp/helpdesk/dto
mkdir -p src/main/java/com/erp/helpdesk/repository
mkdir -p src/main/java/com/erp/finance/entity
mkdir -p src/main/java/com/erp/finance/repository
mkdir -p src/main/resources/db/migration

cat > src/main/java/com/erp/helpdesk/entity/HelpdeskTeam.java <<'JAVA'
package com.erp.helpdesk.entity;

import com.erp.admin.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "helpdesk_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "alias_name", length = 100)
    private String aliasName;

    @Column(name = "alias_domain", length = 200)
    private String aliasDomain;

    @Column(name = "use_alias")
    @Builder.Default
    private Boolean useAlias = false;

    @Column(name = "default_stage", length = 50)
    private String defaultStage;

    @Column(name = "team_lead_id")
    private Long teamLeadId;

    @Column(name = "team_lead_name", length = 255)
    private String teamLeadName;

    @Column(name = "default_priority", length = 1)
    @Builder.Default
    private String defaultPriority = "0";

    @Column(name = "auto_assign")
    @Builder.Default
    private Boolean autoAssign = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany
    @JoinTable(
        name = "helpdesk_team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
JAVA

cat > src/main/java/com/erp/helpdesk/dto/CreateHelpdeskTeamRequest.java <<'JAVA'
package com.erp.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHelpdeskTeamRequest {
    @NotBlank(message = "Team name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String aliasName;

    @Size(max = 200)
    private String aliasDomain;

    private Boolean useAlias;

    @Size(max = 50)
    private String defaultStage;

    private Long teamLeadId;

    @Size(max = 255)
    private String teamLeadName;

    @Size(max = 1)
    private String defaultPriority;

    private Boolean autoAssign;

    private String description;

    private Boolean isActive;

    private Set<Long> memberIds;
}
JAVA

cat > src/main/java/com/erp/helpdesk/dto/HelpdeskTeamDto.java <<'JAVA'
package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.HelpdeskTeam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTeamDto {
    private Long id;
    private String name;
    private String aliasName;
    private String aliasDomain;
    private Boolean useAlias;
    private String defaultStage;
    private Long teamLeadId;
    private String teamLeadName;
    private String defaultPriority;
    private Boolean autoAssign;
    private String description;
    private Set<Long> memberIds;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HelpdeskTeamDto fromEntity(HelpdeskTeam team) {
        return HelpdeskTeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .aliasName(team.getAliasName())
                .aliasDomain(team.getAliasDomain())
                .useAlias(team.getUseAlias())
                .defaultStage(team.getDefaultStage())
                .teamLeadId(team.getTeamLeadId())
                .teamLeadName(team.getTeamLeadName())
                .defaultPriority(team.getDefaultPriority())
                .autoAssign(team.getAutoAssign())
                .description(team.getDescription())
                .memberIds(team.getMembers() != null
                        ? team.getMembers().stream().map(u -> u.getId()).collect(Collectors.toSet())
                        : null)
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
JAVA

cat > src/main/java/com/erp/helpdesk/repository/HelpdeskTeamRepository.java <<'JAVA'
package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskTeamRepository extends JpaRepository<HelpdeskTeam, Long> {
    Page<HelpdeskTeam> findByIsActiveTrue(Pageable pageable);

    List<HelpdeskTeam> findByIsActiveTrue();
}
JAVA

cat > src/main/java/com/erp/finance/entity/Invoice.java <<'JAVA'
package com.erp.finance.entity;

import com.erp.sales.entity.Customer;
import com.erp.sales.entity.SalesOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLine> lines = new ArrayList<>();

    // Payments are now handled via the new Move-based system.
    // This field is kept for backward compatibility but not mapped via JPA.
    @Transient
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    public void addLine(InvoiceLine line) {
        lines.add(line);
        line.setInvoice(this);
    }

    public void clearLines() {
        lines.clear();
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    public void calculatePaidAmount() {
        if (payments != null && !payments.isEmpty()) {
            this.paidAmount = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public BigDecimal getBalance() {
        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return total.subtract(paid);
    }

    public BigDecimal getTotal() {
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public void setTotal(BigDecimal total) {
        this.totalAmount = total;
    }
}
JAVA

cat > src/main/java/com/erp/finance/repository/InvoiceRepository.java <<'JAVA'
package com.erp.finance.repository;

import com.erp.finance.entity.Invoice;
import com.erp.finance.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("""
            SELECT i FROM Invoice i
            LEFT JOIN i.customer c
            WHERE LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Invoice> search(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT i FROM Invoice i
            WHERE (:status IS NULL OR i.status = :status)
              AND (:customerId IS NULL OR i.customer.id = :customerId)
              AND (:dateFrom IS NULL OR i.invoiceDate >= :dateFrom)
              AND (:dateTo IS NULL OR i.invoiceDate <= :dateTo)
            """)
    Page<Invoice> findWithFilters(
            @Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    @EntityGraph(attributePaths = {"lines", "salesOrder", "customer"})
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdWithPayments(@Param("id") Long id);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    long countByStatus(InvoiceStatus status);
}
JAVA

python3 - <<'PY'
from pathlib import Path
import re

sales_service = Path("src/main/java/com/erp/sales/service/SalesOrderService.java")
s = sales_service.read_text()

s = s.replace("import com.erp.finance.entity.Incoterm;", "import com.erp.sales.entity.Incoterm;")
s = s.replace("import com.erp.finance.repository.IncotermRepository;", "import com.erp.sales.repository.IncotermRepository;")

# Add the controller's `search` argument to the service method signature; this keeps search ignored for now.
s = re.sub(
    r"public\s+PageResponse<\s*SalesOrderDto\s*>\s+findAll\s*\(\s*int\s+page\s*,\s*int\s+size\s*,\s*OrderStatus\s+status\s*,",
    "public PageResponse<SalesOrderDto> findAll(int page, int size, String search, OrderStatus status,",
    s,
)
s = re.sub(
    r"public\s+PageResponse\s+findAll\s*\(\s*int\s+page\s*,\s*int\s+size\s*,\s*OrderStatus\s+status\s*,",
    "public PageResponse<SalesOrderDto> findAll(int page, int size, String search, OrderStatus status,",
    s,
)

# Fix BigDecimal math where quantity is Integer.
s = s.replace(
    "lineRequest.getUnitPrice().multiply(lineRequest.getQuantity())",
    "lineRequest.getUnitPrice().multiply(BigDecimal.valueOf(lineRequest.getQuantity()))",
)

# MoveLine.quantity is BigDecimal, while SalesOrderLine.quantity is Integer.
s = s.replace(
    "moveLine.setQuantity(line.getQuantity());",
    "moveLine.setQuantity(BigDecimal.valueOf(line.getQuantity()));",
)

sales_service.write_text(s)

invoice_service = Path("src/main/java/com/erp/finance/service/InvoiceService.java")
s = invoice_service.read_text()
s = s.replace(".total(BigDecimal.ZERO)", ".totalAmount(BigDecimal.ZERO)")
s = s.replace(".date(request.getPaymentDate())", ".date(request.getPaymentDate().toLocalDate())")
invoice_service.write_text(s)
PY

cat > src/main/resources/db/migration/V42__repair_invoice_sales_order_fk.sql <<'SQL'
-- Adds the invoice -> sales order column expected by Invoice.java / InvoiceService.java.
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS sales_order_id BIGINT;

DO $$
BEGIN
    ALTER TABLE invoices
        ADD CONSTRAINT fk_invoices_sales_order
        FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders(id);
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'constraint fk_invoices_sales_order already exists';
END $$;

CREATE INDEX IF NOT EXISTS idx_invoices_sales_order_id ON invoices(sales_order_id);
SQL

echo
echo "Patch files written."
echo "Now run:"
echo "  mvn clean compile"
echo
echo "If compile passes, run:"
echo "  mvn spring-boot:run"
