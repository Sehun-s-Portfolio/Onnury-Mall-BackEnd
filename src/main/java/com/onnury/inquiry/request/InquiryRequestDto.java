package com.onnury.inquiry.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class InquiryRequestDto extends AbstractVO {
    private String type; // 문의 타입
    private String inquiryTitle; // 문의 제목
    private String inquiryContent; // 문의 내용
}
