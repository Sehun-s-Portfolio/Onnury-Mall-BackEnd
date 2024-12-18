package com.onnury.inquiry.request;

import lombok.Getter;

@Getter
public class InquiryRequestDto {
    private String type; // 문의 타입
    private String inquiryTitle; // 문의 제목
    private String inquiryContent; // 문의 내용
}
