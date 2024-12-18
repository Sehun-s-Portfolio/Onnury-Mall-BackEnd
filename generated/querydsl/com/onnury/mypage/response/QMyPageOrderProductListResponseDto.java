package com.onnury.mypage.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.mypage.response.QMyPageOrderProductListResponseDto is a Querydsl Projection type for MyPageOrderProductListResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMyPageOrderProductListResponseDto extends ConstructorExpression<MyPageOrderProductListResponseDto> {

    private static final long serialVersionUID = 845493974L;

    public QMyPageOrderProductListResponseDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> seq, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<String> productImgurl, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<Long> supplierId, com.querydsl.core.types.Expression<Integer> productAmount, com.querydsl.core.types.Expression<Integer> productOptionAmount, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<Integer> deliveryPrice, com.querydsl.core.types.Expression<Integer> dangerPlacePrice, com.querydsl.core.types.Expression<Integer> onnurypay, com.querydsl.core.types.Expression<Integer> productTotalAmount, com.querydsl.core.types.Expression<String> memo, com.querydsl.core.types.Expression<String> transportNumber, com.querydsl.core.types.Expression<String> parcelName, com.querydsl.core.types.Expression<String> completePurchaseCheck, com.querydsl.core.types.Expression<java.time.LocalDateTime> completePurchaseAt, com.querydsl.core.types.Expression<Integer> cancelAmount) {
        super(MyPageOrderProductListResponseDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, long.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, String.class, String.class, String.class, String.class, java.time.LocalDateTime.class, int.class}, orderNumber, seq, productName, productImgurl, productClassificationCode, detailOptionTitle, supplierName, supplierId, productAmount, productOptionAmount, quantity, deliveryPrice, dangerPlacePrice, onnurypay, productTotalAmount, memo, transportNumber, parcelName, completePurchaseCheck, completePurchaseAt, cancelAmount);
    }

}

