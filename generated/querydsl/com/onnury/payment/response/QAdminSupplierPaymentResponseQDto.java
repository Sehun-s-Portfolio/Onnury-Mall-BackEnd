package com.onnury.payment.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.Generated;

/**
 * com.onnury.payment.response.QAdminSupplierPaymentResponseQDto is a Querydsl Projection type for AdminSupplierPaymentResponseQDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAdminSupplierPaymentResponseQDto extends ConstructorExpression<AdminSupplierPaymentResponseQDto> {

    private static final long serialVersionUID = 22832523L;

    public QAdminSupplierPaymentResponseQDto(com.querydsl.core.types.Expression<String> orderNumber, com.querydsl.core.types.Expression<String> supplierId, com.querydsl.core.types.Expression<String> detailOptionTitle, com.querydsl.core.types.Expression<String> productClassificationCode, com.querydsl.core.types.Expression<String> productName, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<java.time.LocalDateTime> orderedAt, com.querydsl.core.types.Expression<String> buyMemberLoginId, com.querydsl.core.types.Expression<String> creditStatementNumber, com.querydsl.core.types.Expression<Integer> creditApprovalPrice, com.querydsl.core.types.Expression<String> onNuryStatementNumber, com.querydsl.core.types.Expression<Integer> onNuryApprovalPrice, com.querydsl.core.types.Expression<String> cancleorderNumber, com.querydsl.core.types.Expression<String> canclesupplierId, com.querydsl.core.types.Expression<String> cancleproductName, com.querydsl.core.types.Expression<String> cancleproductClassificationCode, com.querydsl.core.types.Expression<String> cancledetailOptionTitle, com.querydsl.core.types.Expression<Integer> canclecancelAmount, com.querydsl.core.types.Expression<String> cancleonNuryStatementNumber, com.querydsl.core.types.Expression<Integer> cancleonNuryCanclePrice, com.querydsl.core.types.Expression<String> canclecreditStatementNumber, com.querydsl.core.types.Expression<Integer> canclecreditCanclePrice, com.querydsl.core.types.Expression<java.time.LocalDateTime> cancelRequestAt) {
        super(AdminSupplierPaymentResponseQDto.class, new Class<?>[]{String.class, String.class, String.class, String.class, String.class, int.class, java.time.LocalDateTime.class, String.class, String.class, int.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class, int.class, String.class, int.class, String.class, int.class, java.time.LocalDateTime.class}, orderNumber, supplierId, detailOptionTitle, productClassificationCode, productName, quantity, orderedAt, buyMemberLoginId, creditStatementNumber, creditApprovalPrice, onNuryStatementNumber, onNuryApprovalPrice, cancleorderNumber, canclesupplierId, cancleproductName, cancleproductClassificationCode, cancledetailOptionTitle, canclecancelAmount, cancleonNuryStatementNumber, cancleonNuryCanclePrice, canclecreditStatementNumber, canclecreditCanclePrice, cancelRequestAt);
    }

}

