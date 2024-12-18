package com.onnury.category.response;

import com.onnury.brand.response.BrandNavigationResponseDto;
import com.querydsl.core.Tuple;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AllCategoryNavigationResponseDto {
    private List<CategoryNavigationResponseDto> allUpCategories; // 모든 대분류 카테고리
    private List<CategoryNavigationResponseDto> allMiddleCategories; // 모든 중분류 카테고리
    private List<CategoryNavigationResponseDto> allDownCategories; // 모든 하위 카테고리
    private List<BrandNavigationResponseDto> allBrands; // 모든 브랜드
    private Tuple Categories; // 모든 대분류 카테고리

}
