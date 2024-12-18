package com.onnury.product.request;

import lombok.Getter;

@Getter
public class ProductDetailOptionUpdateRequestDto {
    private Long detailOptionId; // 수정할 상세 옵션 id (기존 옵션을 수정할 경우 수정할 옵션의 id가 들어가고, 새롭게 추가되는 옵션의 경우에는 0으로 들어간다.)
    private String detailOptionName; // 상세 옵션 명
    private int optionPrice; // 옵션 가격
}
