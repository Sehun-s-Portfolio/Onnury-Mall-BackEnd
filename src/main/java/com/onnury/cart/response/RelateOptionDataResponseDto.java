package com.onnury.cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RelateOptionDataResponseDto {
    private Long productOptionId; // 장바구니에 담긴 제품 옵션의 id
    private String productOptionTitle; // 장바구니에 담은 제품 옵션 타이틀
    private Long productDetailOptionId; // 장바구니에 담긴 제품 상세 옵션 id
    private String productDetailOptionTitle; // 제품 상세 옵션 타이틀
}
