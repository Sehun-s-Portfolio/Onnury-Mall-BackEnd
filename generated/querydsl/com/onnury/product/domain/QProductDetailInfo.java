package com.onnury.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductDetailInfo is a Querydsl query type for ProductDetailInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductDetailInfo extends EntityPathBase<ProductDetailInfo> {

    private static final long serialVersionUID = 1119688183L;

    public static final QProductDetailInfo productDetailInfo = new QProductDetailInfo("productDetailInfo");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> productDetailInfoId = createNumber("productDetailInfoId", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public QProductDetailInfo(String variable) {
        super(ProductDetailInfo.class, forVariable(variable));
    }

    public QProductDetailInfo(Path<? extends ProductDetailInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductDetailInfo(PathMetadata metadata) {
        super(ProductDetailInfo.class, metadata);
    }

}

