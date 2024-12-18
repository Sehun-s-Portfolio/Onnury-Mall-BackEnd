package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductDetailOptionCreateResponseDto {
    private Long detailOptionId;
    private String detailOptionName; // 상세 옵션 명
    private int optionPrice; // 옵션 가격
}
