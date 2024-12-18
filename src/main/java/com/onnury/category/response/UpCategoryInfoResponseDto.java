package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UpCategoryInfoResponseDto {
    private Long upCategoryId;
    private String upCategoryName;
    private List<MiddleCategoryInfoResponseDto> relatedMiddleCategories;
    private List<RelatedBrandResponseDto> relatedBrands;
}
