package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.customer.*;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public CustomerResponse create(CustomerRequest req, UserPrincipal user) {
        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", user.getCompanyId()));
        Customer customer = Customer.builder()
                .name(req.getName()).email(req.getEmail()).phone(req.getPhone())
                .address(req.getAddress()).city(req.getCity()).state(req.getState())
                .pincode(req.getPincode()).gstin(req.getGstin()).company(company).build();
        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getAll(Long companyId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Customer> customers = search != null && !search.isBlank()
                ? customerRepository.searchByCompany(companyId, search, pageable)
                : customerRepository.findByCompanyId(companyId, pageable);
        return PagedResponse.<CustomerResponse>builder()
                .content(customers.getContent().stream().map(this::mapToResponse).toList())
                .page(customers.getNumber()).size(customers.getSize())
                .totalElements(customers.getTotalElements()).totalPages(customers.getTotalPages())
                .last(customers.isLast()).build();
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest req, UserPrincipal user) {
        Customer c = customerRepository.findByIdAndCompanyId(id, user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        if (req.getName() != null) c.setName(req.getName());
        if (req.getEmail() != null) c.setEmail(req.getEmail());
        if (req.getPhone() != null) c.setPhone(req.getPhone());
        if (req.getAddress() != null) c.setAddress(req.getAddress());
        if (req.getCity() != null) c.setCity(req.getCity());
        if (req.getState() != null) c.setState(req.getState());
        if (req.getPincode() != null) c.setPincode(req.getPincode());
        if (req.getGstin() != null) c.setGstin(req.getGstin());
        return mapToResponse(customerRepository.save(c));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id, Long companyId) {
        return mapToResponse(customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id)));
    }

    private CustomerResponse mapToResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId()).name(c.getName()).email(c.getEmail()).phone(c.getPhone())
                .address(c.getAddress()).city(c.getCity()).state(c.getState())
                .pincode(c.getPincode()).gstin(c.getGstin())
                .companyId(c.getCompany().getId()).active(c.getActive())
                .createdAt(c.getCreatedAt()).build();
    }
}
