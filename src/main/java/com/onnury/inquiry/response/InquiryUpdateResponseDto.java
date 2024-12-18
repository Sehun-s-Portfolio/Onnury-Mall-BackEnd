package com.onnury.inquiry.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class InquiryUpdateResponseDto {
    private Long inquiryId;
    private String type; // 문의타입
    private String inquiryTitle; // 문의 제목
    private String inquiryContent; // 문의 내용
    private String answer; // 문의 답변
    private LocalDateTime answerAt; // 문의 답변 시간
}