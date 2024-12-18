package com.onnury.brand.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class BrandCreateResponseDto {
    private String brandTitle; // 브랜드명
    private String status; // 브랜드 상태
    private List<BrandMediaResponseDto> brandImages; // 브랜드 관련 이미지들
}