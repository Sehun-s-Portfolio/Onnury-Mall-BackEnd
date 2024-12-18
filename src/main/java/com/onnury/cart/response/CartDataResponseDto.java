package com.onnury.cart.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CartDataResponseDto {
    private Long cartId; // 장바구니 id
    private Long memberId; // 장바구니에 담은 고객 id
    private Long productId; // 장바구니에 담은 제품 id
    private String productName; // 장바구니에 담은 제품명
    private String productCode; // 장바구니에 담은 제품 분류 코드
    private String productImage; // 장바구니에 담긴 제품의 대표 이미지
    private List<RelateOptionDataResponseDto> productOptionList;
    private int productDetailOptionPrice; // 제품 상세 옵션 가격
    private int productPrice; // 제품 가격
    private int quantity; // 제품 수량
    private String status; // 제품 상태
    private String expressionCheck; // 제품 노출 여부
}
