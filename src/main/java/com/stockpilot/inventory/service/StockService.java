package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.stock.*;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.enums.StockMovementType;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public StockMovementResponse adjustStock(StockAdjustmentRequest request, UserPrincipal currentUser) {
        Product product = productRepository.findByIdAndCompanyId(request.getProductId(), currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        Company company = companyRepository.findById(currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", currentUser.getCompanyId()));

        int prevQty = product.getQuantity();
        int newQty;

        switch (request.getType()) {
            case PURCHASE, RETURN -> newQty = prevQty + request.getQuantity();
            case DAMAGE, ADJUSTMENT -> {
                if (prevQty < request.getQuantity() && request.getType() == StockMovementType.DAMAGE) {
                    throw new BadRequestException("Cannot remove more stock than available");
                }
                newQty = request.getType() == StockMovementType.DAMAGE
                        ? prevQty - request.getQuantity() : request.getQuantity(); // ADJUSTMENT sets absolute
            }
            default -> throw new BadRequestException("Invalid stock movement type for manual adjustment");
        }

        product.setQuantity(newQty);
        if (request.getUnitPrice() != null && request.getType() == StockMovementType.PURCHASE) {
            product.setCostPrice(request.getUnitPrice());
        }
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product).type(request.getType()).quantity(request.getQuantity())
                .previousQty(prevQty).newQty(newQty).unitPrice(request.getUnitPrice())
                .reason(request.getReason()).performedBy(user).company(company).build();

        return mapToResponse(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StockMovementResponse> getMovements(Long companyId, int page, int size) {
        Page<StockMovement> movements = movementRepository.findByCompanyIdOrderByMovementDateDesc(
                companyId, PageRequest.of(page, size));
        return PagedResponse.<StockMovementResponse>builder()
                .content(movements.getContent().stream().map(this::mapToResponse).toList())
                .page(movements.getNumber()).size(movements.getSize())
                .totalElements(movements.getTotalElements()).totalPages(movements.getTotalPages())
                .last(movements.isLast()).build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<StockMovementResponse> getProductMovements(Long productId, int page, int size) {
        Page<StockMovement> movements = movementRepository.findByProductIdOrderByMovementDateDesc(
                productId, PageRequest.of(page, size));
        return PagedResponse.<StockMovementResponse>builder()
                .content(movements.getContent().stream().map(this::mapToResponse).toList())
                .page(movements.getNumber()).size(movements.getSize())
                .totalElements(movements.getTotalElements()).totalPages(movements.getTotalPages())
                .last(movements.isLast()).build();
    }

    private StockMovementResponse mapToResponse(StockMovement m) {
        return StockMovementResponse.builder()
                .id(m.getId()).productId(m.getProduct().getId()).productName(m.getProduct().getName())
                .type(m.getType()).quantity(m.getQuantity()).previousQty(m.getPreviousQty())
                .newQty(m.getNewQty()).unitPrice(m.getUnitPrice()).reason(m.getReason())
                .referenceId(m.getReferenceId())
                .performedByName(m.getPerformedBy() != null ? m.getPerformedBy().getFullName() : null)
                .movementDate(m.getMovementDate()).build();
    }
}
