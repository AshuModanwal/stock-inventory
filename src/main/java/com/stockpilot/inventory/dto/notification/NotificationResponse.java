package com.stockpilot.inventory.dto.notification;

import com.stockpilot.inventory.enums.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
