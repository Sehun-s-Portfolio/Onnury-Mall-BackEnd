package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminPaymentResponse2Dto is a Querydsl Projection type for AdminPaymentResponse2Dto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminPaymentResponse2Dto extends ConstructorExpression<AdminPaymentResponse2Dto> {

    private static final long serialVersionUID = 1612950902L;

    public QAdminPaymentResponse2Dto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> linkCompany, com.querydsl.core.types.Expression<String> supplierCode, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<String> transportNumber, com.querydsl.core.types.Expression<String> parcelName, com.querydsl.core.types.Expression<String> receiver, com.querydsl.core.types.Expression<String> Address, com.querydsl.core.types.Expression<String> orderedAt, com.querydsl.core.types.Expression<String> buyMemberLoginId, com.querydsl.core.types.Expression<String> buyMemberName, com.querydsl.core.types.Expression<Integer> cancelAmount, com.querydsl.core.types.Expression<Integer> creditCommissionPrice, com.querydsl.core.types.Expression<Integer> onnuryCommissionPrice, com.querydsl.core.types.Expression<String> eventCheck, com.querydsl.core.types.Expression<String> eventInfo, com.querydsl.core.types.Expression<String> completePurchaseAt, com.querydsl.core.types.Expression<String> completePurchaseCheck) {
        super(AdminPaymentResponse2Dto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, int.class, int.class, int.class, String.class, String.class, String.class, String.class}, orderNumber, linkCompany, supplierCode, supplierName, detailOptionTitle, productClassificationCode, productName, quantity, transportNumber, parcelName, receiver, Address, orderedAt, buyMemberLoginId, buyMemberName, cancelAmount, creditCommissionPrice, onnuryCommissionPrice, eventCheck, eventInfo, completePurchaseAt, completePurchaseCheck);
    }

}

