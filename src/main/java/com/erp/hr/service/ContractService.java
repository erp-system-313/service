package com.erp.hr.service;

import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.hr.dto.ContractDto;
import com.erp.hr.dto.CreateContractRequest;
import com.erp.hr.dto.UpdateContractRequest;
import com.erp.hr.entity.Contract;
import com.erp.hr.entity.Employee;
import com.erp.hr.repository.ContractRepository;
import com.erp.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public PageResponse<ContractDto> findAll(int page, int size, Long employeeId, String status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Contract> contracts;
        if (employeeId != null && status != null) {
            contracts = contractRepository.findByEmployeeIdAndStatus(employeeId,
                    Contract.ContractStatus.valueOf(status.toUpperCase()), pageable);
        } else if (employeeId != null) {
            contracts = contractRepository.findByEmployeeId(employeeId, pageable);
        } else if (status != null) {
            contracts = contractRepository.findByStatus(
                    Contract.ContractStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            contracts = contractRepository.findAll(pageable);
        }

        return PageResponse.from(contracts.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public ContractDto findById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        return toDto(contract);
    }

    @Transactional
    public ContractDto create(CreateContractRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));

        Contract contract = Contract.builder()
                .employee(employee)
                .type(Contract.ContractType.valueOf(request.getType().toUpperCase()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .wage(request.getWage())
                .benefits(request.getBenefits())
                .build();

        contract = contractRepository.save(contract);
        log.info("Created contract with id: {} for employee id: {}", contract.getId(), employee.getId());
        return toDto(contract);
    }

    @Transactional
    public ContractDto update(Long id, UpdateContractRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));

        if (request.getType() != null) {
            contract.setType(Contract.ContractType.valueOf(request.getType().toUpperCase()));
        }
        if (request.getStartDate() != null) {
            contract.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }
        if (request.getWage() != null) {
            contract.setWage(request.getWage());
        }
        if (request.getBenefits() != null) {
            contract.setBenefits(request.getBenefits());
        }
        if (request.getStatus() != null) {
            contract.setStatus(Contract.ContractStatus.valueOf(request.getStatus().toUpperCase()));
        }

        contract = contractRepository.save(contract);
        log.info("Updated contract with id: {}", contract.getId());
        return toDto(contract);
    }

    @Transactional
    public void delete(Long id) {
        if (!contractRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contract", id);
        }
        contractRepository.deleteById(id);
        log.info("Deleted contract with id: {}", id);
    }

    private ContractDto toDto(Contract contract) {
        return ContractDto.builder()
                .id(contract.getId())
                .employeeId(contract.getEmployee().getId())
                .employeeName(contract.getEmployee().getFullName())
                .type(contract.getType().name())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .wage(contract.getWage())
                .benefits(contract.getBenefits())
                .status(contract.getStatus().name())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}
