package com.onnury.notice.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalNoticeResponseDto {
    private Long totalNoticeCount;
    private List<NoticeResponseDto> noticeList;
}
