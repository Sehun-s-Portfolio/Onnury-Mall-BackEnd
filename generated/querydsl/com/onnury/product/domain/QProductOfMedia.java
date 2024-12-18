package com.onnury.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductOfMedia is a Querydsl query type for ProductOfMedia
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOfMedia extends EntityPathBase<ProductOfMedia> {

    private static final long serialVersionUID = 1359710709L;

    public static final QProductOfMedia productOfMedia = new QProductOfMedia("productOfMedia");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> mediaId = createNumber("mediaId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> productOfMediaId = createNumber("productOfMediaId", Long.class);

    public QProductOfMedia(String variable) {
        super(ProductOfMedia.class, forVariable(variable));
    }

    public QProductOfMedia(Path<? extends ProductOfMedia> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductOfMedia(PathMetadata metadata) {
        super(ProductOfMedia.class, metadata);
    }

}

