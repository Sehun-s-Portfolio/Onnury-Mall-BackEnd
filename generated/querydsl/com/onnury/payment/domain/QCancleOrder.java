package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCancleOrder is a Querydsl query type for CancleOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCancleOrder extends EntityPathBase<CancleOrder> {

    private static final long serialVersionUID = 1936210612L;

    public static final QCancleOrder cancleOrder = new QCancleOrder("cancleOrder");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Integer> cancelAmount = createNumber("cancelAmount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> cancelAt = createDateTime("cancelAt", java.time.LocalDateTime.class);

    public final StringPath cancelCheck = createString("cancelCheck");

    public final DateTimePath<java.time.LocalDateTime> cancelRequestAt = createDateTime("cancelRequestAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> cancleOrderId = createNumber("cancleOrderId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> creditCanclePrice = createNumber("creditCanclePrice", Integer.class);

    public final StringPath creditStatementNumber = createString("creditStatementNumber");

    public final NumberPath<Integer> dangerPlacePrice = createNumber("dangerPlacePrice", Integer.class);

    public final NumberPath<Integer> deliveryPrice = createNumber("deliveryPrice", Integer.class);

    public final StringPath detailOptionTitle = createString("detailOptionTitle");

    public final StringPath linkCompany = createString("linkCompany");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> onNuryCanclePrice = createNumber("onNuryCanclePrice", Integer.class);

    public final StringPath onNuryStatementNumber = createString("onNuryStatementNumber");

    public final StringPath orderNumber = createString("orderNumber");

    public final NumberPath<Integer> productAmount = createNumber("productAmount", Integer.class);

    public final StringPath productClassificationCode = createString("productClassificationCode");

    public final StringPath productName = createString("productName");

    public final NumberPath<Integer> productOptionAmount = createNumber("productOptionAmount", Integer.class);

    public final StringPath seq = createString("seq");

    public final StringPath supplierId = createString("supplierId");

    public final NumberPath<Integer> totalPrice = createNumber("totalPrice", Integer.class);

    public QCancleOrder(String variable) {
        super(CancleOrder.class, forVariable(variable));
    }

    public QCancleOrder(Path<? extends CancleOrder> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCancleOrder(PathMetadata metadata) {
        super(CancleOrder.class, metadata);
    }

}

