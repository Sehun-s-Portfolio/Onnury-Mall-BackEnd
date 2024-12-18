package com.onnury.exception.notice;

import com.onnury.notice.request.NoticeRequestDto;
import com.onnury.notice.request.NoticeUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class NoticeException implements NoticeExceptionInterface{

    /** 공지사항 작성 정보 검증 **/
    @Override
    public boolean checkWriteNoticeInfo(NoticeRequestDto noticeRequestDto) {

        if(noticeRequestDto.getNoticeTitle().isEmpty() || noticeRequestDto.getNoticeTitle() == null ||
        noticeRequestDto.getNoticeContent().isEmpty() || noticeRequestDto.getNoticeContent() == null){
            return true;
        }

        return false;
    }


    /** 공지사항 수정 정보 **/
    @Override
    public boolean checkUpdateNoticeInfo(NoticeUpdateRequestDto noticeUpdateRequestDto) {

        if(noticeUpdateRequestDto.getNoticeTitle().isEmpty() || noticeUpdateRequestDto.getNoticeTitle() == null ||
                noticeUpdateRequestDto.getNoticeContent().isEmpty() || noticeUpdateRequestDto.getNoticeContent() == null){
            return true;
        }

        return false;
    }
}
