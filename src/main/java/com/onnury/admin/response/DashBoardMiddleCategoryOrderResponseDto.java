package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashBoardMiddleCategoryOrderResponseDto {
    private Long categoryId; // 중분류 카테고리 id
    private String categoryName; // 중분류 카테고리 명
    private Long categoryOrderCount; // 중분류 카테고리 주문 건수
}
