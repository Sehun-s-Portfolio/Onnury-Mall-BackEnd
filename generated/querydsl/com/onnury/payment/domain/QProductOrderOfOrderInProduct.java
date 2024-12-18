package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductOrderOfOrderInProduct is a Querydsl query type for ProductOrderOfOrderInProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOrderOfOrderInProduct extends EntityPathBase<ProductOrderOfOrderInProduct> {

    private static final long serialVersionUID = -2138516800L;

    public static final QProductOrderOfOrderInProduct productOrderOfOrderInProduct = new QProductOrderOfOrderInProduct("productOrderOfOrderInProduct");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> orderInProductId = createNumber("orderInProductId", Long.class);

    public final NumberPath<Long> productOrderId = createNumber("productOrderId", Long.class);

    public final NumberPath<Long> productOrderOfOrderInProductId = createNumber("productOrderOfOrderInProductId", Long.class);

    public QProductOrderOfOrderInProduct(String variable) {
        super(ProductOrderOfOrderInProduct.class, forVariable(variable));
    }

    public QProductOrderOfOrderInProduct(Path<? extends ProductOrderOfOrderInProduct> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductOrderOfOrderInProduct(PathMetadata metadata) {
        super(ProductOrderOfOrderInProduct.class, metadata);
    }

}

