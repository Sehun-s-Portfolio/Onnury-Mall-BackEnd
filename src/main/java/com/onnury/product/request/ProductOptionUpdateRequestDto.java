package com.onnury.product.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductOptionUpdateRequestDto {
    private Long productOptionId; // 수정할 옵션 id (기존 옵션을 수정할 경우 수정할 옵션의 id가 들어가고, 새롭게 추가되는 옵션의 경우에는 0으로 들어간다.)
    private String productOptionTitle; // 상위 옵션 명
    private String necessaryCheck; // 옵션 선택 필수 유무 (Y / N)
    private List<ProductDetailOptionUpdateRequestDto> productDetailOptionList;
}
