package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductOrder is a Querydsl query type for ProductOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOrder extends EntityPathBase<ProductOrder> {

    private static final long serialVersionUID = 1136447117L;

    public static final QProductOrder productOrder = new QProductOrder("productOrder");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath Address = createString("Address");

    public final StringPath buyMemberLoginId = createString("buyMemberLoginId");

    public final DateTimePath<java.time.LocalDateTime> completePaymentAt = createDateTime("completePaymentAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath message = createString("message");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final DateTimePath<java.time.LocalDateTime> orderedAt = createDateTime("orderedAt", java.time.LocalDateTime.class);

    public final StringPath orderNumber = createString("orderNumber");

    public final StringPath postNumber = createString("postNumber");

    public final NumberPath<Long> proudctOrderId = createNumber("proudctOrderId", Long.class);

    public final StringPath receiver = createString("receiver");

    public final StringPath receiverPhone = createString("receiverPhone");

    public final NumberPath<Integer> totalPurchasePrice = createNumber("totalPurchasePrice", Integer.class);

    public QProductOrder(String variable) {
        super(ProductOrder.class, forVariable(variable));
    }

    public QProductOrder(Path<? extends ProductOrder> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductOrder(PathMetadata metadata) {
        super(ProductOrder.class, metadata);
    }

}

