package com.onnury.cart.request;

import lombok.Getter;

import java.util.List;

@Getter
public class CartAddRequestDto {
    private Long productId;
    private List<Long> productOptionIds;
    private List<Long> productDetailOptionIds;
    private int quantity; // 제품 + 옵션 수량
}
