package com.onnury.notice.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class NoticeUpdateRequestDto extends AbstractVO {
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
}
