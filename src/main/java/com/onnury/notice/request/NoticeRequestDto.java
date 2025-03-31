package com.onnury.notice.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class NoticeRequestDto extends AbstractVO {
    private String noticeTitle;
    private String noticeContent;
}
