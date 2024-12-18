package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderInProduct is a Querydsl query type for OrderInProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderInProduct extends EntityPathBase<OrderInProduct> {

    private static final long serialVersionUID = -2137888822L;

    public static final QOrderInProduct orderInProduct = new QOrderInProduct("orderInProduct");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath businessNumber = createString("businessNumber");

    public final NumberPath<Integer> cancelAmount = createNumber("cancelAmount", Integer.class);

    public final NumberPath<Long> cartId = createNumber("cartId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> completePurchaseAt = createDateTime("completePurchaseAt", java.time.LocalDateTime.class);

    public final StringPath completePurchaseCheck = createString("completePurchaseCheck");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> creditCommissionPrice = createNumber("creditCommissionPrice", Integer.class);

    public final NumberPath<Integer> dangerPlacePrice = createNumber("dangerPlacePrice", Integer.class);

    public final NumberPath<Integer> deliveryPrice = createNumber("deliveryPrice", Integer.class);

    public final StringPath detailOptionTitle = createString("detailOptionTitle");

    public final StringPath eventCheck = createString("eventCheck");

    public final StringPath eventInfo = createString("eventInfo");

    public final StringPath frcNumber = createString("frcNumber");

    public final StringPath memo = createString("memo");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> onnuryCommissionPrice = createNumber("onnuryCommissionPrice", Integer.class);

    public final NumberPath<Integer> onnurypay = createNumber("onnurypay", Integer.class);

    public final NumberPath<Long> orderInProductId = createNumber("orderInProductId", Long.class);

    public final StringPath orderNumber = createString("orderNumber");

    public final StringPath parcelName = createString("parcelName");

    public final NumberPath<Integer> productAmount = createNumber("productAmount", Integer.class);

    public final StringPath productClassificationCode = createString("productClassificationCode");

    public final StringPath productName = createString("productName");

    public final NumberPath<Integer> productOptionAmount = createNumber("productOptionAmount", Integer.class);

    public final NumberPath<Integer> productTotalAmount = createNumber("productTotalAmount", Integer.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final StringPath seq = createString("seq");

    public final StringPath supplierId = createString("supplierId");

    public final StringPath transportNumber = createString("transportNumber");

    public QOrderInProduct(String variable) {
        super(OrderInProduct.class, forVariable(variable));
    }

    public QOrderInProduct(Path<? extends OrderInProduct> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderInProduct(PathMetadata metadata) {
        super(OrderInProduct.class, metadata);
    }

}

