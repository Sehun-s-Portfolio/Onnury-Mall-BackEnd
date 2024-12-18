package com.onnury.link.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLink is a Querydsl query type for Link
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLink extends EntityPathBase<Link> {

    private static final long serialVersionUID = -312113980L;

    public static final QLink link1 = new QLink("link1");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath link = createString("link");

    public final StringPath linkCompany = createString("linkCompany");

    public final NumberPath<Long> linkId = createNumber("linkId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath type = createString("type");

    public QLink(String variable) {
        super(Link.class, forVariable(variable));
    }

    public QLink(Path<? extends Link> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLink(PathMetadata metadata) {
        super(Link.class, metadata);
    }

}

