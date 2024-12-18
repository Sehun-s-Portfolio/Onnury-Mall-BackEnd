package com.onnury.inquiry.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class FaqResponseDto {
    private Long faqId;
    private String type; // 유형
    private String question; // 질문
    private String answer; // 답변
    private String answerCheck; // 답변 여부
    private String expressCheck; // 노출유무
    private LocalDateTime faqCreatedAt; // 문의 일자
}