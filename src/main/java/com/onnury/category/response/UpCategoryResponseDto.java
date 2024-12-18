package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpCategoryResponseDto {
    private Long categoryId; // 카테고리 id
    private int categoryGroup; // 카테고리 분류(대 - 0, 중 - 1, 소 - 2)
    private String motherCode; // 상위 카테고리 코드
    private String classificationCode; // 카테고리 고유 분류 코드
    private String categoryName; // 카테고리 이름
}
