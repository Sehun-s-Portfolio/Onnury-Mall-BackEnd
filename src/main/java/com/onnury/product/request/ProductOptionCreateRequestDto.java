package com.onnury.product.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductOptionCreateRequestDto {
    private String productOptionTitle; // 상위 옵션 명
    private String necessaryCheck; // 옵션 선택 필수 유무 (Y / N)
    private List<ProductDetailOptionCreateRequestDto> productDetailOptionList;
}
