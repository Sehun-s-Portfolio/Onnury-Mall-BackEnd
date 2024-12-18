package com.onnury.exception.inquiry;

import org.springframework.stereotype.Component;

@Component
public interface InquiryExceptioInterface {

    // 수정하고자 하는 문의의 정보가 옳바른지 확인
    boolean checkUpdateInquiryInfo(String answer);

}
