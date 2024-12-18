package com.onnury.brand.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BrandDataResponseDto {
    private Long brandId;
    private String brandTitle; // 브랜드명
    private String status; // 상태
    private String imgUrl; // 브랜드 이미지 url
}