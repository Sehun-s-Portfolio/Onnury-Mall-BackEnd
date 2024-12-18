package com.onnury.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -1023377608L;

    public static final QProduct product = new QProduct("product");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Long> categoryInBrandId = createNumber("categoryInBrandId", Long.class);

    public final StringPath classificationCode = createString("classificationCode");

    public final StringPath consignmentStore = createString("consignmentStore");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> deliveryPrice = createNumber("deliveryPrice", Integer.class);

    public final StringPath deliveryType = createString("deliveryType");

    public final StringPath eventDescription = createString("eventDescription");

    public final DateTimePath<java.time.LocalDateTime> eventEndDate = createDateTime("eventEndDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> eventPrice = createNumber("eventPrice", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> eventStartDate = createDateTime("eventStartDate", java.time.LocalDateTime.class);

    public final StringPath expressionCheck = createString("expressionCheck");

    public final StringPath madeInOrigin = createString("madeInOrigin");

    public final StringPath manufacturer = createString("manufacturer");

    public final StringPath memo = createString("memo");

    public final StringPath modelNumber = createString("modelNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> normalPrice = createNumber("normalPrice", Integer.class);

    public final StringPath optionCheck = createString("optionCheck");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final StringPath productName = createString("productName");

    public final NumberPath<Integer> purchasePrice = createNumber("purchasePrice", Integer.class);

    public final StringPath relateImgIds = createString("relateImgIds");

    public final StringPath sellClassification = createString("sellClassification");

    public final NumberPath<Integer> sellPrice = createNumber("sellPrice", Integer.class);

    public final StringPath status = createString("status");

    public final NumberPath<Long> supplierId = createNumber("supplierId", Long.class);

    public QProduct(String variable) {
        super(Product.class, forVariable(variable));
    }

    public QProduct(Path<? extends Product> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProduct(PathMetadata metadata) {
        super(Product.class, metadata);
    }

}

