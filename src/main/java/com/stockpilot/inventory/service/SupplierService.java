package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.supplier.*;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public SupplierResponse create(SupplierRequest req, UserPrincipal user) {
        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", user.getCompanyId()));
        Supplier supplier = Supplier.builder()
                .name(req.getName()).email(req.getEmail()).phone(req.getPhone())
                .address(req.getAddress()).city(req.getCity()).state(req.getState())
                .pincode(req.getPincode()).gstin(req.getGstin())
                .contactPerson(req.getContactPerson()).company(company).build();
        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplierResponse> getAll(Long companyId, int page, int size) {
        Page<Supplier> suppliers = supplierRepository.findByCompanyId(companyId, PageRequest.of(page, size, Sort.by("name")));
        return PagedResponse.<SupplierResponse>builder()
                .content(suppliers.getContent().stream().map(this::mapToResponse).toList())
                .page(suppliers.getNumber()).size(suppliers.getSize())
                .totalElements(suppliers.getTotalElements()).totalPages(suppliers.getTotalPages())
                .last(suppliers.isLast()).build();
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest req, UserPrincipal user) {
        Supplier s = supplierRepository.findByIdAndCompanyId(id, user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        if (req.getName() != null) s.setName(req.getName());
        if (req.getEmail() != null) s.setEmail(req.getEmail());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getAddress() != null) s.setAddress(req.getAddress());
        if (req.getContactPerson() != null) s.setContactPerson(req.getContactPerson());
        return mapToResponse(supplierRepository.save(s));
    }

    private SupplierResponse mapToResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId()).name(s.getName()).email(s.getEmail()).phone(s.getPhone())
                .address(s.getAddress()).city(s.getCity()).state(s.getState())
                .pincode(s.getPincode()).gstin(s.getGstin()).contactPerson(s.getContactPerson())
                .companyId(s.getCompany().getId()).active(s.getActive())
                .createdAt(s.getCreatedAt()).build();
    }
}
