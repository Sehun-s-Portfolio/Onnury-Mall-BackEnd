package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AdminTotalProductSearchResponseDto {
    private Long totalSearchProductCount;
    private List<ProductSearchResponseDto> searchProductList;
}
