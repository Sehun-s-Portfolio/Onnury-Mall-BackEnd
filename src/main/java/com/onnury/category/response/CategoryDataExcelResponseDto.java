package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CategoryDataExcelResponseDto {
    private String ucategoryName; // 카테고리이름
    private String mcategoryName; // 카테고리이름
    private String dcategoryName; // 카테고리이름
    private Long productCount;
}