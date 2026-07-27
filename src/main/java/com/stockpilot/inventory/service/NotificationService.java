package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.notification.NotificationResponse;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.enums.NotificationType;
import com.stockpilot.inventory.enums.RoleName;
import com.stockpilot.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createLowStockAlert(Product product, Company company) {
        String title = product.isOutOfStock() ? "Out of Stock!" : "Low Stock Alert";
        String message = product.isOutOfStock()
                ? product.getName() + " is out of stock. Restock immediately."
                : product.getName() + " has only " + product.getQuantity() + " units left.";
        NotificationType type = product.isOutOfStock() ? NotificationType.OUT_OF_STOCK : NotificationType.LOW_STOCK;

        // Notify company admins
        List<User> admins = userRepository.findByCompanyIdAndActiveTrue(company.getId());
        for (User admin : admins) {
            boolean isAdminOrSales = admin.getRoles().stream()
                    .anyMatch(r -> r.getName() == RoleName.COMPANY_ADMIN);
            if (isAdminOrSales) {
                Notification notification = Notification.builder()
                        .user(admin).title(title).message(message).type(type)
                        .entityType("Product").entityId(product.getId()).build();
                notificationRepository.save(notification);
            }
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        return PagedResponse.<NotificationResponse>builder()
                .content(notifications.getContent().stream().map(this::mapToResponse).toList())
                .page(notifications.getNumber()).size(notifications.getSize())
                .totalElements(notifications.getTotalElements()).totalPages(notifications.getTotalPages())
                .last(notifications.isLast()).build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).isRead(n.getIsRead()).entityType(n.getEntityType())
                .entityId(n.getEntityId()).createdAt(n.getCreatedAt()).build();
    }
}
