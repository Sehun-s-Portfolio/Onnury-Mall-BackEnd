package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminCancelPaymentResponseDto is a Querydsl Projection type for AdminCancelPaymentResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminCancelPaymentResponseDto extends ConstructorExpression<AdminCancelPaymentResponseDto> {

    private static final long serialVersionUID = 1263888L;

    public QAdminCancelPaymentResponseDto(com.querydsl.core.types.Expression<Long> cancleOrderId, com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> seq, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> supplierId, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<Integer> productAmount, com.querydsl.core.types.Expression<Integer> productOptionAmount, com.querydsl.core.types.Expression<Integer> deliveryPrice, com.querydsl.core.types.Expression<Integer> dangerPlacePrice, com.querydsl.core.types.Expression<Integer> cancelAmount, com.querydsl.core.types.Expression<Integer> totalPrice, com.querydsl.core.types.Expression<String> onNuryStatementNumber, com.querydsl.core.types.Expression<String> creditStatementNumber, com.querydsl.core.types.Expression<Integer> onNuryCanclePrice, com.querydsl.core.types.Expression<Integer> creditCanclePrice, com.querydsl.core.types.Expression<String> cancelCheck, com.querydsl.core.types.Expression<String> cancelAt, com.querydsl.core.types.Expression<String> cancelRequestAt) {
        super(AdminCancelPaymentResponseDto.class, new Class<?>[]{long.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, int.class, int.class, int.class, int.class, int.class, int.class, String.class, String.class, int.class, int.class, String.class, String.class, String.class}, cancleOrderId, orderNumber, seq, productName, productClassificationCode, detailOptionTitle, supplierId, supplierName, productAmount, productOptionAmount, deliveryPrice, dangerPlacePrice, cancelAmount, totalPrice, onNuryStatementNumber, creditStatementNumber, onNuryCanclePrice, creditCanclePrice, cancelCheck, cancelAt, cancelRequestAt);
    }

}

