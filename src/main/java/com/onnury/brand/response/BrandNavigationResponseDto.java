package com.onnury.brand.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BrandNavigationResponseDto {
    private Long brandId;
    private String brandTitle;
}
