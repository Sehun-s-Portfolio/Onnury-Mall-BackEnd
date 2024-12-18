package com.onnury.query.label;

import com.onnury.label.domain.Label;
import com.onnury.label.request.LabelUpdateRequestDto;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.label.response.LabelListUpResponseDto;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.media.domain.QMedia.media;
import static com.onnury.label.domain.QLabel.label;

@Slf4j
@RequiredArgsConstructor
@Component
public class LabelQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;


    // 라벨 수정
    @Transactional
    public Label updateLabel(Long labelId, MultipartFile updateLabelImg, LabelUpdateRequestDto updateLabelInfo) throws IOException {

        // 수정하고자 하는 배너 호출
        Label updateLabel = jpaQueryFactory
                .selectFrom(label)
                .where(label.labelId.eq(labelId))
                .fetchOne();

        // 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause clause = jpaQueryFactory
                .update(label)
                .where(label.labelId.eq(labelId));

        // 수정할 내용이 있는지 확인하기 위한 boolean 변수
        boolean existUpdateContent = false;
        assert updateLabel != null;

        // 라벨명 수정 세팅
        if (!updateLabelInfo.getLabelTitle().isEmpty() && !updateLabel.getLabelTitle().equals(updateLabelInfo.getLabelTitle())) {
            existUpdateContent = true;
            clause.set(label.labelTitle, updateLabelInfo.getLabelTitle());
        }

        /**
        //라벨컬러 코드 세팅
        if (!updateLabelInfo.getColorCode().isEmpty() && !updateLabel.getColorCode().equals(updateLabelInfo.getColorCode())) {
            existUpdateContent = true;
            clause.set(label.colorCode, updateLabelInfo.getColorCode());
        }
         **/

        //라벨 게시일 세팅
        if (!updateLabelInfo.getStartPostDate().toString().isEmpty() && !updateLabel.getStartPostDate().equals(updateLabelInfo.getStartPostDate())) {
            existUpdateContent = true;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
            LocalDateTime startPostDate = LocalDateTime.parse(updateLabelInfo.getStartPostDate() + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            clause.set(label.startPostDate, startPostDate);
        }

        //라벨 종료일 세팅
        if (!updateLabelInfo.getEndPostDate().toString().isEmpty() && !updateLabel.getEndPostDate().equals(updateLabelInfo.getEndPostDate())) {
            existUpdateContent = true;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
            LocalDateTime endPostDate = LocalDateTime.parse(updateLabelInfo.getEndPostDate() + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            clause.set(label.endPostDate, endPostDate);
        }

        // 라벨 상위 노출 세팅
        if (!updateLabelInfo.getTopExpression().isEmpty() && !updateLabel.getTopExpression().equals(updateLabelInfo.getTopExpression())) {
            existUpdateContent = true;
            clause.set(label.topExpression, updateLabelInfo.getTopExpression());
        }

        // 이미지 수정
        if (updateLabelImg != null) {

            // 수정할 배너 정보에 업로드 이미지 경로가 존재하는지 확인
            if (!updateLabel.getImgUrl().isEmpty()) {

                // 같이 삭제될 Media 데이터 호출
                Media deleteMedia = jpaQueryFactory
                        .selectFrom(media)
                        .where(media.mappingContentId.eq(updateLabel.getLabelId())
                                .and(media.type.eq("label")))
                        .fetchOne();

                // Media 데이터 존재 검증
                assert deleteMedia != null;

                // 같이 삭제할 이미지 파일 호출
                File deleteImgfile = new File(deleteMedia.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImgfile.delete()) {
                    existUpdateContent = true;

                    // 연관된 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(deleteMedia.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");

                    // 수정할 이미지 파일을 기준으로 업로드
                    HashMap<String, String> newUpdateBannerImgInfo = mediaUploadInterface.uploadLabelImage(updateLabelImg);

                    // 이미지 데이터 저장
                    Media media = Media.builder()
                            .imgUploadUrl(newUpdateBannerImgInfo.get("imgUploadUrl"))
                            .imgUrl(newUpdateBannerImgInfo.get("imgUrl"))
                            .imgTitle(newUpdateBannerImgInfo.get("imgTitle"))
                            .imgUuidTitle(newUpdateBannerImgInfo.get("imgUuidTitle"))
                            .representCheck("N")
                            .type("label")
                            .mappingContentId(updateLabel.getLabelId())
                            .build();

                    mediaRepository.save(media);

                    // 동적 수정 clause 조건에 이미지 수정 경로 추가
                    clause.set(label.imgUrl, newUpdateBannerImgInfo.get("imgUrl"));
                }
            }
        }

        // 수정할 컨텐츠가 존재할 경우 업데이트 실행
        if (existUpdateContent) {
            log.info("수정 성공");
            clause.execute();
        } else {
            log.info("수정 실패");
        }

        entityManager.flush();
        entityManager.clear();

        return jpaQueryFactory
                .selectFrom(label)
                .where(label.labelId.eq(labelId))
                .fetchOne();
    }


    // 배너 삭제
    public boolean deleteLabel(Long deleteLabelId) {

        // 배너 삭제
        jpaQueryFactory
                .delete(label)
                .where(label.labelId.eq(deleteLabelId))
                .execute();

        // 같이 삭제될 Media 데이터 호출
        Media deleteMedia = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(deleteLabelId)
                        .and(media.type.eq("label")))
                .fetchOne();

        // Media 데이터 존재 검증
        assert deleteMedia != null;

        // 같이 삭제할 이미지 파일 호출
        File deleteImgfile = new File(deleteMedia.getImgUploadUrl());

        // 이미지 파일 삭제 처리
        if (deleteImgfile.delete()) {
            log.info("업로드된 이미지 파일 삭제");
        }

        // 연관된 Media 데이터 삭제
        jpaQueryFactory
                .delete(media)
                .where(media.mediaId.eq(deleteMedia.getMediaId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return false;
    }


    //관리자 라벨 페이지 리스트업
    public LabelListUpResponseDto listUpLabel(int page) {
        Long total = 0L;
        List<Label> result = new ArrayList<>();
        List<LabelDataResponseDto> labelList = new ArrayList<>();

        total = jpaQueryFactory
                .select(label.count())
                .from(label)
                .fetchOne();

        result = jpaQueryFactory
                .selectFrom(label)
                .limit(10)
                .offset(paging(page))
                .fetch();

        for (Label eachSupplierList : result) {
            labelList.add(
                    LabelDataResponseDto.builder()
                            .labelId(eachSupplierList.getLabelId())
                            .labelTitle(eachSupplierList.getLabelTitle())
                            .colorCode(eachSupplierList.getColorCode())
                            .startPostDate(eachSupplierList.getStartPostDate())
                            .endPostDate(eachSupplierList.getEndPostDate())
                            .imgUrl(eachSupplierList.getImgUrl())
                            .topExpression(eachSupplierList.getTopExpression())
                            .build()
            );
        }

        return LabelListUpResponseDto.builder()
                .labelDataResponseDto(labelList)
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


    // 상위 노출 라벨 리스트 호출
    public List<LabelDataResponseDto> topExpressionLabelList() {
        return jpaQueryFactory
                .selectFrom(label)
                .where(label.topExpression.eq("Y")
                        .and(label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now()))))
                .orderBy(label.modifiedAt.desc())
                .fetch()
                .stream()
                .map(eachLabel ->
                    LabelDataResponseDto.builder()
                            .labelId(eachLabel.getLabelId())
                            .labelTitle(eachLabel.getLabelTitle())
                            .colorCode(eachLabel.getColorCode())
                            .startPostDate(eachLabel.getStartPostDate())
                            .endPostDate(eachLabel.getEndPostDate())
                            .imgUrl(eachLabel.getImgUrl())
                            .topExpression(eachLabel.getTopExpression())
                            .build()
                )
                .collect(Collectors.toList());
    }

}
