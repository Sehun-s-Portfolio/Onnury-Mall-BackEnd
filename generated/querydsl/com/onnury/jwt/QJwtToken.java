package com.onnury.jwt;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QJwtToken is a Querydsl query type for JwtToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJwtToken extends EntityPathBase<JwtToken> {

    private static final long serialVersionUID = 721327757L;

    public static final QJwtToken jwtToken = new QJwtToken("jwtToken");

    public final com.onnury.share.QTimeStamped _super = new com.onnury.share.QTimeStamped(this);

    public final StringPath accessToken = createString("accessToken");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath grantType = createString("grantType");

    public final StringPath mappingAccount = createString("mappingAccount");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath refreshToken = createString("refreshToken");

    public final NumberPath<Long> tokenId = createNumber("tokenId", Long.class);

    public final StringPath type = createString("type");

    public QJwtToken(String variable) {
        super(JwtToken.class, forVariable(variable));
    }

    public QJwtToken(Path<? extends JwtToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJwtToken(PathMetadata metadata) {
        super(JwtToken.class, metadata);
    }

}

