package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QCancelPaymentResponseDto is a Querydsl Projection type for CancelPaymentResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QCancelPaymentResponseDto extends ConstructorExpression<CancelPaymentResponseDto> {

    private static final long serialVersionUID = 959331363L;

    public QCancelPaymentResponseDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> seq, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<java.time.LocalDateTime> orderedAt, com.querydsl.core.types.Expression<String> buyMemberLoginId, com.querydsl.core.types.Expression<String> message, com.querydsl.core.types.Expression<Integer> deliveryPrice, com.querydsl.core.types.Expression<Integer> dangerPlacePrice, com.querydsl.core.types.Expression<java.time.LocalDateTime> completePaymentAt, com.querydsl.core.types.Expression<String> receiverPhone, com.querydsl.core.types.Expression<String> creditStatementNumber, com.querydsl.core.types.Expression<Integer> creditApprovalPrice, com.querydsl.core.types.Expression<String> onNuryStatementNumber, com.querydsl.core.types.Expression<Integer> onNuryApprovalPrice, com.querydsl.core.types.Expression<Integer> totalApprovalPrice, com.querydsl.core.types.Expression<String> transportNumber, com.querydsl.core.types.Expression<String> parcelName, com.querydsl.core.types.Expression<String> receiver, com.querydsl.core.types.Expression<String> address) {
        super(CancelPaymentResponseDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, java.time.LocalDateTime.class, String.class, String.class, int.class, int.class, java.time.LocalDateTime.class, String.class, String.class, int.class, String.class, int.class, int.class, String.class, String.class, String.class, String.class}, orderNumber, detailOptionTitle, seq, productClassificationCode, productName, quantity, orderedAt, buyMemberLoginId, message, deliveryPrice, dangerPlacePrice, completePaymentAt, receiverPhone, creditStatementNumber, creditApprovalPrice, onNuryStatementNumber, onNuryApprovalPrice, totalApprovalPrice, transportNumber, parcelName, receiver, address);
    }

}

