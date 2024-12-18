package com.onnury.media.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QMedia is a Querydsl query type for Media
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMedia extends EntityPathBase<Media> {

    private static final long serialVersionUID = 2080510104L;

    public static final QMedia media = new QMedia("media");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath imgTitle = createString("imgTitle");

    public final StringPath imgUploadUrl = createString("imgUploadUrl");

    public final StringPath imgUrl = createString("imgUrl");

    public final StringPath imgUuidTitle = createString("imgUuidTitle");

    public final NumberPath<Long> mappingContentId = createNumber("mappingContentId", Long.class);

    public final NumberPath<Long> mediaId = createNumber("mediaId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath representCheck = createString("representCheck");

    public final StringPath type = createString("type");

    public QMedia(String variable) {
        super(Media.class, forVariable(variable));
    }

    public QMedia(Path<? extends Media> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMedia(PathMetadata metadata) {
        super(Media.class, metadata);
    }

}

