package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminPaymentResponse3Dto is a Querydsl Projection type for AdminPaymentResponse3Dto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminPaymentResponse3Dto extends ConstructorExpression<AdminPaymentResponse3Dto> {

    private static final long serialVersionUID = 1612980693L;

    public QAdminPaymentResponse3Dto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> supplierId, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<String> orderedAt, com.querydsl.core.types.Expression<String> buyMemberLoginId, com.querydsl.core.types.Expression<String> cancelCheck, com.querydsl.core.types.Expression<String> cancelAt, com.querydsl.core.types.Expression<String> seq) {
        super(AdminPaymentResponse3Dto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class}, orderNumber, supplierId, supplierName, detailOptionTitle, productClassificationCode, productName, quantity, orderedAt, buyMemberLoginId, cancelCheck, cancelAt, seq);
    }

}

