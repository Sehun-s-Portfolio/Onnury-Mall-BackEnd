package com.onnury.notice.request;

import lombok.Getter;

@Getter
public class NoticeUpdateRequestDto {
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
}
