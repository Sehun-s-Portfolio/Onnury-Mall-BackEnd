package com.onnury.brand.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MainPageBrandResponseDto {
    private Long brandId;
    private String brandTitle;
    private String status;
    private Long mediaId;
    private String mediaUrl;
}
