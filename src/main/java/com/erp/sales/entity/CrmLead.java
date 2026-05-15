package com.erp.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CRM Lead — potential sales opportunity (unqualified or qualified).
 * Similar to Odoo's crm.lead. Combines leads and opportunities in one model.
 */
@Entity
@Table(name = "crm_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lead/opportunity name (e.g., "Website redesign for Acme Corp"). */
    @Column(nullable = false, length = 255)
    private String name;

    /** Lead type. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeadType type = LeadType.LEAD;

    /** Pipeline stage. */
    @Column(name = "stage", length = 50)
    @Builder.Default
    private String stage = "NEW";

    /** Lead priority: 0=low, 1=medium, 2=high, 3=urgent. */
    @Column(length = 1)
    @Builder.Default
    private String priority = "0";

    /** Expected revenue. */
    @Column(name = "expected_revenue", precision = 12, scale = 2)
    private BigDecimal expectedRevenue;

    /** Probability of closing (percentage). */
    @Column(precision = 5, scale = 2)
    private BigDecimal probability;

    /** Expected closing date. */
    @Column(name = "expected_closing")
    private LocalDate expectedClosing;

    /** Partner/customer ID. */
    @Column(name = "partner_id")
    private Long partnerId;

    /** Partner name. */
    @Column(name = "partner_name", length = 255)
    private String partnerName;

    /** Contact name. */
    @Column(name = "contact_name", length = 255)
    private String contactName;

    /** Contact email. */
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    /** Contact phone. */
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    /** Company name (if different from partner). */
    @Column(name = "company_name", length = 255)
    private String companyName;

    /** Lead source (website, referral, trade show, etc.). */
    @Column(name = "source", length = 100)
    private String source;

    /** Lead medium (email, phone, social, etc.). */
    @Column(name = "medium", length = 100)
    private String medium;

    /** Campaign name. */
    @Column(name = "campaign", length = 200)
    private String campaign;

    /** Assigned salesperson. */
    @Column(name = "user_id")
    private Long userId;

    /** Salesperson name. */
    @Column(name = "user_name", length = 255)
    private String userName;

    /** Sales team. */
    @Column(name = "team_id")
    private Long teamId;

    /** Sales team name. */
    @Column(name = "team_name", length = 255)
    private String teamName;

    /** Country. */
    @Column(name = "country_id")
    private Long countryId;

    /** Description / notes. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Converted to opportunity date. */
    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    /** Converted to quotation (sales order) ID. */
    @Column(name = "converted_to_order_id")
    private Long convertedToOrderId;

    /** Lost reason. */
    @Column(name = "lost_reason", length = 500)
    private String lostReason;

    /** Date marked as lost. */
    @Column(name = "lost_at")
    private LocalDateTime lostAt;

    /** Tag names (comma-separated). */
    @Column(name = "tags", length = 500)
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Activity date (next follow-up). */
    @Column(name = "activity_date")
    private LocalDate activityDate;

    /** Activity summary. */
    @Column(name = "activity_summary", length = 500)
    private String activitySummary;

    public enum LeadType {
        LEAD, OPPORTUNITY
    }
}
