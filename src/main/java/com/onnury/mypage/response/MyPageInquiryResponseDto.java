package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class MyPageInquiryResponseDto {
    private Long inquiryId;
    private String type; // 문의타입
    private String inquiryTitle; // 문의 제목
    private LocalDateTime answerAt; // 문의 답변 시간
    private String answerCheck; // 문의 답변 여부 (답변 대기, 답변 완료)
    private Long memberId; // 문의자 id
    private String userName; // 문의자 명
    private LocalDateTime createdAt; // 문의 일시
}
