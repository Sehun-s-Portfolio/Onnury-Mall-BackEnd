package com.onnury.banner.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MainPagePromotionBannerResponseDto {
    private Long bannerId; // 배너 id
    private String title; // 배너 타이틀
    private String linkUrl; // 링크 페이지 url
    private int expressionOrder; // 노출 순서
    private String startPostDate; // 게시 시작일
    private String endPostDate; // 게시 종료일
    private BannerMediaResponseDto bannerImage; // 배너 이미지
}
