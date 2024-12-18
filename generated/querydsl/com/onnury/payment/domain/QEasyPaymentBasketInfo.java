package com.onnury.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QEasyPaymentBasketInfo is a Querydsl query type for EasyPaymentBasketInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEasyPaymentBasketInfo extends EntityPathBase<EasyPaymentBasketInfo> {

    private static final long serialVersionUID = -801324246L;

    public static final QEasyPaymentBasketInfo easyPaymentBasketInfo = new QEasyPaymentBasketInfo("easyPaymentBasketInfo");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> easyPaymentApprovalId = createNumber("easyPaymentApprovalId", Long.class);

    public final NumberPath<Long> easyPaymentBasketInfoId = createNumber("easyPaymentBasketInfoId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath productNo = createString("productNo");

    public final StringPath productPgCno = createString("productPgCno");

    public final StringPath sellerId = createString("sellerId");

    public QEasyPaymentBasketInfo(String variable) {
        super(EasyPaymentBasketInfo.class, forVariable(variable));
    }

    public QEasyPaymentBasketInfo(Path<? extends EasyPaymentBasketInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEasyPaymentBasketInfo(PathMetadata metadata) {
        super(EasyPaymentBasketInfo.class, metadata);
    }

}

