package com.erp.purchasing.service;

import com.erp.admin.service.AuditLogService;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.purchasing.dto.CreateSupplierRequest;
import com.erp.purchasing.dto.SupplierDto;
import com.erp.purchasing.dto.UpdateSupplierRequest;
import com.erp.purchasing.entity.Supplier;
import com.erp.purchasing.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    public PageResponse<SupplierDto> findAll(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Supplier> suppliers;
        if (status != null) {
            Supplier.Status supplierStatus = Supplier.Status.valueOf(status.toUpperCase());
            suppliers = supplierRepository.findByStatus(supplierStatus, pageable);
        } else {
            suppliers = supplierRepository.findAll(pageable);
        }

        return PageResponse.from(suppliers.map(this::toDto));
    }

    public SupplierDto findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        return toDto(supplier);
    }

    @Transactional
    public SupplierDto create(CreateSupplierRequest request, Long currentUserId, String ipAddress) {
        if (supplierRepository.existsByName(request.getName())) {
            throw new BusinessException("SUPPLIER_001", "Supplier name already exists");
        }
        if (supplierRepository.existsByCode(request.getCode())) {
            throw new BusinessException("SUPPLIER_002", "Supplier code already exists");
        }

        Supplier supplier = Supplier.builder()
                .code(request.getCode())
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .taxId(request.getTaxId())
                .paymentTerms(request.getPaymentTerms())
                .status(Supplier.Status.ACTIVE)
                .build();

        supplier = supplierRepository.save(supplier);
        log.info("Created supplier with id: {} and name: {}", supplier.getId(), request.getName());

        auditLogService.log(null, "CREATE", "Supplier", supplier.getId(), null, ipAddress, "Supplier created");

        return toDto(supplier);
    }

    @Transactional
    public SupplierDto update(Long id, UpdateSupplierRequest request, Long currentUserId, String ipAddress) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        if (request.getName() != null && !request.getName().equals(supplier.getName())) {
            if (supplierRepository.existsByName(request.getName())) {
                throw new BusinessException("SUPPLIER_001", "Supplier name already exists");
            }
            supplier.setName(request.getName());
        }

        if (request.getCode() != null) supplier.setCode(request.getCode());
        if (request.getContactPerson() != null) supplier.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null) supplier.setEmail(request.getEmail());
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        if (request.getTaxId() != null) supplier.setTaxId(request.getTaxId());
        if (request.getPaymentTerms() != null) supplier.setPaymentTerms(request.getPaymentTerms());

        supplier = supplierRepository.save(supplier);
        log.info("Updated supplier with id: {}", supplier.getId());

        auditLogService.log(null, "UPDATE", "Supplier", supplier.getId(), null, ipAddress, "Supplier updated");

        return toDto(supplier);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        supplier.setStatus(Supplier.Status.INACTIVE);
        supplierRepository.save(supplier);
        log.info("Deactivated supplier with id: {}", id);

        auditLogService.log(null, "DELETE", "Supplier", id, null, ipAddress, "Supplier deactivated");
    }

    public long countActive() {
        return supplierRepository.countByStatus(Supplier.Status.ACTIVE);
    }

    private SupplierDto toDto(Supplier supplier) {
        return SupplierDto.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .taxId(supplier.getTaxId())
                .paymentTerms(supplier.getPaymentTerms())
                .totalPurchased(supplier.getTotalPurchased())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
