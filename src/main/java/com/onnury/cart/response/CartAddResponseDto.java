package com.onnury.cart.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartAddResponseDto {
    private Long cartId;
    private Long memberId; // 장바구니에 담은 고객 id
    private Long productId; // 장바구니에 담은 제품 id
    private String productCode; // 장바구니에 담은 제품 분류 코드
    private String productOptionTitle; // 장바구니에 담은 제품 옵션 타이틀
    private String productDetailOptionTitle; // 제품 상세 옵션 타이틀
    private int productDetailOptionPrice; // 제품 상세 옵션 가격
    private int productPrice; // 제품 가격
    private int quantity; // 제품 수량
}
