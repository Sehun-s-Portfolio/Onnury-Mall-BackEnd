package com.onnury.inquiry.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QInquiry is a Querydsl query type for Inquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInquiry extends EntityPathBase<Inquiry> {

    private static final long serialVersionUID = -863751112L;

    public static final QInquiry inquiry = new QInquiry("inquiry");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath answer = createString("answer");

    public final DateTimePath<java.time.LocalDateTime> answerAt = createDateTime("answerAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath inquiryContent = createString("inquiryContent");

    public final NumberPath<Long> inquiryId = createNumber("inquiryId", Long.class);

    public final StringPath inquiryTitle = createString("inquiryTitle");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath type = createString("type");

    public QInquiry(String variable) {
        super(Inquiry.class, forVariable(variable));
    }

    public QInquiry(Path<? extends Inquiry> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInquiry(PathMetadata metadata) {
        super(Inquiry.class, metadata);
    }

}

