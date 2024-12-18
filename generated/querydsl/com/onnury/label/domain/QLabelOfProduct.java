package com.onnury.label.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLabelOfProduct is a Querydsl query type for LabelOfProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLabelOfProduct extends EntityPathBase<LabelOfProduct> {

    private static final long serialVersionUID = 1976821600L;

    public static final QLabelOfProduct labelOfProduct = new QLabelOfProduct("labelOfProduct");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> labelId = createNumber("labelId", Long.class);

    public final NumberPath<Long> labelOfProductId = createNumber("labelOfProductId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public QLabelOfProduct(String variable) {
        super(LabelOfProduct.class, forVariable(variable));
    }

    public QLabelOfProduct(Path<? extends LabelOfProduct> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLabelOfProduct(PathMetadata metadata) {
        super(LabelOfProduct.class, metadata);
    }

}

