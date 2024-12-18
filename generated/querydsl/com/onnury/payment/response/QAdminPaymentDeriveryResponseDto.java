package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminPaymentDeriveryResponseDto is a Querydsl Projection type for AdminPaymentDeriveryResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminPaymentDeriveryResponseDto extends ConstructorExpression<AdminPaymentDeriveryResponseDto> {

    private static final long serialVersionUID = -471152920L;

    public QAdminPaymentDeriveryResponseDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> seq, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<String> supplierId, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<Integer> onnuryPay, com.querydsl.core.types.Expression<Integer> creditPay, com.querydsl.core.types.Expression<Integer> amount, com.querydsl.core.types.Expression<String> cancleStatus, com.querydsl.core.types.Expression<String> frcNumber, com.querydsl.core.types.Expression<String> businessNumber) {
        super(AdminPaymentDeriveryResponseDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, int.class, int.class, String.class, String.class, String.class}, orderNumber, seq, productName, supplierId, supplierName, onnuryPay, creditPay, amount, cancleStatus, frcNumber, businessNumber);
    }

}

