package com.erp.sales.service;

import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.sales.dto.*;
import com.erp.sales.entity.PriceList;
import com.erp.sales.entity.PriceListItem;
import com.erp.sales.repository.PriceListItemRepository;
import com.erp.sales.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceListService {

    private final PriceListRepository priceListRepository;
    private final PriceListItemRepository priceListItemRepository;

    // ---- PriceList CRUD ----

    public PageResponse<PriceListDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<PriceList> priceLists = priceListRepository.findByIsActiveTrue(pageable);
        return PageResponse.from(priceLists.map(PriceListDto::fromEntity));
    }

    public PriceListDto findById(Long id) {
        PriceList priceList = priceListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", id));
        return PriceListDto.fromEntity(priceList);
    }

    @Transactional
    public PriceListDto create(CreatePriceListRequest request) {
        PriceList priceList = PriceList.builder()
                .name(request.getName())
                .currencyId(request.getCurrencyId())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .build();

        priceList = priceListRepository.save(priceList);
        log.info("Created price list with id: {}", priceList.getId());
        return PriceListDto.fromEntity(priceList);
    }

    @Transactional
    public PriceListDto update(Long id, CreatePriceListRequest request) {
        PriceList priceList = priceListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", id));

        priceList.setName(request.getName());
        priceList.setCurrencyId(request.getCurrencyId());
        priceList.setValidFrom(request.getValidFrom());
        priceList.setValidTo(request.getValidTo());

        priceList = priceListRepository.save(priceList);
        log.info("Updated price list with id: {}", id);
        return PriceListDto.fromEntity(priceList);
    }

    @Transactional
    public void delete(Long id) {
        PriceList priceList = priceListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", id));
        priceList.setIsActive(false);
        priceListRepository.save(priceList);
        log.info("Soft-deleted price list with id: {}", id);
    }

    // ---- PriceListItem CRUD ----

    public List<PriceListItemDto> getItems(Long priceListId) {
        return priceListItemRepository.findByPriceListId(priceListId).stream()
                .map(PriceListItemDto::fromEntity)
                .toList();
    }

    @Transactional
    public PriceListItemDto addItem(Long priceListId, CreatePriceListItemRequest request) {
        PriceList priceList = priceListRepository.findById(priceListId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", priceListId));

        PriceListItem item = PriceListItem.builder()
                .priceList(priceList)
                .productId(request.getProductId())
                .minQuantity(request.getMinQuantity() != null ? request.getMinQuantity() : BigDecimal.ONE)
                .fixedPrice(request.getFixedPrice())
                .discountPercent(request.getDiscountPercent())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .build();

        item = priceListItemRepository.save(item);
        log.info("Added item {} to price list {}", item.getId(), priceListId);
        return PriceListItemDto.fromEntity(item);
    }

    @Transactional
    public void removeItem(Long priceListId, Long itemId) {
        PriceListItem item = priceListItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceListItem", itemId));
        if (!item.getPriceList().getId().equals(priceListId)) {
            throw new IllegalArgumentException("Item does not belong to this price list");
        }
        priceListItemRepository.delete(item);
        log.info("Removed item {} from price list {}", itemId, priceListId);
    }

    // ---- Price Computation ----

    /**
     * Compute the price for a product given a pricelist, quantity, and date.
     * Returns null if no matching rule is found.
     */
    @Transactional(readOnly = true)
    public PriceResult computePrice(Long priceListId, Long productId, BigDecimal quantity, LocalDate date) {
        if (date == null) date = LocalDate.now();
        if (quantity == null) quantity = BigDecimal.ONE;

        List<PriceListItem> rules = priceListItemRepository.findMatchingRules(
                priceListId, productId, quantity, date);

        if (rules.isEmpty()) {
            return null; // No matching rule; caller should use base price
        }

        // Take the first (most specific) rule — ordered by minQuantity DESC
        PriceListItem rule = rules.get(0);

        BigDecimal basePrice = rule.getFixedPrice();
        BigDecimal discount = rule.getDiscountPercent();

        return new PriceResult(basePrice, discount, rule.getId());
    }

    public record PriceResult(BigDecimal basePrice, BigDecimal discountPercent, Long ruleId) {}
}
