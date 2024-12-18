package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentZppReq is a Querydsl query type for PaymentZppReq
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentZppReq extends EntityPathBase<PaymentZppReq> {

    private static final long serialVersionUID = 1571374780L;

    public static final QPaymentZppReq paymentZppReq = new QPaymentZppReq("paymentZppReq");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> paymentApprovalId = createNumber("paymentApprovalId", Long.class);

    public final NumberPath<Long> paymentZppReqId = createNumber("paymentZppReqId", Long.class);

    public final StringPath sellerID = createString("sellerID");

    public final StringPath sellerName = createString("sellerName");

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public final StringPath zppID = createString("zppID");

    public final StringPath zppName = createString("zppName");

    public QPaymentZppReq(String variable) {
        super(PaymentZppReq.class, forVariable(variable));
    }

    public QPaymentZppReq(Path<? extends PaymentZppReq> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentZppReq(PathMetadata metadata) {
        super(PaymentZppReq.class, metadata);
    }

}

