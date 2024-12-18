package com.onnury.supplier.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QSupplier is a Querydsl query type for Supplier
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSupplier extends EntityPathBase<Supplier> {

    private static final long serialVersionUID = 72336744L;

    public static final QSupplier supplier = new QSupplier("supplier");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath address = createString("address");

    public final NumberPath<Long> adminAccountId = createNumber("adminAccountId", Long.class);

    public final StringPath bcryptPassword = createString("bcryptPassword");

    public final StringPath businessNumber = createString("businessNumber");

    public final StringPath contactCall = createString("contactCall");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Double> creditCommission = createNumber("creditCommission", Double.class);

    public final StringPath cscall = createString("cscall");

    public final StringPath csInfo = createString("csInfo");

    public final StringPath email = createString("email");

    public final StringPath frcNumber = createString("frcNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Double> onnuryCommission = createNumber("onnuryCommission", Double.class);

    public final StringPath personInCharge = createString("personInCharge");

    public final StringPath recalladdress = createString("recalladdress");

    public final StringPath represent = createString("represent");

    public final StringPath status = createString("status");

    public final StringPath supplierCompany = createString("supplierCompany");

    public final NumberPath<Long> supplierId = createNumber("supplierId", Long.class);

    public final StringPath tel = createString("tel");

    public QSupplier(String variable) {
        super(Supplier.class, forVariable(variable));
    }

    public QSupplier(Path<? extends Supplier> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSupplier(PathMetadata metadata) {
        super(Supplier.class, metadata);
    }

}

