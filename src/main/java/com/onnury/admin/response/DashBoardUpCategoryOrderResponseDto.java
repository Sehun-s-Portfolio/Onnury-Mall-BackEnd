package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashBoardUpCategoryOrderResponseDto {
    private Long categoryId; // 대분류 카테고리 id
    private String categoryName; // 대분류 카테고리 명
    private Long categoryOrderCount; // 대분류 카테고리 주문 건수
}
