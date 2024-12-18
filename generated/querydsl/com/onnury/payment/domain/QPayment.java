package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = 1172622936L;

    public static final QPayment payment = new QPayment("payment");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath address = createString("address");

    public final StringPath buyMemberLoginId = createString("buyMemberLoginId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> creditApprovalPrice = createNumber("creditApprovalPrice", Integer.class);

    public final StringPath creditStatementNumber = createString("creditStatementNumber");

    public final StringPath linkCompany = createString("linkCompany");

    public final StringPath message = createString("message");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> onNuryApprovalPrice = createNumber("onNuryApprovalPrice", Integer.class);

    public final StringPath onNuryStatementNumber = createString("onNuryStatementNumber");

    public final DateTimePath<java.time.LocalDateTime> orderedAt = createDateTime("orderedAt", java.time.LocalDateTime.class);

    public final StringPath orderNumber = createString("orderNumber");

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final StringPath postNumber = createString("postNumber");

    public final StringPath receiver = createString("receiver");

    public final StringPath receiverPhone = createString("receiverPhone");

    public final NumberPath<Integer> totalApprovalPrice = createNumber("totalApprovalPrice", Integer.class);

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

