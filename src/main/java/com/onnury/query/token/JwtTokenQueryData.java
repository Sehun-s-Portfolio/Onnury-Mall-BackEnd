package com.onnury.query.token;

import com.onnury.jwt.JwtToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
//import javax.transaction.Transactional;

import static com.onnury.jwt.QJwtToken.jwtToken;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    // 기존에 이미 발급된 토큰이 존재할 경우 삭제 처리
//    @Transactional(transactionManager = "MasterTransactionManager")
    public void deletePrevToken(String mappingAccount, String type){
        JwtToken token = jpaQueryFactory
                .selectFrom(jwtToken)
                .where(jwtToken.mappingAccount.eq(mappingAccount)
                        .and(jwtToken.type.eq(type)))
                .orderBy(jwtToken.createdAt.desc())
                .limit(1)
                .fetchOne();

        if(token != null){
            jpaQueryFactory
                    .delete(jwtToken)
                    .where(jwtToken.tokenId.eq(token.getTokenId()))
                    .execute();

            entityManager.flush();
//            entityManager.clear();
        }
    }
}
