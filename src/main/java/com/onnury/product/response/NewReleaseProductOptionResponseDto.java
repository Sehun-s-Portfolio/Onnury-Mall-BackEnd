package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NewReleaseProductOptionResponseDto {
    private Long productOptionId;
    private String productOptionTitle; // 상위 옵션 명
    private String necessaryCheck; // 옵션 선택 필수 유무 (Y / N)
    private List<NewReleaseProductDetailOptionResponseDto> productDetailOptionList;
}
