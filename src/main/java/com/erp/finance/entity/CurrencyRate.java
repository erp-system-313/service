package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Exchange rate for a currency on a specific date.
 */
@Entity
@Table(name = "currency_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "company_id")
    private Long companyId;
}
