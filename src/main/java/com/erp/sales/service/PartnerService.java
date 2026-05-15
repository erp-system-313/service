package com.erp.sales.service;

import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.sales.dto.CreatePartnerRequest;
import com.erp.sales.dto.PartnerDto;
import com.erp.sales.dto.UpdatePartnerRequest;
import com.erp.sales.entity.Partner;
import com.erp.sales.entity.Partner.PartnerType;
import com.erp.sales.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PageResponse<PartnerDto> findAll(int page, int size, String search, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Partner> partners;
        if (search != null && !search.isBlank()) {
            partners = isActive != null && isActive
                    ? partnerRepository.searchActive(search, pageable)
                    : partnerRepository.searchAll(search, pageable);
        } else if (isActive != null && isActive) {
            partners = partnerRepository.findByIsActiveTrue(pageable);
        } else {
            partners = partnerRepository.findAll(pageable);
        }

        return PageResponse.from(partners.map(PartnerDto::fromEntity));
    }

    public PartnerDto findById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", id));
        return PartnerDto.fromEntity(partner);
    }

    public List<PartnerDto> getContacts(Long parentId) {
        return partnerRepository.findByParentId(parentId).stream()
                .map(PartnerDto::fromEntity)
                .toList();
    }

    @Transactional
    public PartnerDto create(CreatePartnerRequest request) {
        Partner partner = Partner.builder()
                .name(request.getName())
                .type(request.getType() != null ? request.getType() : PartnerType.INDIVIDUAL)
                .email(request.getEmail())
                .phone(request.getPhone())
                .mobile(request.getMobile())
                .website(request.getWebsite())
                .taxId(request.getTaxId())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : java.math.BigDecimal.ZERO)
                .paymentTermId(request.getPaymentTermId())
                .pricelistId(request.getPricelistId())
                .salespersonId(request.getSalespersonId())
                .teamId(request.getTeamId())
                .notes(request.getNotes())
                .build();

        if (request.getParentId() != null) {
            Partner parent = partnerRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getParentId()));
            partner.setParent(parent);
        }

        partner = partnerRepository.save(partner);
        log.info("Created partner with id: {} type: {}", partner.getId(), partner.getType());
        return PartnerDto.fromEntity(partner);
    }

    @Transactional
    public PartnerDto update(Long id, UpdatePartnerRequest request) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", id));

        if (request.getName() != null) partner.setName(request.getName());
        if (request.getType() != null) partner.setType(request.getType());
        if (request.getEmail() != null) partner.setEmail(request.getEmail());
        if (request.getPhone() != null) partner.setPhone(request.getPhone());
        if (request.getMobile() != null) partner.setMobile(request.getMobile());
        if (request.getWebsite() != null) partner.setWebsite(request.getWebsite());
        if (request.getTaxId() != null) partner.setTaxId(request.getTaxId());
        if (request.getAddress() != null) partner.setAddress(request.getAddress());
        if (request.getCity() != null) partner.setCity(request.getCity());
        if (request.getState() != null) partner.setState(request.getState());
        if (request.getZipCode() != null) partner.setZipCode(request.getZipCode());
        if (request.getCountry() != null) partner.setCountry(request.getCountry());
        if (request.getIsActive() != null) partner.setIsActive(request.getIsActive());
        if (request.getCreditLimit() != null) partner.setCreditLimit(request.getCreditLimit());
        if (request.getPaymentTermId() != null) partner.setPaymentTermId(request.getPaymentTermId());
        if (request.getPricelistId() != null) partner.setPricelistId(request.getPricelistId());
        if (request.getSalespersonId() != null) partner.setSalespersonId(request.getSalespersonId());
        if (request.getTeamId() != null) partner.setTeamId(request.getTeamId());
        if (request.getNotes() != null) partner.setNotes(request.getNotes());

        if (request.getParentId() != null) {
            Partner parent = partnerRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getParentId()));
            partner.setParent(parent);
        } else if (request.getParentId() == null && partner.getParent() != null) {
            // Allow unsetting parent by omitting parentId (not passing it means no change)
        }

        partner = partnerRepository.save(partner);
        log.info("Updated partner with id: {}", id);
        return PartnerDto.fromEntity(partner);
    }

    @Transactional
    public void delete(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", id));
        partner.setIsActive(false);
        partnerRepository.save(partner);
        log.info("Soft-deleted partner with id: {}", id);
    }
}
