package com.onnury.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductOfOption is a Querydsl query type for ProductOfOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOfOption extends EntityPathBase<ProductOfOption> {

    private static final long serialVersionUID = -730746748L;

    public static final QProductOfOption productOfOption = new QProductOfOption("productOfOption");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> productOfOptionId = createNumber("productOfOptionId", Long.class);

    public final NumberPath<Long> productOptionId = createNumber("productOptionId", Long.class);

    public QProductOfOption(String variable) {
        super(ProductOfOption.class, forVariable(variable));
    }

    public QProductOfOption(Path<? extends ProductOfOption> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductOfOption(PathMetadata metadata) {
        super(ProductOfOption.class, metadata);
    }

}

