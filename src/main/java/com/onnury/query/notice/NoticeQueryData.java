package com.onnury.query.notice;

import com.onnury.notice.domain.Notice;
import com.onnury.notice.repository.NoticeRepository;
import com.onnury.notice.request.NoticeRequestDto;
import com.onnury.notice.request.NoticeUpdateRequestDto;
import com.onnury.notice.response.NoticeDetailResponseDto;
import com.onnury.notice.response.NoticeResponseDto;
import com.onnury.notice.response.TotalNoticeResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
//import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.notice.domain.QNotice.notice;

@Slf4j
@RequiredArgsConstructor
@Component
public class NoticeQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final NoticeRepository noticeRepository;
    private final EntityManager entityManager;

    // 관리자 공지사항 작성
    public NoticeResponseDto writeNotice(NoticeRequestDto noticeRequestDto) {

        // 공지사항 작성 후 저장
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .noticeTitle(noticeRequestDto.getNoticeTitle())
                        .noticeContent(noticeRequestDto.getNoticeContent())
                        .build()
        );

        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .build();
    }


    // 고객 측 공지사항 리스트 및 관리자 공지사항 리스트 조회
    public TotalNoticeResponseDto getNoticeList(int page) {

        // 공지사항 리스트 추출
        List<Notice> notices = jpaQueryFactory
                .selectFrom(notice)
                .orderBy(notice.createdAt.desc())
                .offset((page * 10L) - 10L)
                .limit(10)
                .fetch();

        Long totalNoticeCount = 0L;
        List<NoticeResponseDto> noticeList = new ArrayList<>();

        // 만약 공지사항이 하나라도 존재할 경우 진입
        if (!notices.isEmpty()) {

            // 총 공지사항 수량
            totalNoticeCount = jpaQueryFactory
                    .select(notice.count())
                    .from(notice)
                    .fetchOne();

            // 추출한 공지사항 리스트를 기준으로 반환 객체 리스트에 맞게끔 매핑하여 저장
            noticeList = notices.stream()
                    .map(eachNotice ->
                            NoticeResponseDto.builder()
                                    .noticeId(eachNotice.getNoticeId())
                                    .noticeTitle(eachNotice.getNoticeTitle())
                                    .noticeContent(eachNotice.getNoticeContent())
                                    .createdAt(eachNotice.getCreatedAt())
                                    .build()
                    )
                    .collect(Collectors.toList());
        }

        return TotalNoticeResponseDto.builder()
                .totalNoticeCount(totalNoticeCount)
                .noticeList(noticeList)
                .build();
    }


    // 관리자 공지사항 수정
    @Transactional(transactionManager = "MasterTransactionManager")
    public NoticeResponseDto updateNotice(NoticeUpdateRequestDto noticeUpdateRequestDto) {

        // 공지사항 수정용 JPAUpdateClause 생성
        JPAUpdateClause noticeClause = jpaQueryFactory
                .update(notice)
                .where(notice.noticeId.eq(noticeUpdateRequestDto.getNoticeId()));

        // 수정 사항 확인 여부 Boolean 변수
        boolean checkExistUpdateContent = false;

        // 공지사항 제목 수정 세팅
        if (noticeUpdateRequestDto.getNoticeTitle() != null || !noticeUpdateRequestDto.getNoticeTitle().isEmpty()) {
            checkExistUpdateContent = true;
            noticeClause.set(notice.noticeTitle, noticeUpdateRequestDto.getNoticeTitle());
        }

        // 공지사항 내용 수정 세팅
        if (noticeUpdateRequestDto.getNoticeContent() != null || !noticeUpdateRequestDto.getNoticeContent().isEmpty()) {
            checkExistUpdateContent = true;
            noticeClause.set(notice.noticeContent, noticeUpdateRequestDto.getNoticeContent());
        }

        // 수정
        if (checkExistUpdateContent) {
            log.info("공지사항 수정 실패");
            noticeClause.execute();

            entityManager.flush();
            entityManager.clear();
        } else { // 수정 안함
            log.info("공지사항 수정 실패");
        }

        Notice updateNotice = jpaQueryFactory
                .selectFrom(notice)
                .where(notice.noticeId.eq(noticeUpdateRequestDto.getNoticeId()))
                .fetchOne();

        assert updateNotice != null;

        return NoticeResponseDto.builder()
                .noticeId(updateNotice.getNoticeId())
                .noticeTitle(updateNotice.getNoticeTitle())
                .noticeContent(updateNotice.getNoticeContent())
                .build();
    }


    // 관리자 공지사항 삭제
    @Transactional(transactionManager = "MasterTransactionManager")
    public String deleteNotice(Long noticeId) {

        // 공지사항 삭제 처리
        jpaQueryFactory
                .delete(notice)
                .where(notice.noticeId.eq(noticeId))
                .execute();

        return "공지사항을 삭제하셨습니다.";
    }


    // 공지사항 상세 정보 호출
    public NoticeDetailResponseDto getNoticeDetail(Long noticeId){

        // 상세 정보를 노출할 공지사항
        Notice callNotice = jpaQueryFactory
                .selectFrom(notice)
                .where(notice.noticeId.eq(noticeId))
                .fetchOne();

        return NoticeDetailResponseDto.builder()
                .noticeId(callNotice.getNoticeId())
                .noticeTitle(callNotice.getNoticeTitle())
                .noticeContent(callNotice.getNoticeContent())
                .createdAt(callNotice.getCreatedAt())
                .build();
    }
}
