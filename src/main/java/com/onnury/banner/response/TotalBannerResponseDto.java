package com.onnury.banner.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalBannerResponseDto {
    private Long totalBannerCount;
    private List<BannerDataResponseDto> responseBannerList;
}
