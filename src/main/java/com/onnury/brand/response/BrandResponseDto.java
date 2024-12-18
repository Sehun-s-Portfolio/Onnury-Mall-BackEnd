package com.onnury.brand.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BrandResponseDto {
    private Long brandId; // 브랜드 id
    private String brandTitle; // 브랜드 명
}
