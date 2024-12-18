package com.onnury.product.request;

import lombok.Getter;

@Getter
public class ProductDetailOptionCreateRequestDto {
    private String detailOptionName; // 상세 옵션 명
    private int optionPrice; // 옵션 가격
}
