package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductDetailImageInfoResponseDto {
    private Long productDetailImageId;
    private String type;
    private String imgUrl;
}
