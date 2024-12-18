package com.onnury.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductDetailOption is a Querydsl query type for ProductDetailOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductDetailOption extends EntityPathBase<ProductDetailOption> {

    private static final long serialVersionUID = -1842410626L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductDetailOption productDetailOption = new QProductDetailOption("productDetailOption");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath detailOptionName = createString("detailOptionName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> optionPrice = createNumber("optionPrice", Integer.class);

    public final NumberPath<Long> productDetailOptionId = createNumber("productDetailOptionId", Long.class);

    public final QProductOption productOption;

    public QProductDetailOption(String variable) {
        this(ProductDetailOption.class, forVariable(variable), INITS);
    }

    public QProductDetailOption(Path<? extends ProductDetailOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductDetailOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductDetailOption(PathMetadata metadata, PathInits inits) {
        this(ProductDetailOption.class, metadata, inits);
    }

    public QProductDetailOption(Class<? extends ProductDetailOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.productOption = inits.isInitialized("productOption") ? new QProductOption(forProperty("productOption")) : null;
    }

}

