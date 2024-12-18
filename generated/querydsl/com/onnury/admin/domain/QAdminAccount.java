package com.onnury.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminAccount is a Querydsl query type for AdminAccount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminAccount extends EntityPathBase<AdminAccount> {

    private static final long serialVersionUID = 945042581L;

    public static final QAdminAccount adminAccount = new QAdminAccount("adminAccount");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final NumberPath<Long> adminAccountId = createNumber("adminAccountId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath loginId = createString("loginId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath password = createString("password");

    public final ListPath<String, StringPath> roles = this.<String, StringPath>createList("roles", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath type = createString("type");

    public QAdminAccount(String variable) {
        super(AdminAccount.class, forVariable(variable));
    }

    public QAdminAccount(Path<? extends AdminAccount> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdminAccount(PathMetadata metadata) {
        super(AdminAccount.class, metadata);
    }

}

