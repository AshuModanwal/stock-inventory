package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.category.*;
import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public CategoryResponse create(CategoryRequest request, UserPrincipal user) {
        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", user.getCompanyId()));

        String slug = SlugGenerator.generateSlug(request.getName());
        if (categoryRepository.existsBySlugAndCompanyId(slug, company.getId())) {
            throw new DuplicateResourceException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(request.getName()).slug(slug)
                .description(request.getDescription())
                .company(company).build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAll(Long companyId, int page, int size) {
        Page<Category> cats = categoryRepository.findByCompanyId(companyId, PageRequest.of(page, size, Sort.by("name")));
        return PagedResponse.<CategoryResponse>builder()
                .content(cats.getContent().stream().map(this::mapToResponse).toList())
                .page(cats.getNumber()).size(cats.getSize())
                .totalElements(cats.getTotalElements()).totalPages(cats.getTotalPages())
                .last(cats.isLast()).build();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveList(Long companyId) {
        return categoryRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request, UserPrincipal user) {
        Category cat = categoryRepository.findByIdAndCompanyId(id, user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        if (request.getName() != null) {
            cat.setName(request.getName());
            cat.setSlug(SlugGenerator.generateSlug(request.getName()));
        }
        if (request.getDescription() != null) cat.setDescription(request.getDescription());
        return mapToResponse(categoryRepository.save(cat));
    }

    @Transactional
    public void toggleActive(Long id, UserPrincipal user) {
        Category cat = categoryRepository.findByIdAndCompanyId(id, user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        cat.setActive(!cat.getActive());
        categoryRepository.save(cat);
    }

    private CategoryResponse mapToResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).image(c.getImage())
                .companyId(c.getCompany().getId()).active(c.getActive())
                .createdAt(c.getCreatedAt()).build();
    }
}
