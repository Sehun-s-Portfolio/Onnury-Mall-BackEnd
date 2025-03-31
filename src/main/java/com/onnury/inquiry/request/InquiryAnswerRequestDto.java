package com.onnury.inquiry.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class InquiryAnswerRequestDto extends AbstractVO {
    private Long inquiryId;
    private String answer;
}
