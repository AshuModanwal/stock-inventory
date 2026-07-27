package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.product.*;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(ProductRequest request, UserPrincipal user) {
        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", user.getCompanyId()));

        if (productRepository.existsBySkuAndCompanyId(request.getSku(), company.getId())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndCompanyId(request.getCategoryId(), company.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Product product = Product.builder()
                .name(request.getName()).sku(request.getSku()).barcode(request.getBarcode())
                .description(request.getDescription()).category(category)
                .costPrice(request.getCostPrice()).sellPrice(request.getSellPrice())
                .mrp(request.getMrp()).quantity(request.getQuantity())
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 5)
                .unit(request.getUnit() != null ? request.getUnit() : "PCS")
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : company.getTaxRate())
                .hsnCode(request.getHsnCode()).company(company).build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAll(Long companyId, int page, int size,
                                                  String search, Long categoryId, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sortBy != null ? sortBy : "createdAt").descending());

        Page<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.searchByCompany(companyId, search, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCompanyAndCategory(companyId, categoryId, pageable);
        } else {
            products = productRepository.findByCompanyId(companyId, pageable);
        }

        return PagedResponse.<ProductResponse>builder()
                .content(products.getContent().stream().map(this::mapToResponse).toList())
                .page(products.getNumber()).size(products.getSize())
                .totalElements(products.getTotalElements()).totalPages(products.getTotalPages())
                .last(products.isLast()).build();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id, Long companyId) {
        return mapToResponse(productRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id)));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse update(Long id, ProductRequest request, UserPrincipal user) {
        Product product = productRepository.findByIdAndCompanyId(id, user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getBarcode() != null) product.setBarcode(request.getBarcode());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getSellPrice() != null) product.setSellPrice(request.getSellPrice());
        if (request.getMrp() != null) product.setMrp(request.getMrp());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getLowStockThreshold() != null) product.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getTaxRate() != null) product.setTaxRate(request.getTaxRate());
        if (request.getHsnCode() != null) product.setHsnCode(request.getHsnCode());

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findByIdAndCompanyId(request.getCategoryId(), user.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(cat);
        }

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void toggleActive(Long id, UserPrincipal user) {
        Product product = productRepository.findByIdAndCompanyId(id, user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(!product.getActive());
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStock(Long companyId) {
        return productRepository.findLowStockByCompany(companyId).stream().map(this::mapToResponse).toList();
    }

    public ProductResponse mapToResponse(Product p) {
        double margin = p.getCostPrice() > 0 ? ((p.getSellPrice() - p.getCostPrice()) / p.getCostPrice()) * 100 : 0;
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).sku(p.getSku()).barcode(p.getBarcode())
                .description(p.getDescription())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .costPrice(p.getCostPrice()).sellPrice(p.getSellPrice()).mrp(p.getMrp())
                .quantity(p.getQuantity()).lowStockThreshold(p.getLowStockThreshold())
                .unit(p.getUnit()).taxRate(p.getTaxRate()).hsnCode(p.getHsnCode())
                .image(p.getImage()).companyId(p.getCompany().getId())
                .active(p.getActive()).lowStock(p.isLowStock()).outOfStock(p.isOutOfStock())
                .profitMargin(Math.round(margin * 100.0) / 100.0)
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
