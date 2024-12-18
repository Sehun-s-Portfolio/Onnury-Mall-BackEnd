package com.onnury.cart.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductInCart is a Querydsl query type for ProductInCart
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductInCart extends EntityPathBase<ProductInCart> {

    private static final long serialVersionUID = -383802684L;

    public static final QProductInCart productInCart = new QProductInCart("productInCart");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Long> cartId = createNumber("cartId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> productDetailOptionId = createNumber("productDetailOptionId", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> productInCartId = createNumber("productInCartId", Long.class);

    public final NumberPath<Long> productOptionId = createNumber("productOptionId", Long.class);

    public QProductInCart(String variable) {
        super(ProductInCart.class, forVariable(variable));
    }

    public QProductInCart(Path<? extends ProductInCart> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductInCart(PathMetadata metadata) {
        super(ProductInCart.class, metadata);
    }

}

