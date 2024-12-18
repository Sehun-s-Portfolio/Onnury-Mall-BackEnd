package com.onnury.exception.notice;

import com.onnury.notice.request.NoticeRequestDto;
import com.onnury.notice.request.NoticeUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public interface NoticeExceptionInterface {

    /** 공지사항 작성 정보 검증 **/
    boolean checkWriteNoticeInfo(NoticeRequestDto noticeRequestDto);

    /** 공지사항 수정 정보 **/
    boolean checkUpdateNoticeInfo(NoticeUpdateRequestDto noticeUpdateRequestDto);
}
