package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RelatedCategoryDataResponseDto {
    private Long categoryId;
    private String categoryName;
}
