package com.onnury.category.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RelatedBrandResponseDto {
    private Long brandId;
    private String brandName;
}
