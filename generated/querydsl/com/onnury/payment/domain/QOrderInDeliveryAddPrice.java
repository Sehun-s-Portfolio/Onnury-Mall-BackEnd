package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderInDeliveryAddPrice is a Querydsl query type for OrderInDeliveryAddPrice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderInDeliveryAddPrice extends EntityPathBase<OrderInDeliveryAddPrice> {

    private static final long serialVersionUID = 1220361409L;

    public static final QOrderInDeliveryAddPrice orderInDeliveryAddPrice = new QOrderInDeliveryAddPrice("orderInDeliveryAddPrice");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final StringPath businessNumber = createString("businessNumber");

    public final StringPath cancleStatus = createString("cancleStatus");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> creditPay = createNumber("creditPay", Integer.class);

    public final StringPath frcNumber = createString("frcNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> onnuryPay = createNumber("onnuryPay", Integer.class);

    public final NumberPath<Long> orderInDeliveryAddPriceId = createNumber("orderInDeliveryAddPriceId", Long.class);

    public final StringPath orderNumber = createString("orderNumber");

    public final StringPath productName = createString("productName");

    public final StringPath seq = createString("seq");

    public final StringPath supplierId = createString("supplierId");

    public QOrderInDeliveryAddPrice(String variable) {
        super(OrderInDeliveryAddPrice.class, forVariable(variable));
    }

    public QOrderInDeliveryAddPrice(Path<? extends OrderInDeliveryAddPrice> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderInDeliveryAddPrice(PathMetadata metadata) {
        super(OrderInDeliveryAddPrice.class, metadata);
    }

}

