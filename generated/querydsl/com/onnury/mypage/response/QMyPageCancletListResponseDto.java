package com.onnury.mypage.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.mypage.response.QMyPageCancletListResponseDto is a Querydsl Projection type for MyPageCancletListResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMyPageCancletListResponseDto extends ConstructorExpression<MyPageCancletListResponseDto> {

    private static final long serialVersionUID = -689290303L;

    public QMyPageCancletListResponseDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<String> productImgurl, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<Integer> cancelAmount, com.querydsl.core.types.Expression<java.time.LocalDateTime> cancelAt, com.querydsl.core.types.Expression<String> cancelCheck, com.querydsl.core.types.Expression<Integer> onNuryCanclePrice, com.querydsl.core.types.Expression<Integer> creditCanclePrice, com.querydsl.core.types.Expression<java.time.LocalDateTime> cancelRequestAt) {
        super(MyPageCancletListResponseDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, java.time.LocalDateTime.class, String.class, int.class, int.class, java.time.LocalDateTime.class}, orderNumber, productName, productImgurl, productClassificationCode, detailOptionTitle, cancelAmount, cancelAt, cancelCheck, onNuryCanclePrice, creditCanclePrice, cancelRequestAt);
    }

}

