package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentApproval is a Querydsl query type for PaymentApproval
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentApproval extends EntityPathBase<PaymentApproval> {

    private static final long serialVersionUID = 1966915771L;

    public static final QPaymentApproval paymentApproval = new QPaymentApproval("paymentApproval");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath accountNo = createString("accountNo");

    public final StringPath approvedAt = createString("approvedAt");

    public final StringPath bankCd = createString("bankCd");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath merchantOrderDt = createString("merchantOrderDt");

    public final StringPath merchantOrderID = createString("merchantOrderID");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath payMeasureTp = createString("payMeasureTp");

    public final NumberPath<Long> paymentApprovalId = createNumber("paymentApprovalId", Long.class);

    public final StringPath payZppNote = createString("payZppNote");

    public final StringPath productName = createString("productName");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Integer> taxFreeAmount = createNumber("taxFreeAmount", Integer.class);

    public final StringPath tid = createString("tid");

    public final NumberPath<Integer> totalAmount = createNumber("totalAmount", Integer.class);

    public final NumberPath<Integer> vatAmount = createNumber("vatAmount", Integer.class);

    public QPaymentApproval(String variable) {
        super(PaymentApproval.class, forVariable(variable));
    }

    public QPaymentApproval(Path<? extends PaymentApproval> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentApproval(PathMetadata metadata) {
        super(PaymentApproval.class, metadata);
    }

}

