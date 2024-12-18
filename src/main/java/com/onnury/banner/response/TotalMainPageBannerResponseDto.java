package com.onnury.banner.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalMainPageBannerResponseDto {
    private Long totalBannerCount;
    private List<MainPageBannerResponseDto> bannerList;
}
