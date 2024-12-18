package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminOrderInProductResponseQDto2 is a Querydsl Projection type for AdminOrderInProductResponseQDto2
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminOrderInProductResponseQDto2 extends ConstructorExpression<AdminOrderInProductResponseQDto2> {

    private static final long serialVersionUID = -519614007L;

    public QAdminOrderInProductResponseQDto2(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> linkCompany, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<String> orderedAt, com.querydsl.core.types.Expression<String> creditStatementNumber, com.querydsl.core.types.Expression<Integer> creditApprovalPrice, com.querydsl.core.types.Expression<String> onNuryStatementNumber, com.querydsl.core.types.Expression<Integer> onNuryApprovalPrice, com.querydsl.core.types.Expression<Integer> creditCanclePrice, com.querydsl.core.types.Expression<Integer> onNuryCanclePrice, com.querydsl.core.types.Expression<Integer> onnuryCommission, com.querydsl.core.types.Expression<Integer> creditCommission, com.querydsl.core.types.Expression<Integer> onnuryCommissionPrice, com.querydsl.core.types.Expression<Integer> creditCommissionPrice, com.querydsl.core.types.Expression<String> eventCheck, com.querydsl.core.types.Expression<String> eventInfo) {
        super(AdminOrderInProductResponseQDto2.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, int.class, String.class, String.class, int.class, String.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, String.class, String.class}, orderNumber, linkCompany, supplierName, detailOptionTitle, productClassificationCode, productName, quantity, orderedAt, creditStatementNumber, creditApprovalPrice, onNuryStatementNumber, onNuryApprovalPrice, creditCanclePrice, onNuryCanclePrice, onnuryCommission, creditCommission, onnuryCommissionPrice, creditCommissionPrice, eventCheck, eventInfo);
    }

}

