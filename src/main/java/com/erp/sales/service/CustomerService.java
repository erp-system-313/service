package com.erp.sales.service;

import com.erp.sales.dto.CreateCustomerRequest;
import com.erp.sales.dto.CustomerDto;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.UpdateCustomerRequest;
import com.erp.sales.entity.Customer;
import com.erp.sales.repository.CustomerRepository;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;

    public PageResponse<CustomerDto> findAll(int page, int size, String search, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Customer> customers;
        if (search != null && !search.isBlank()) {
            customers = customerRepository.searchByNameOrEmail(search, pageable);
        } else if (isActive != null) {
            customers = customerRepository.findByIsActive(isActive, pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        return PageResponse.from(customers.map(this::toDto));
    }

    public CustomerDto findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return toDto(customer);
    }

    public List<CustomerDto> findActiveCustomers() {
        return customerRepository.findByIsActiveTrue().stream()
                .map(this::toDto)
                .toList();
    }

    public List<SalesOrderDto> getCustomerOrders(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        
        return salesOrderRepository.findByCustomerId(customerId, PageRequest.of(0, 100, Sort.by("createdAt").descending()))
                .getContent()
                .stream()
                .map(SalesOrderDto::fromEntity)
                .toList();
    }

    @Transactional
    public CustomerDto create(CreateCustomerRequest request) {
        if (request.getEmail() != null && customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("CUSTOMER_001", "Email already exists");
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .creditLimit(request.getCreditLimit())
                .paymentTerms(request.getPaymentTerms())
                .isActive(true)
                .build();

        customer = customerRepository.save(customer);
        log.info("Created customer with id: {}", customer.getId());

        return toDto(customer);
    }

    @Transactional
    public CustomerDto update(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(customer.getEmail()) 
                    && customerRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BusinessException("CUSTOMER_001", "Email already exists");
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getCreditLimit() != null) customer.setCreditLimit(request.getCreditLimit());
        if (request.getPaymentTerms() != null) customer.setPaymentTerms(request.getPaymentTerms());
        if (request.getIsActive() != null) customer.setIsActive(request.getIsActive());

        customer = customerRepository.save(customer);
        log.info("Updated customer with id: {}", customer.getId());

        return toDto(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        customer.setIsActive(false);
        customerRepository.save(customer);
        log.info("Soft deleted customer with id: {}", id);
    }

    private CustomerDto toDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .creditLimit(customer.getCreditLimit())
                .paymentTerms(customer.getPaymentTerms())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}