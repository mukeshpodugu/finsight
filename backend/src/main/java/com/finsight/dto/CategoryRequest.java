package com.finsight.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    
    @NotBlank(message = "Category type is required (INCOME or EXPENSE)")
    private String type;
    
    private String colorCode;
    private String iconName;
}
