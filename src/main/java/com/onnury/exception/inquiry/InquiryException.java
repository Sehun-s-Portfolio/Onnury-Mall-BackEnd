package com.onnury.exception.inquiry;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InquiryException implements InquiryExceptioInterface {

    private final JPAQueryFactory jpaQueryFactory;


    // 수정하고자 하는 문의의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateInquiryInfo(String answer) {

        if(answer.isEmpty()){
            return true;
        }

        return false;
    }
}
