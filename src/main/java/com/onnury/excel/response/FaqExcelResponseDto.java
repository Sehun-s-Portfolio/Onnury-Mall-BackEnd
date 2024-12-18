package com.onnury.excel.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FaqExcelResponseDto {
    private Long faqId;
    private String type; // FAQ타입
    private String question; // 질문
    private String answer; // 답변
    private String expressCheck; // 노출유무
    private String createdAt;
    private String modifiedAt;
}
