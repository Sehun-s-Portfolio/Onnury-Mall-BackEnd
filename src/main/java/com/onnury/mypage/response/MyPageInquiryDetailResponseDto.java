package com.onnury.mypage.response;

import com.onnury.media.response.MediaResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class MyPageInquiryDetailResponseDto {
    private Long inquiryId;
    private String type; // 문의타입
    private String inquiryTitle; // 문의 제목
    private String inquiryContent; // 문의 내용
    private LocalDateTime createdAt; // 문의 등록 시간
    private LocalDateTime answerAt; // 문의 답변 시간
    private String answer; // 문의 답변 내용
    private Long memberId; // 문의자 id
    private String userName; // 문의자 명
    private List<MediaResponseDto> relateFiles; // 연관 파일들
}
