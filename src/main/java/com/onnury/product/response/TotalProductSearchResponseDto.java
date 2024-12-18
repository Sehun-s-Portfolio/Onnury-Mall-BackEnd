package com.onnury.product.response;

import com.onnury.brand.response.BrandDataResponseDto;
import com.onnury.category.response.RelatedCategoryDataResponseDto;
import com.onnury.label.response.LabelResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalProductSearchResponseDto {
    private int totalSearchProductCount;
    private int maxPrice;
    private List<ProductSearchResponseDto> searchProductList;
    private List<BrandDataResponseDto> brandList;
    private List<LabelResponseDto> labelList;
    private List<RelatedCategoryDataResponseDto> upCategoryList;
    private List<RelatedCategoryDataResponseDto> middleCategoryList;
    private List<RelatedCategoryDataResponseDto> downCategoryList;
}
