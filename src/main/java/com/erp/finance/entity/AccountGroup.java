package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Hierarchical grouping of accounts by code prefix ranges.
 * Example: Assets (code 1-19999), Current Assets (1000-1299), etc.
 */
@Entity
@Table(name = "account_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "code_prefix_from", length = 10)
    private String codePrefixFrom;

    @Column(name = "code_prefix_to", length = 10)
    private String codePrefixTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AccountGroup parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountGroup> children = new ArrayList<>();

    @Column(name = "sequence")
    private Integer sequence;
}
