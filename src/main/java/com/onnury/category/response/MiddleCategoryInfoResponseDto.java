package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MiddleCategoryInfoResponseDto {
    private Long middleCategoryId;
    private String middleCategoryName;
    private List<DownCategoryInfoResponseDto> relatedDownCategories;
}
