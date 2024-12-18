package com.onnury.notice.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NoticeDetailResponseDto {
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime createdAt;
}
