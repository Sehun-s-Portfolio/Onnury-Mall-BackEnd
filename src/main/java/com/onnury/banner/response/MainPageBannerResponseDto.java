package com.onnury.banner.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MainPageBannerResponseDto {
    private Long bannerId; // 배너 id
    private String title; // 배너 타이틀
    private String linkUrl; // 링크 페이지 url
    private int expressionOrder; // 노출 순서
    private List<BannerMediaResponseDto> bannerImages; // 배너 이미지
}
