package com.onnury.exception.inquiry;


import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.inquiry.request.FaqCreateRequestDto;
import com.onnury.inquiry.request.FaqUpdateRequestDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FaqException implements FaqExceptioInterface {

    private final JPAQueryFactory jpaQueryFactory;

    // 생성하고자 하는 FAQ의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateFaqInfo(FaqCreateRequestDto faqInfo) {

        if(faqInfo.getType().isEmpty() || faqInfo.getQuestion().isEmpty() || faqInfo.getAnswer().isEmpty() || faqInfo.getExpressCheck().isEmpty()){
                return true;
        }

        return false;
    }

    // 수정하고자 하는 브랜드의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateFaqInfo(FaqUpdateRequestDto faqInfo) {

        if(faqInfo.getType().isEmpty() || faqInfo.getQuestion().isEmpty() || faqInfo.getAnswer().isEmpty() || faqInfo.getExpressCheck().isEmpty()){
            return true;
        }

        return false;
    }
}
