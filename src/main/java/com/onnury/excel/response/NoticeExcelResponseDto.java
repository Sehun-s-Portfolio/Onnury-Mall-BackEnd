package com.onnury.excel.response;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;

@Builder
@Getter
public class NoticeExcelResponseDto {
    private Long noticeId;
    private String noticeTitle; // 공지사항 타이틀
    private String noticeContent; // 공지사항 내용
    private String createdAt;
    private String modifiedAt;
}
