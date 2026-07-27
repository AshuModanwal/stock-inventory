package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.company.*;
import com.stockpilot.inventory.entity.Company;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        if (request.getEmail() != null && companyRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Company with email already exists");
        }

        Company company = Company.builder()
                .name(request.getName())
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .gstin(request.getGstin())
                .pan(request.getPan())
                .website(request.getWebsite())
                .invoicePrefix(request.getInvoicePrefix() != null ? request.getInvoicePrefix() : "INV")
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : 18.0)
                .build();

        return mapToResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(Long id) {
        return mapToResponse(companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<CompanyResponse> getAllCompanies(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Company> companies = search != null && !search.isBlank()
                ? companyRepository.search(search, pageable)
                : companyRepository.findAll(pageable);

        return PagedResponse.<CompanyResponse>builder()
                .content(companies.getContent().stream().map(this::mapToResponse).toList())
                .page(companies.getNumber())
                .size(companies.getSize())
                .totalElements(companies.getTotalElements())
                .totalPages(companies.getTotalPages())
                .last(companies.isLast())
                .build();
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        if (request.getName() != null) company.setName(request.getName());
        if (request.getDisplayName() != null) company.setDisplayName(request.getDisplayName());
        if (request.getEmail() != null) company.setEmail(request.getEmail());
        if (request.getPhone() != null) company.setPhone(request.getPhone());
        if (request.getAddress() != null) company.setAddress(request.getAddress());
        if (request.getCity() != null) company.setCity(request.getCity());
        if (request.getState() != null) company.setState(request.getState());
        if (request.getPincode() != null) company.setPincode(request.getPincode());
        if (request.getGstin() != null) company.setGstin(request.getGstin());
        if (request.getPan() != null) company.setPan(request.getPan());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getInvoicePrefix() != null) company.setInvoicePrefix(request.getInvoicePrefix());
        if (request.getTaxRate() != null) company.setTaxRate(request.getTaxRate());

        return mapToResponse(companyRepository.save(company));
    }

    @Transactional
    public void toggleActive(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        company.setActive(!company.getActive());
        companyRepository.save(company);
    }

    public CompanyResponse mapToResponse(Company c) {
        return CompanyResponse.builder()
                .id(c.getId()).name(c.getName()).displayName(c.getDisplayName())
                .email(c.getEmail()).phone(c.getPhone()).address(c.getAddress())
                .city(c.getCity()).state(c.getState()).pincode(c.getPincode())
                .gstin(c.getGstin()).pan(c.getPan()).logo(c.getLogo())
                .website(c.getWebsite()).invoicePrefix(c.getInvoicePrefix())
                .taxRate(c.getTaxRate()).active(c.getActive()).createdAt(c.getCreatedAt())
                .build();
    }
}
