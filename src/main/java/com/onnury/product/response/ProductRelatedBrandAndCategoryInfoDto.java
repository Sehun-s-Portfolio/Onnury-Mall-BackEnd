package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductRelatedBrandAndCategoryInfoDto {
    private Long productId;
    private Long brandId;
    private Long upCategoryId;
    private Long middleCategoryId;
    private Long downCategoryId;
}
