package com.onnury.label.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLabel is a Querydsl query type for Label
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLabel extends EntityPathBase<Label> {

    private static final long serialVersionUID = -22082920L;

    public static final QLabel label = new QLabel("label");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath colorCode = createString("colorCode");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> endPostDate = createDateTime("endPostDate", java.time.LocalDateTime.class);

    public final StringPath imgUrl = createString("imgUrl");

    public final NumberPath<Long> labelId = createNumber("labelId", Long.class);

    public final StringPath labelTitle = createString("labelTitle");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final DateTimePath<java.time.LocalDateTime> startPostDate = createDateTime("startPostDate", java.time.LocalDateTime.class);

    public final StringPath topExpression = createString("topExpression");

    public QLabel(String variable) {
        super(Label.class, forVariable(variable));
    }

    public QLabel(Path<? extends Label> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLabel(PathMetadata metadata) {
        super(Label.class, metadata);
    }

}

