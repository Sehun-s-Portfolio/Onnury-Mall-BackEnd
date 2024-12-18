package com.onnury.product.response;

import com.onnury.brand.response.BrandDataResponseDto;
import com.onnury.category.response.RelatedCategoryDataResponseDto;
import com.onnury.label.response.LabelResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalProductPageMainProductResponseDto {
    private int totalMainProductCount;
    private int maxPrice;
    private List<ProductPageMainProductResponseDto> mainProductList;
    private List<BrandDataResponseDto> brandList;
    private List<LabelResponseDto> labelList;
    private List<RelatedCategoryDataResponseDto> relatedUnderCategoryList;
}
