package com.onnury.query.link;

import com.onnury.link.domain.Link;
import com.onnury.link.request.LinkUpdateRequestDto;
import com.onnury.link.response.LinkListResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
//import javax.transaction.Transactional;
import java.util.List;

import static com.onnury.link.domain.QLink.link1;

@Slf4j
@RequiredArgsConstructor
@Component
public class LinkQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;


    // 링크 수정
    @Transactional(transactionManager = "MasterTransactionManager")
    public Link updateLink(LinkUpdateRequestDto linkInfo){

        jpaQueryFactory
                .update(link1)
                .set(link1.type, linkInfo.getType())
                .set(link1.linkCompany, linkInfo.getLinkCompany())
                .set(link1.link, linkInfo.getLink())
                .where(link1.linkId.eq(linkInfo.getLinkId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return jpaQueryFactory
                .selectFrom(link1)
                .where(link1.linkId.eq(linkInfo.getLinkId()))
                .fetchOne();
    }


    // 링크 삭제
    public boolean deleteLink(Long linkId) {

        // 링크 삭제
        jpaQueryFactory
                .delete(link1)
                .where(link1.linkId.eq(linkId))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return false;
    }


    // 관리자 링크 리스트업
    public LinkListResponseDto listUpLink(int page) {
        Long total = jpaQueryFactory
                .select(link1.count())
                .from(link1)
                .fetchOne();

        List<Link> result = jpaQueryFactory
                .selectFrom(link1)
                .limit(10)
                .offset(paging(page))
                .fetch();

        return LinkListResponseDto.builder()
                .list(result)
                .total(total)
                .build();
    }


    // page 계산
    private int paging(int page) {
        if (page > 0) {
            // 검색 요청 키워드에서 텍스트 서칭이 가능하도록 키워드에 % 기호 적용
            return (page - 1) * 10;
        }
        return 0;
    }
}