package com.onnury.inquiry.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class FaqCreateRequestDto extends AbstractVO {
    private String type; // FAQ타입
    private String question; // 질문
    private String answer; // 답변
    private String expressCheck; // 노출유무
}
