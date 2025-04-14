package com.onnury.query.inquiry;

import com.onnury.inquiry.domain.Inquiry;
import com.onnury.inquiry.repository.InquiryRepository;
import com.onnury.inquiry.request.InquiryAnswerRequestDto;
import com.onnury.inquiry.request.InquiryRequestDto;
import com.onnury.inquiry.response.InquiryDataResponseDto;
import com.onnury.inquiry.response.InquiryListUpResponseDto;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.response.MediaResponseDto;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.member.domain.Member;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
//import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.inquiry.domain.QInquiry.inquiry;
import static com.onnury.member.domain.QMember.member;
import static com.onnury.media.domain.QMedia.media;

@Slf4j
@RequiredArgsConstructor
@Component
public class InquiryQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final InquiryRepository inquiryRepository;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;

    @Transactional(transactionManager = "MasterTransactionManager")
    public Inquiry updateInquiry(InquiryAnswerRequestDto inquiryAnswerRequestDto) throws IOException {

        jpaQueryFactory
                .update(inquiry)
                .set(inquiry.answer, inquiryAnswerRequestDto.getAnswer())
                .set(inquiry.answerAt, LocalDateTime.now())
                .where(inquiry.inquiryId.eq(inquiryAnswerRequestDto.getInquiryId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(inquiry.inquiryId.eq(inquiryAnswerRequestDto.getInquiryId()))
                .fetchOne();
    }


    // 관리자 문의 내역 페이지 리스트업
    public InquiryListUpResponseDto listUpInquiry(int page, String searchType, String searchType2, String searchKeyword) {
        Long total = 0L;

        // 문의 내역 조회 후 데이터 정제
        List<InquiryDataResponseDto> inquiryList = jpaQueryFactory
                .selectFrom(inquiry)
                .where(
                        inquiry.inquiryId.gt(0L)
                                .and(inquirysearch(searchType))
                                .and(inquirysearch2(searchType2, searchKeyword))
                )
                .orderBy(inquiry.createdAt.desc())
                .limit(10)
                .offset(paging(page))
                .fetch()
                .stream()
                .map(eachInquiry -> {

                    // 문의 작성 회원의 일부 정보 추출
                    Tuple memberInfo = jpaQueryFactory
                            .select(member.type, member.loginId, member.manager, member.userName)
                            .from(member)
                            .where(member.memberId.eq(eachInquiry.getMemberId()))
                            .fetchOne();

                    // 문의와 연관된 이미지 파일들 호출
                    List<Media> relateMedias = jpaQueryFactory
                            .selectFrom(media)
                            .where(media.mappingContentId.eq(eachInquiry.getInquiryId())
                                    .and(media.type.eq("inquiry")))
                            .fetch();

                    // 호출된 이미지 파일들을 반환 객체로 변환하여 저장할 리스트 생성
                    List<MediaResponseDto> relateImages = new ArrayList<>();

                    // 연관 이미지들이 존재할 경우 진입
                    if(!relateMedias.isEmpty()){
                        // 연관 이미지들 반환 객체로 매핑하여 리스트 저장
                        relateImages = relateMedias.stream()
                                .map(eachImage ->
                                        MediaResponseDto.builder()
                                                .mediaId(eachImage.getMediaId())
                                                .imgUploadUrl(eachImage.getImgUploadUrl())
                                                .imgUrl(eachImage.getImgUrl())
                                                .imgTitle(eachImage.getImgTitle())
                                                .imgUuidTitle(eachImage.getImgUuidTitle())
                                                .representCheck(eachImage.getRepresentCheck())
                                                .build()
                                )
                                .collect(Collectors.toList());
                    }

                    assert memberInfo != null;

                    Tuple inquiryDateTime = jpaQueryFactory
                            .select(inquiry.createdAt.stringValue(), inquiry.answerAt.stringValue())
                            .from(inquiry)
                            .where(inquiry.inquiryId.eq(eachInquiry.getInquiryId()))
                            .fetchOne();

                    // 문의 작성 회원의 유형이 기업일 경우, 담당자 명 저장
                    if (memberInfo.get(member.type).equals("B")) {
                        return InquiryDataResponseDto.builder()
                                .inquiryId(eachInquiry.getInquiryId())
                                .inquiryTitle(eachInquiry.getInquiryTitle())
                                .inquiryContent(eachInquiry.getInquiryContent())
                                .type(eachInquiry.getType())
                                .answer(eachInquiry.getAnswer())
                                .answerAt(inquiryDateTime.get(inquiry.answerAt.stringValue()) != null ?  inquiryDateTime.get(inquiry.answerAt.stringValue()) : null)
                                .createdAt(inquiryDateTime.get(inquiry.createdAt.stringValue()))
                                .customerLoginId(memberInfo.get(member.loginId))
                                .customerName(memberInfo.get(member.manager))
                                .relateImages(relateImages)
                                .build();
                    } else {// 문의 작성 회원의 유형이 일반일 경우, 고객 명 저장
                        return InquiryDataResponseDto.builder()
                                .inquiryId(eachInquiry.getInquiryId())
                                .inquiryTitle(eachInquiry.getInquiryTitle())
                                .inquiryContent(eachInquiry.getInquiryContent())
                                .type(eachInquiry.getType())
                                .answer(eachInquiry.getAnswer())
                                .answerAt(inquiryDateTime.get(inquiry.answerAt.stringValue()) != null ?  inquiryDateTime.get(inquiry.answerAt.stringValue()) : null)
                                .createdAt(inquiryDateTime.get(inquiry.createdAt.stringValue()))
                                .customerLoginId(memberInfo.get(member.loginId))
                                .customerName(memberInfo.get(member.userName))
                                .relateImages(relateImages)
                                .build();
                    }

                })
                .collect(Collectors.toList());

        // 문의 내역이 존재할 경우 진입
        if (!inquiryList.isEmpty()) {
            // 문의 총 갯수 추출
            total = jpaQueryFactory
                    .select(inquiry.count())
                    .from(inquiry)
                    .where(
                            inquiry.inquiryId.gt(0L)
                                    .and(inquirysearch(searchType))
                                    .and(inquirysearch2(searchType2, searchKeyword))
                    )
                    .fetchOne();
        }

        return InquiryListUpResponseDto.builder()
                .inquiryDataResponseDto(inquiryList)
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


    // 고객 문의 작성
    public InquiryDataResponseDto writeInquiry(Member inquiryMember, InquiryRequestDto inquiryRequestDto, List<MultipartFile> inquiryFile) throws IOException {

        // 문의 작성
        Inquiry writeInquiry = Inquiry.builder()
                .type(inquiryRequestDto.getType())
                .inquiryTitle(inquiryRequestDto.getInquiryTitle())
                .inquiryContent(inquiryRequestDto.getInquiryContent())
                .memberId(inquiryMember.getMemberId())
                .build();

        // 문의 저장
        inquiryRepository.save(writeInquiry);

        List<MediaResponseDto> relateImages = new ArrayList<>();

        // 만약 문의할 때 파일도 존재할 경우 업로드 처리
        if(inquiryFile != null && !inquiryFile.isEmpty()){
            // 문의 관련 파일들 업로드
            List<HashMap<String, String>> resultInquiryFiles = mediaUploadInterface.uploadInquiryFiles(inquiryFile);

            // 문의 파일들 한 번에 저장하기 위한 리스트 생성
            List<Media> inquiryMedias = new ArrayList<>();

            // 업로드된 파일들 기준으로 Media 도메인 정보 입력 및 리스트 저장
            resultInquiryFiles.forEach(eachInquiryFile -> {
                inquiryMedias.add(
                        Media.builder()
                                .imgUploadUrl(eachInquiryFile.get("imgUploadUrl"))
                                .imgUrl(eachInquiryFile.get("imgUrl"))
                                .imgTitle(eachInquiryFile.get("imgTitle"))
                                .imgUuidTitle(eachInquiryFile.get("imgUuidTitle"))
                                .representCheck("N")
                                .type("inquiry")
                                .mappingContentId(writeInquiry.getInquiryId())
                                .build()
                );
            });

            // 연관된 문의 파일들 일괄 저장 후 도메인 리스트화
            List<Media> relateMedias = mediaRepository.saveAll(inquiryMedias);

            // 연관된 문의 파일 정보 추출 및 반환 객체로 매핑
            relateImages = relateMedias.stream()
                    .map(eachImage ->
                            MediaResponseDto.builder()
                                    .mediaId(eachImage.getMediaId())
                                    .imgUploadUrl(eachImage.getImgUploadUrl())
                                    .imgUrl(eachImage.getImgUrl())
                                    .imgTitle(eachImage.getImgTitle())
                                    .imgUuidTitle(eachImage.getImgUuidTitle())
                                    .representCheck(eachImage.getRepresentCheck())
                                    .build()
                    )
                    .collect(Collectors.toList());
        }

        return InquiryDataResponseDto.builder()
                .inquiryId(writeInquiry.getInquiryId())
                .type(writeInquiry.getType())
                .inquiryTitle(writeInquiry.getInquiryTitle())
                .inquiryContent(writeInquiry.getInquiryContent())
                .answer(writeInquiry.getAnswer())
                .relateImages(relateImages)
                .build();
    }


    private BooleanExpression inquirysearch(String searchType) {

        if (!searchType.isEmpty()) {
            if(searchType.equals("전체")){
                return null;
            }else{
                // 검색 요청 키워드에서 텍스트 서칭이 가능하도록 키워드에 % 기호 적용
                return inquiry.type.eq(searchType);
            }
        }

        return null;
    }

    // inquirysearch2
    private BooleanExpression inquirysearch2(String searchType2, String searchKeyword) {

        if (!searchType2.isEmpty()) {
            //'회원ID', '이름', '제목'
            if (searchType2.equals("회원ID")) {
                if (!searchKeyword.isEmpty()) {
                    List<Long> searchInquiryIds = jpaQueryFactory
                            .select(member.memberId)
                            .from(member)
                            .where(member.loginId.like("%" + searchKeyword.replace(" ", "%") + "%"))
                            .fetch();
                    return inquiry.memberId.in(searchInquiryIds);
                }
            } else if (searchType2.equals("이름")) {
                if (!searchKeyword.isEmpty()) {
                    List<Long> searchInquiryIds = jpaQueryFactory
                            .select(member.memberId)
                            .from(member)
                            .where(member.userName.like("%" + searchKeyword.replace(" ", "%") + "%")
                                    .or(member.manager.like("%" + searchKeyword.replace(" ", "%") + "%")))
                            .fetch();
                    return inquiry.memberId.in(searchInquiryIds);
                }
            } else if (searchType2.equals("제목")) {
                if (!searchKeyword.isEmpty()) {
                    return inquiry.inquiryTitle.like("%" + searchKeyword.replace(" ", "%") + "%");
                }
            } else if(searchType2.equals("전체")){
                if (!searchKeyword.isEmpty()) {
                    List<Long> searchInquiryIds = jpaQueryFactory
                            .select(member.memberId)
                            .from(member)
                            .where(member.loginId.like("%" + searchKeyword.replace(" ", "%") + "%")
                                    .or(member.userName.like("%" + searchKeyword.replace(" ", "%") + "%")
                                            .or(member.manager.like("%" + searchKeyword.replace(" ", "%") + "%"))))
                            .fetch();
                    return inquiry.memberId.in(searchInquiryIds)
                            .or(inquiry.inquiryTitle.like("%" + searchKeyword.replace(" ", "%") + "%"));
                } else {
                    return null;
                }
            }

        }
        return null;
    }
}