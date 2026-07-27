package com.stockpilot.inventory.dto.category;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private Long companyId;
    private Boolean active;
    private LocalDateTime createdAt;
}
