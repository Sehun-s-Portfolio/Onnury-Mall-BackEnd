package com.onnury.product.response;

import com.onnury.category.response.RelatedCategoryDataResponseDto;
import com.onnury.label.response.LabelResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalBrandProductPageResponseDto {
    private Long totalBrandProductCount;
    private int maxPrice;
    private Long brandId;
    private String brandImgUrl;
    private List<BrandProductResponseDto> brandProductList;
    private List<LabelResponseDto> labelList;
    private List<RelatedCategoryDataResponseDto> middleCategoryList;
}
