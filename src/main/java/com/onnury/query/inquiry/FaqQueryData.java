package com.onnury.query.inquiry;

import com.onnury.inquiry.domain.Faq;
import com.onnury.inquiry.request.FaqUpdateRequestDto;
import com.onnury.inquiry.response.FaqDataResponseDto;
import com.onnury.inquiry.response.FaqListUpResponseDto;
import com.onnury.inquiry.response.FaqResponseDto;
import com.onnury.inquiry.response.TotalFaqListResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.inquiry.domain.QFaq.faq;


@Slf4j
@RequiredArgsConstructor
@Component
public class FaqQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public Faq updateFaq(Long Faqid, FaqUpdateRequestDto faqInfo) throws IOException {

        jpaQueryFactory
                .update(faq)
                .set(faq.type, faqInfo.getType())
                .set(faq.question, faqInfo.getQuestion())
                .set(faq.answer, faqInfo.getAnswer())
                .set(faq.expressCheck, faqInfo.getExpressCheck())
                .where(faq.faqId.eq(Faqid))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return jpaQueryFactory
                .selectFrom(faq)
                .where(faq.faqId.eq(Faqid))
                .fetchOne();
    }

    // FAQ 삭제
    public boolean deleteFaq(Long Faqid) {

        // FAQ 삭제
        jpaQueryFactory
                .delete(faq)
                .where(faq.faqId.eq(Faqid))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return false;
    }

    //관리자 브랜드 페이지 리스트업
    public FaqListUpResponseDto listUpFaq(int page, String type) {
        Long total = 0L;
        new ArrayList<>();
        List<FaqDataResponseDto> faqList = new ArrayList<>();

        if (type == null || type.isEmpty()) {
            total = jpaQueryFactory
                    .select(faq.count())
                    .from(faq)
                    .fetchOne();

            List<Faq> result = jpaQueryFactory
                    .selectFrom(faq)
                    .orderBy(faq.createdAt.desc())
                    .limit(10)
                    .offset(paging(page))
                    .fetch();

            for (Faq eachFaqList : result) {
                faqList.add(
                        FaqDataResponseDto.builder()
                                .faqId(eachFaqList.getFaqId())
                                .type(eachFaqList.getType())
                                .question(eachFaqList.getQuestion())
                                .answer(eachFaqList.getAnswer())
                                .expressCheck(eachFaqList.getExpressCheck())
                                .build()
                );
            }
        } else {
            total = jpaQueryFactory
                    .select(faq.count())
                    .from(faq)
                    .where(faq.type.eq(type))
                    .fetchOne();

            List<Faq> result = jpaQueryFactory
                    .selectFrom(faq)
                    .where(faq.type.eq(type))
                    .orderBy(faq.createdAt.desc())
                    .limit(10)
                    .offset(paging(page))
                    .fetch();

            for (Faq eachFaqList : result) {
                faqList.add(
                        FaqDataResponseDto.builder()
                                .faqId(eachFaqList.getFaqId())
                                .type(eachFaqList.getType())
                                .question(eachFaqList.getQuestion())
                                .answer(eachFaqList.getAnswer())
                                .expressCheck(eachFaqList.getExpressCheck())
                                .build()
                );
            }
        }

        return FaqListUpResponseDto.builder()
                .faqDataResponseDto(faqList)
                .total(total)
                .build();
    }


    //page 계산
    private int paging(int page) {
        if (page > 0) {
            // 검색 요청 키워드에서 텍스트 서칭이 가능하도록 키워드에 % 기호 적용
            return (page - 1) * 10;
        }
        return 0;
    }


    // 고객 FAQ 리스트 조회
    public TotalFaqListResponseDto getFaqList(int page, String type) {

        // FAQ 리스트 호출
        List<Faq> faqList = jpaQueryFactory
                .selectFrom(faq)
                .where(faq.expressCheck.eq("Y")
                        .and(eqFaqType(type)))
                .orderBy(faq.createdAt.desc())
                .offset((page * 10L) - 10L)
                .limit(10)
                .fetch();

        Long totalFaqCount = 0L;
        List<FaqResponseDto> faqDataList = new ArrayList<>();

        // FAQ 리스트가 존재 시 진입
        if (!faqList.isEmpty()) {

            // 선택한 유형에 맞춘 총 FAQ 수
            totalFaqCount = jpaQueryFactory
                    .select(faq.count())
                    .from(faq)
                    .where(faq.expressCheck.eq("Y")
                            .and(eqFaqType(type)))
                    .fetchOne();

            // 선택한 유형에 따른 FAQ 데이터 처리
            faqDataList = faqList.stream()
                    .map(eachFaq -> {
                        FaqResponseDto faqData;

                        // 만약 답변이 달리지 않은 FAQ인 경우 답변 대기 및 답변 일자 null 처리
                        if (eachFaq.getAnswer() == null || eachFaq.getAnswer().isEmpty()) {
                            faqData = FaqResponseDto.builder()
                                    .faqId(eachFaq.getFaqId())
                                    .faqCreatedAt(eachFaq.getCreatedAt())
                                    .type(eachFaq.getType())
                                    .question(eachFaq.getQuestion())
                                    .answer(eachFaq.getAnswer())
                                    .answerCheck("답변 대기")
                                    .expressCheck(eachFaq.getExpressCheck())
                                    .faqCreatedAt(eachFaq.getCreatedAt())
                                    .build();

                        } else { // 답변이 달린 FAQ인 경우 답변 완료 및 답변 일자 처리
                            faqData = FaqResponseDto.builder()
                                    .faqId(eachFaq.getFaqId())
                                    .faqCreatedAt(eachFaq.getCreatedAt())
                                    .type(eachFaq.getType())
                                    .question(eachFaq.getQuestion())
                                    .answer(eachFaq.getAnswer())
                                    .answerCheck("답변 완료")
                                    .expressCheck(eachFaq.getExpressCheck())
                                    .faqCreatedAt(eachFaq.getCreatedAt())
                                    .build();
                        }

                        return faqData;
                    })
                    .collect(Collectors.toList());
        }

        return TotalFaqListResponseDto.builder()
                .total(totalFaqCount)
                .faqDataResponseDto(faqDataList)
                .build();
    }


    private BooleanExpression eqFaqType(String type) {
        if (type != null || !type.isEmpty()) {
            return faq.type.eq(type);
        }

        return faq.type.eq("이용안내");
    }
}