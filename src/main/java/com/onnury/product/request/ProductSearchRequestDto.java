package com.onnury.product.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductSearchRequestDto {
    private int page;
    private Long upCategoryId;
    private Long middleCategoryId;
    private Long downCategoryId;
    private Long brandId;
    private Long supplierId;
    private String searchKeyword;
}
