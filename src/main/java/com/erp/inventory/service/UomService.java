package com.erp.inventory.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.inventory.entity.Uom;
import com.erp.inventory.entity.UomCategory;
import com.erp.inventory.entity.UomType;
import com.erp.inventory.repository.UomCategoryRepository;
import com.erp.inventory.repository.UomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UomService {

    private final UomRepository uomRepository;
    private final UomCategoryRepository uomCategoryRepository;

    // ---- Categories ----

    @Transactional(readOnly = true)
    public List<UomCategory> findAllCategories() {
        return uomCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UomCategory findCategoryById(Long id) {
        return uomCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UomCategory", id));
    }

    @Transactional
    public UomCategory createCategory(String name) {
        if (uomCategoryRepository.existsByName(name)) {
            throw new BusinessException("UOM_001", "UoM category '" + name + "' already exists");
        }
        UomCategory category = UomCategory.builder().name(name).build();
        uomCategoryRepository.save(category);
        log.info("Created UoM category: {}", name);
        return category;
    }

    // ---- UoMs ----

    @Transactional(readOnly = true)
    public List<Uom> findAllActive() {
        return uomRepository.findAllActiveWithCategory();
    }

    @Transactional(readOnly = true)
    public Uom findById(Long id) {
        return uomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Uom", id));
    }

    @Transactional(readOnly = true)
    public List<Uom> findByCategory(Long categoryId) {
        return uomRepository.findByCategoryActive(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Uom> findByType(UomType type) {
        return uomRepository.findByUomType(type);
    }

    @Transactional
    public Uom create(String name, String code, Long categoryId,
                       BigDecimal factor, Boolean isReference, UomType type) {
        if (code != null && uomRepository.existsByCode(code)) {
            throw new BusinessException("UOM_002", "UoM with code '" + code + "' already exists");
        }

        UomCategory category = uomCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("UomCategory", categoryId));

        Uom uom = Uom.builder()
                .name(name)
                .code(code)
                .category(category)
                .factor(factor != null ? factor : BigDecimal.ONE)
                .isReference(isReference != null ? isReference : false)
                .uomType(type != null ? type : UomType.UNIT)
                .active(true)
                .build();

        uomRepository.save(uom);
        log.info("Created UoM: {} ({})", name, code);
        return uom;
    }

    @Transactional
    public Uom update(Long id, String name, String code, BigDecimal factor, Boolean active) {
        Uom uom = findById(id);
        if (name != null) uom.setName(name);
        if (code != null) {
            if (!code.equals(uom.getCode()) && uomRepository.existsByCode(code)) {
                throw new BusinessException("UOM_002", "UoM with code '" + code + "' already exists");
            }
            uom.setCode(code);
        }
        if (factor != null) uom.setFactor(factor);
        if (active != null) uom.setActive(active);
        uomRepository.save(uom);
        return uom;
    }

    // ---- Conversion ----

    @Transactional(readOnly = true)
    public BigDecimal convert(BigDecimal quantity, Long fromUomId, Long toUomId) {
        Uom from = findById(fromUomId);
        Uom to = findById(toUomId);
        return from.convert(quantity, to);
    }
}
