package com.onnury.category.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCategoryInBrand is a Querydsl query type for CategoryInBrand
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategoryInBrand extends EntityPathBase<CategoryInBrand> {

    private static final long serialVersionUID = 1854181366L;

    public static final QCategoryInBrand categoryInBrand = new QCategoryInBrand("categoryInBrand");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final NumberPath<Long> category1Id = createNumber("category1Id", Long.class);

    public final NumberPath<Long> category2Id = createNumber("category2Id", Long.class);

    public final NumberPath<Long> category3Id = createNumber("category3Id", Long.class);

    public final NumberPath<Long> categoryInBrandId = createNumber("categoryInBrandId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public QCategoryInBrand(String variable) {
        super(CategoryInBrand.class, forVariable(variable));
    }

    public QCategoryInBrand(Path<? extends CategoryInBrand> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategoryInBrand(PathMetadata metadata) {
        super(CategoryInBrand.class, metadata);
    }

}

