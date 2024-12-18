package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CategoryCreateResponseDto {
    private int categoryGroup; // 카테고리 분류(대 - 0, 중 - 1, 소 - 2)
    private String motherCode; // 상위 카테고리 코드
    private String classificationCode; // 자코드
    private String categoryName; // 카테고리이름
    private String imgUrl; // 카테고리 이미지 호출 경로
    private String type; // 이미지 용도 유형
}
