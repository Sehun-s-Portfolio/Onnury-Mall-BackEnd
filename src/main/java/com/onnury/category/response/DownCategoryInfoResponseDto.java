package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DownCategoryInfoResponseDto {
    private Long downCategoryId;
    private String downCategoryName;
}
