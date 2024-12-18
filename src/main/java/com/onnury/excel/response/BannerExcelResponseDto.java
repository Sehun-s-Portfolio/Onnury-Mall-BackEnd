package com.onnury.excel.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BannerExcelResponseDto {
    private Long bannerId;
    private String title; // 배너 명
    private String linkUrl; // 배너 클릭 시 이동 페이지 url
    private int expressionOrder; // 배너 노출 순서
    private String expressionCheck; // 배너 노출 확인
    private String startPostDate; // 배너 게시 시작 일
    private String endPostDate; // 배너 게시 마지막 일
    private String createdAt;
    private String modifiedAt;
}
