package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminPaymentProductResponseDto is a Querydsl Projection type for AdminPaymentProductResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminPaymentProductResponseDto extends ConstructorExpression<AdminPaymentProductResponseDto> {

    private static final long serialVersionUID = 1361637591L;

    public QAdminPaymentProductResponseDto(com.querydsl.core.types.Expression<String> seq, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<String> orderedAt, com.querydsl.core.types.Expression<String> buyMemberLoginId, com.querydsl.core.types.Expression<String> creditStatementNumber, com.querydsl.core.types.Expression<Integer> creditApprovalPrice, com.querydsl.core.types.Expression<String> onNuryStatementNumber, com.querydsl.core.types.Expression<Integer> onNuryApprovalPrice, com.querydsl.core.types.Expression<Integer> deliveryPrice, com.querydsl.core.types.Expression<Integer> dangerPlacePrice, com.querydsl.core.types.Expression<Integer> totalApprovalPrice, com.querydsl.core.types.Expression<String> parcelName, com.querydsl.core.types.Expression<String> transportNumber, com.querydsl.core.types.Expression<Integer> cancelAmount, com.querydsl.core.types.Expression<Integer> creditCommissionPrice, com.querydsl.core.types.Expression<Integer> onnuryCommissionPrice, com.querydsl.core.types.Expression<String> eventCheck, com.querydsl.core.types.Expression<String> eventInfo) {
        super(AdminPaymentProductResponseDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, String.class, String.class, String.class, int.class, String.class, int.class, int.class, int.class, int.class, String.class, String.class, int.class, int.class, int.class, String.class, String.class}, seq, supplierName, detailOptionTitle, productClassificationCode, productName, quantity, orderedAt, buyMemberLoginId, creditStatementNumber, creditApprovalPrice, onNuryStatementNumber, onNuryApprovalPrice, deliveryPrice, dangerPlacePrice, totalApprovalPrice, parcelName, transportNumber, cancelAmount, creditCommissionPrice, onnuryCommissionPrice, eventCheck, eventInfo);
    }

}

