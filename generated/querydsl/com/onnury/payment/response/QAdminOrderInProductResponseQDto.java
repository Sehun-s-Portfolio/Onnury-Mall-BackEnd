package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminOrderInProductResponseQDto is a Querydsl Projection type for AdminOrderInProductResponseQDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminOrderInProductResponseQDto extends ConstructorExpression<AdminOrderInProductResponseQDto> {

    private static final long serialVersionUID = 814522249L;

    public QAdminOrderInProductResponseQDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> linkCompany, com.querydsl.core.types.Expression<String> supplierName, com.querydsl.core.types.Expression<Long> supplierId, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<String> orderedAt, com.querydsl.core.types.Expression<String> creditStatementNumber, com.querydsl.core.types.Expression<Integer> creditApprovalPrice, com.querydsl.core.types.Expression<String> onNuryStatementNumber, com.querydsl.core.types.Expression<Integer> onNuryApprovalPrice, com.querydsl.core.types.Expression<Integer> creditCanclePrice, com.querydsl.core.types.Expression<Integer> onNuryCanclePrice, com.querydsl.core.types.Expression<Double> onnuryCommission, com.querydsl.core.types.Expression<Double> creditCommission, com.querydsl.core.types.Expression<Integer> onnuryCommissionPrice, com.querydsl.core.types.Expression<Integer> creditCommissionPrice, com.querydsl.core.types.Expression<String> eventCheck, com.querydsl.core.types.Expression<String> eventInfo, com.querydsl.core.types.Expression<Integer> deliveryAddPrice, com.querydsl.core.types.Expression<String> completePurchaseAt, com.querydsl.core.types.Expression<String> completePurchaseCheck, com.querydsl.core.types.Expression<String> parcelName, com.querydsl.core.types.Expression<String> transportNumber, com.querydsl.core.types.Expression<String> transportCheck) {
        super(AdminOrderInProductResponseQDto.class, new Class<?>[]{String.class, String.class, String.class, long.class, String.class, String.class, String.class, int.class, String.class, String.class, int.class, String.class, int.class, int.class, int.class, double.class, double.class, int.class, int.class, String.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class}, orderNumber, linkCompany, supplierName, supplierId, detailOptionTitle, productClassificationCode, productName, quantity, orderedAt, creditStatementNumber, creditApprovalPrice, onNuryStatementNumber, onNuryApprovalPrice, creditCanclePrice, onNuryCanclePrice, onnuryCommission, creditCommission, onnuryCommissionPrice, creditCommissionPrice, eventCheck, eventInfo, deliveryAddPrice, completePurchaseAt, completePurchaseCheck, parcelName, transportNumber, transportCheck);
    }

}

