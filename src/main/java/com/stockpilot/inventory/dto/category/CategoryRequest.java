package com.stockpilot.inventory.dto.category;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CategoryRequest {
    @NotBlank @Size(max = 100)
    private String name;
    private String description;
}
