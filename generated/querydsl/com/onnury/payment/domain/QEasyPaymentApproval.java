package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QEasyPaymentApproval is a Querydsl query type for EasyPaymentApproval
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEasyPaymentApproval extends EntityPathBase<EasyPaymentApproval> {

    private static final long serialVersionUID = 468571993L;

    public static final QEasyPaymentApproval easyPaymentApproval = new QEasyPaymentApproval("easyPaymentApproval");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath accountNo = createString("accountNo");

    public final StringPath acquirerCode = createString("acquirerCode");

    public final StringPath acquirerName = createString("acquirerName");

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final StringPath approvalDate = createString("approvalDate");

    public final StringPath approvalNo = createString("approvalNo");

    public final StringPath authId = createString("authId");

    public final StringPath bankCode = createString("bankCode");

    public final StringPath bankName = createString("bankName");

    public final StringPath cardBizGubun = createString("cardBizGubun");

    public final StringPath cardGubun = createString("cardGubun");

    public final StringPath cardNo = createString("cardNo");

    public final StringPath cashReceiptApprovalDate = createString("cashReceiptApprovalDate");

    public final StringPath cashReceiptApprovalNo = createString("cashReceiptApprovalNo");

    public final StringPath cashReceiptResCd = createString("cashReceiptResCd");

    public final StringPath cashReceiptResMsg = createString("cashReceiptResMsg");

    public final NumberPath<Long> couponAmount = createNumber("couponAmount", Long.class);

    public final StringPath cpCode = createString("cpCode");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath depositName = createString("depositName");

    public final NumberPath<Long> easyPaymentApprovalId = createNumber("easyPaymentApprovalId", Long.class);

    public final StringPath escrowUsed = createString("escrowUsed");

    public final StringPath expiryDate = createString("expiryDate");

    public final StringPath freeInstallmentTypeCode = createString("freeInstallmentTypeCode");

    public final StringPath installmentMonth = createString("installmentMonth");

    public final StringPath issuerCode = createString("issuerCode");

    public final StringPath issuerName = createString("issuerName");

    public final StringPath mallId = createString("mallId");

    public final StringPath mobBillId = createString("mobBillId");

    public final StringPath mobileAnsimUsed = createString("mobileAnsimUsed");

    public final StringPath mobileNo = createString("mobileNo");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath msgAuthValue = createString("msgAuthValue");

    public final StringPath multiCardAmount = createString("multiCardAmount");

    public final StringPath multiCponAmount = createString("multiCponAmount");

    public final StringPath multiPntAmount = createString("multiPntAmount");

    public final StringPath partCancelUsed = createString("partCancelUsed");

    public final StringPath payMethodTypeCode = createString("payMethodTypeCode");

    public final StringPath pgCno = createString("pgCno");

    public final StringPath prepaidBillId = createString("prepaidBillId");

    public final NumberPath<Long> prepaidRemainAmount = createNumber("prepaidRemainAmount", Long.class);

    public final StringPath shopOrderNo = createString("shopOrderNo");

    public final StringPath shopTransactionId = createString("shopTransactionId");

    public final StringPath statusCode = createString("statusCode");

    public final StringPath statusMessage = createString("statusMessage");

    public final StringPath subCardCd = createString("subCardCd");

    public final StringPath transactionDate = createString("transactionDate");

    public final StringPath vanSno = createString("vanSno");

    public final StringPath virtualBankCode = createString("virtualBankCode");

    public final StringPath virtualBankName = createString("virtualBankName");

    public QEasyPaymentApproval(String variable) {
        super(EasyPaymentApproval.class, forVariable(variable));
    }

    public QEasyPaymentApproval(Path<? extends EasyPaymentApproval> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEasyPaymentApproval(PathMetadata metadata) {
        super(EasyPaymentApproval.class, metadata);
    }

}

