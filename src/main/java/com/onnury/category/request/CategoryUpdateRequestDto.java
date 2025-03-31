package com.onnury.category.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class CategoryUpdateRequestDto extends AbstractVO {
    private int categoryGroup; // 카테고리 분류(대 - 0, 중 - 1, 소 - 2)
    private String motherCode; // 상위 카테고리 코드
    private String categoryName; // 카테고리이름
}
