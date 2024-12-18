package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminCancleOrderResponseQDto is a Querydsl Projection type for AdminCancleOrderResponseQDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminCancleOrderResponseQDto extends ConstructorExpression<AdminCancleOrderResponseQDto> {

    private static final long serialVersionUID = -520974509L;

    public QAdminCancleOrderResponseQDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> supplierId, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> canclecancelAmount, com.querydsl.core.types.Expression<String> cancleonNuryStatementNumber, com.querydsl.core.types.Expression<Integer> cancleonNuryCanclePrice, com.querydsl.core.types.Expression<String> canclecreditStatementNumber, com.querydsl.core.types.Expression<Integer> canclecreditCanclePrice, com.querydsl.core.types.Expression<java.time.LocalDateTime> cancelRequestAt) {
        super(AdminCancleOrderResponseQDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, String.class, int.class, String.class, int.class, java.time.LocalDateTime.class}, orderNumber, supplierId, detailOptionTitle, productClassificationCode, productName, canclecancelAmount, cancleonNuryStatementNumber, cancleonNuryCanclePrice, canclecreditStatementNumber, canclecreditCanclePrice, cancelRequestAt);
    }

}

