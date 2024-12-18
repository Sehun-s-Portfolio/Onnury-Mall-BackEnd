package com.onnury.banner.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BannerDataResponseDto {
    private Long bannerId; // 배너 id
    private String title; // 배너 타이틀
    private String linkUrl; // 링크 페이지 url
    private int expressionOrder; // 노출 순서
    private String expressionCheck; // 노출 확인 변수 (Y / N)
    private String appBannerImgUrl;
    private String webBannerImgUrl;
    private String promotionBannerImgUrl;
    private String slideBannerImgUrl;
    private String startPostDate; // 배너 게시 시작일
    private String endPostDate; // 배너 게시 종료일
    private String imgType; // 배너 이미지 타입
}