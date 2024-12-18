package com.onnury.exception.inquiry;

import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.inquiry.request.FaqCreateRequestDto;
import com.onnury.inquiry.request.FaqUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public interface FaqExceptioInterface {

    // 생성하고자 하는 FAQ의 정보가 옳바른지 확인
    boolean checkCreateFaqInfo(FaqCreateRequestDto faqInfo);

    // 수정하고자 하는 브랜드의 정보가 옳바른지 확인
    boolean checkUpdateFaqInfo(FaqUpdateRequestDto faqInfo);
}
