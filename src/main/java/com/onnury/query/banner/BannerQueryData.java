package com.onnury.query.banner;

import com.onnury.banner.domain.Banner;
import com.onnury.banner.request.BannerUpdateRequestDto;
import com.onnury.banner.response.*;
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
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.onnury.banner.domain.QBanner.banner;
import static com.onnury.media.domain.QMedia.media;

@Slf4j
@RequiredArgsConstructor
@Component
public class BannerQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;


    // 배너 수정
    @Transactional
    public BannerUpdateResponseDto updateBanner(
            Long bannerId, MultipartFile updateAppBannerImg, MultipartFile updateWebBannerImg, MultipartFile updateSlideBannerImg, BannerUpdateRequestDto updateBannerInfo) throws IOException {

        // 수정하고자 하는 배너 호출
        Banner updateBanner = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.bannerId.eq(bannerId))
                .fetchOne();

        // 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause clause = jpaQueryFactory
                .update(banner)
                .where(banner.bannerId.eq(bannerId));

        // 수정할 내용이 있는지 확인하기 위한 boolean 변수
        boolean existUpdateContent = false;
        assert updateBanner != null;

        // 배너 타이틀 수정 세팅
        if (!updateBannerInfo.getTitle().isEmpty() && !updateBanner.getTitle().equals(updateBannerInfo.getTitle())) {
            existUpdateContent = true;
            clause.set(banner.title, updateBannerInfo.getTitle());
        }

        // 배너 클릭 시 이동 사이트 url 수정 세팅
        if (!updateBannerInfo.getLinkUrl().isEmpty() && !updateBanner.getLinkUrl().equals(updateBannerInfo.getLinkUrl())) {
            existUpdateContent = true;
            clause.set(banner.linkUrl, updateBannerInfo.getLinkUrl());
        }

        // 노출 순서 수정 세팅
        if (updateBannerInfo.getExpressionOrder() != 0 && updateBanner.getExpressionOrder() != updateBannerInfo.getExpressionOrder()) {
            existUpdateContent = true;
            clause.set(banner.expressionOrder, updateBannerInfo.getExpressionOrder());
        }

        // 배너 게시 시작 일 수정 세팅
        if (!updateBannerInfo.getStartPostDate().isEmpty() && !updateBanner.getStartPostDate().equals(updateBannerInfo.getStartPostDate())) {
            existUpdateContent = true;
            clause.set(banner.startPostDate, updateBannerInfo.getStartPostDate() + " 00:00:00");
        }

        // 배너 게시 마지막 일 수정 세팅
        if (!updateBannerInfo.getEndPostDate().isEmpty() && !updateBanner.getEndPostDate().equals(updateBannerInfo.getEndPostDate())) {
            existUpdateContent = true;
            clause.set(banner.endPostDate, updateBannerInfo.getEndPostDate() + " 23:59:59");
        }

        // 새로 업로드 한 배너 이미지를 담는 해시 맵
        HashMap<String, MultipartFile> bannerCreateInfo = new HashMap<>();

        // 앱 이미지 수정
        if (updateAppBannerImg != null) {

            Media appImage = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(updateBanner.getBannerId())
                            .and(media.type.eq("appbanner")))
                    .fetchOne();


            // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
            if (appImage != null) {
                File deleteImage = new File(appImage.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImage.delete()) {

                    // 기존 이미지에 대한 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(appImage.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");
                }
            }

            bannerCreateInfo.put("app", updateAppBannerImg);

        }

        // 웹 이미지 수정
        if (updateWebBannerImg != null) {

            Media webImage = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(updateBanner.getBannerId())
                            .and(media.type.eq("webbanner")))
                    .fetchOne();

            // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
            if (webImage != null) {
                File deleteImage = new File(webImage.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImage.delete()) {

                    // 기존 이미지에 대한 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(webImage.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");
                }
            }

            bannerCreateInfo.put("web", updateWebBannerImg);
        }

        // 슬라이드 이미지 수정
        if (updateSlideBannerImg != null) {

            Media slideImage = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(updateBanner.getBannerId())
                            .and(media.type.eq("slidebanner")))
                    .fetchOne();

            // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
            if (slideImage != null) {
                File deleteImage = new File(slideImage.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImage.delete()) {

                    // 기존 이미지에 대한 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(slideImage.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");
                }
            }

            bannerCreateInfo.put("slide", updateSlideBannerImg);

        }

        // 수정할 이미지 파일을 기준으로 업로드
        List<HashMap<String, String>> newUpdateBannerImgInfo = mediaUploadInterface.uploadBannerImage(bannerCreateInfo);

        for (HashMap<String, String> eachImage : newUpdateBannerImgInfo) {
            // 이미지 데이터 저장
            Media media = Media.builder()
                    .imgUploadUrl(eachImage.get("imgUploadUrl"))
                    .imgUrl(eachImage.get("imgUrl"))
                    .imgTitle(eachImage.get("imgTitle"))
                    .imgUuidTitle(eachImage.get("imgUuidTitle"))
                    .representCheck("N")
                    .type(eachImage.get("purpose") + "banner")
                    .mappingContentId(updateBanner.getBannerId())
                    .build();

            mediaRepository.save(media);
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

        // 새롭게 수정된 배너 정보 호출
        Banner newBanner = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.bannerId.eq(bannerId))
                .fetchOne();

        // 배너와 연관된 이미지 데이터 호출
        assert newBanner != null;
        List<Media> newImages = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(newBanner.getBannerId())
                        .and(media.type.contains("banner")))
                .fetch();

        // 호출한 이미지 데이터들을 기준으로 반환 객체로 변환
        List<BannerMediaResponseDto> responseImages = newImages.stream()
                .map(eachImage ->
                        BannerMediaResponseDto.builder()
                                .mediaId(eachImage.getMediaId())
                                .imgUploadUrl(eachImage.getImgUploadUrl())
                                .imgUrl(eachImage.getImgUrl())
                                .imgTitle(eachImage.getImgTitle())
                                .imgUuidTitle(eachImage.getImgUuidTitle())
                                .type(eachImage.getType())
                                .mappingContentId(eachImage.getMappingContentId())
                                .build()
                )
                .collect(Collectors.toList());

        return BannerUpdateResponseDto.builder()
                .title(newBanner.getTitle())
                .linkUrl(newBanner.getLinkUrl())
                .expressionOrder(newBanner.getExpressionOrder())
                .startPostDate(newBanner.getStartPostDate())
                .endPostDate(newBanner.getEndPostDate())
                .bannerImages(responseImages)
                .build();
    }


    // 배너 삭제
    public boolean deleteBanner(Long deleteBannerId) {

        // 같이 삭제될 Media 데이터 호출
        List<Media> deleteMedias = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(deleteBannerId)
                        .and(media.type.eq("appbanner")
                                .or(media.type.eq("webbanner"))
                                .or(media.type.eq("slidebanner"))
                        )
                )
                .fetch();

        // 배너 삭제
        jpaQueryFactory
                .delete(banner)
                .where(banner.bannerId.eq(deleteBannerId))
                .execute();

        if (deleteMedias != null && !deleteMedias.isEmpty()) {
            for (Media eachImage : deleteMedias) {
                // 같이 삭제할 이미지 파일 호출
                File deleteImgfile = new File(eachImage.getImgUploadUrl());

                // 이미지 파일 삭제 처리
                if (deleteImgfile.delete()) {
                    log.info("업로드된 이미지 파일 삭제");
                }

                // 연관된 Media 데이터 삭제
                jpaQueryFactory
                        .delete(media)
                        .where(media.mediaId.eq(eachImage.getMediaId()))
                        .execute();
            }
        }

        entityManager.flush();
        entityManager.clear();

        return false;
    }


    // [앱]
    // iPhone
    // Android
    // iPad
    // BlackBerry
    // symbian
    // sony
    // mobile

    // [웹]
    // Windows

    // 메인 페이지 배너 리스트
    public TotalMainPageBannerResponseDto listUpBanner(HttpServletRequest request) {
        long totalBannerCount = 0L;

        // 배너 게시 일자, 마무리 일자를 DateTime으로 포맷시킬 formatter 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        // 게시 일자에 포함 되어 노출 되는 배너들 호출
        List<Banner> banners = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.expressionCheck.eq("Y")
                        .and(banner.bannerId.in(
                                jpaQueryFactory
                                        .select(media.mappingContentId)
                                        .from(media)
                                        .where(media.type.eq("webbanner").or(media.type.eq("appbanner"))
                                                .or(media.type.eq("slidebanner")))
                                        .groupBy(media.mappingContentId)
                                        .fetch()
                        ))
                )
                .orderBy(banner.expressionOrder.asc(), banner.createdAt.desc())
                .fetch()
                .stream()
                .filter(eachBanner ->
                        LocalDateTime.parse(eachBanner.getStartPostDate(), formatter).isBefore(LocalDateTime.now()) && LocalDateTime.parse(eachBanner.getEndPostDate(), formatter).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        List<MainPageBannerResponseDto> responseBannerInfos = new ArrayList<>();

        // 노출 배너들이 존재할 경우 진입
        if (!banners.isEmpty()) {
            // 노출되는 전체 배너들의 갯수 추출
            totalBannerCount = (long) banners.size();

            // 노출되어 페이징 처리된 배너들을 접속 중인 플랫폼에 따라 배너 이미지를 추출하고, 이외의 배너 정보들을 반환 리스트 객체에 매핑하여 저장
            responseBannerInfos = banners.stream()
                    .map(eachExpressBanner -> {

                        // 배너 이미지 호출
                        List<Media> relatedImages = new ArrayList<>(
                                jpaQueryFactory
                                        .selectFrom(media)
                                        .where(media.mappingContentId.eq(eachExpressBanner.getBannerId())
                                                .and(media.type.like("%banner%")))
                                        .fetch()
                        );

                        // 반환 리스트 객체 매핑 저장
                        List<BannerMediaResponseDto> relatedReponseImages = relatedImages.stream()
                                .map(eachRelatedImage ->
                                        BannerMediaResponseDto.builder()
                                                .mediaId(eachRelatedImage.getMediaId())
                                                .imgUploadUrl(eachRelatedImage.getImgUploadUrl())
                                                .imgUrl(eachRelatedImage.getImgUrl())
                                                .imgTitle(eachRelatedImage.getImgTitle())
                                                .imgUuidTitle(eachRelatedImage.getImgUuidTitle())
                                                .type(eachRelatedImage.getType())
                                                .mappingContentId(eachRelatedImage.getMappingContentId())
                                                .build()
                                )
                                .collect(Collectors.toList());

                        return MainPageBannerResponseDto.builder()
                                .bannerId(eachExpressBanner.getBannerId())
                                .title(eachExpressBanner.getTitle())
                                .linkUrl(eachExpressBanner.getLinkUrl())
                                .expressionOrder(eachExpressBanner.getExpressionOrder())
                                .bannerImages(relatedReponseImages)
                                .build();
                    })
                    .collect(Collectors.toList());
        }


        return TotalMainPageBannerResponseDto.builder()
                .totalBannerCount(totalBannerCount)
                .bannerList(responseBannerInfos)
                .build();
    }


    // 프로모션 배너 수정
    @Transactional
    public PromotionBannerUpdateResponseDto updatePromotionBanner(Long bannerId, MultipartFile updateBannerImg, BannerUpdateRequestDto updateBannerInfo) throws IOException {

        // 수정하고자 하는 배너 호출
        Banner updateBanner = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.bannerId.eq(bannerId))
                .fetchOne();

        // 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause clause = jpaQueryFactory
                .update(banner)
                .where(banner.bannerId.eq(bannerId));

        // 수정할 내용이 있는지 확인하기 위한 boolean 변수
        boolean existUpdateContent = false;
        assert updateBanner != null;

        // 배너 타이틀 수정 세팅
        if (!updateBannerInfo.getTitle().isEmpty() && !updateBanner.getTitle().equals(updateBannerInfo.getTitle())) {
            existUpdateContent = true;
            clause.set(banner.title, updateBannerInfo.getTitle());
        }

        // 배너 클릭 시 이동 사이트 url 수정 세팅
        if (!updateBannerInfo.getLinkUrl().isEmpty() && !updateBanner.getLinkUrl().equals(updateBannerInfo.getLinkUrl())) {
            existUpdateContent = true;
            clause.set(banner.linkUrl, updateBannerInfo.getLinkUrl());
        }

        // 노출 순서 수정 세팅
        if (updateBannerInfo.getExpressionOrder() != 0 && updateBanner.getExpressionOrder() != updateBannerInfo.getExpressionOrder()) {
            existUpdateContent = true;
            clause.set(banner.expressionOrder, updateBannerInfo.getExpressionOrder());
        }

        // 일자 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        // 프로모션 배너 게시 시작일, 종료일
        String startPostDate = updateBannerInfo.getStartPostDate() + " 00:00:00";
        String endPostDate = updateBannerInfo.getEndPostDate() + " 23:59:59";

        // 게시 시작일, 종료일 LocalDateTime 변환
        LocalDateTime convertStartPostDate = LocalDateTime.parse(startPostDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
        LocalDateTime convertEndPostDate = LocalDateTime.parse(endPostDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 만약 게시일이 현재 일자 범위 내에 있으면 노출 여부 Y
        if (LocalDateTime.now().isAfter(convertStartPostDate) && LocalDateTime.now().isBefore(convertEndPostDate) && updateBanner.getExpressionCheck().equals("N")) {
            existUpdateContent = true;
            clause.set(banner.expressionCheck, "Y");
        }

        // 만약 게시일이 현재 일자 범위 내에 없으면 노출 여부 N
        if (!LocalDateTime.now().isAfter(convertStartPostDate) || !LocalDateTime.now().isBefore(convertEndPostDate)) {
            existUpdateContent = true;
            clause.set(banner.expressionCheck, "N");
        }

        // 배너 게시 시작 일 수정 세팅
        if (!updateBannerInfo.getStartPostDate().isEmpty() && !updateBanner.getStartPostDate().equals(updateBannerInfo.getStartPostDate())) {
            existUpdateContent = true;
            clause.set(banner.startPostDate, startPostDate);
        }

        // 배너 게시 마지막 일 수정 세팅
        if (!updateBannerInfo.getEndPostDate().isEmpty() && !updateBanner.getEndPostDate().equals(updateBannerInfo.getEndPostDate())) {
            existUpdateContent = true;
            clause.set(banner.endPostDate, endPostDate);
        }

        // 새로 업로드 한 배너 이미지를 담는 해시 맵
        HashMap<String, MultipartFile> bannerCreateInfo = new HashMap<>();

        // 프로모션 배너 이미지 수정
        if (updateBannerImg != null) {

            // 기존 프로모션 배너 이미지 데이터 조회
            Media promotionImage = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(updateBanner.getBannerId())
                            .and(media.type.eq("promotionbanner")))
                    .fetchOne();

            // 기존 프로모션 배너 이미지가 존재할 경우,
            if (promotionImage != null) {
                // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
                File deleteImage = new File(promotionImage.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImage.delete()) {

                    // 기존 이미지에 대한 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(promotionImage.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");

                    // 실제로 업로드하기 위해 HashMap에 수정 이미지 파일 저장
                    bannerCreateInfo.put("promotion", updateBannerImg);
                }
            }

            // 수정할 이미지 파일을 기준으로 업로드
            List<HashMap<String, String>> newUpdateBannerImgInfo = mediaUploadInterface.uploadBannerImage(bannerCreateInfo);
            HashMap<String, String> newPromotionImgInfo = newUpdateBannerImgInfo.get(0);

            // 이미지 데이터 저장
            Media newPromotionMedia = Media.builder()
                    .imgUploadUrl(newPromotionImgInfo.get("imgUploadUrl"))
                    .imgUrl(newPromotionImgInfo.get("imgUrl"))
                    .imgTitle(newPromotionImgInfo.get("imgTitle"))
                    .imgUuidTitle(newPromotionImgInfo.get("imgUuidTitle"))
                    .representCheck("N")
                    .type(newPromotionImgInfo.get("purpose") + "banner")
                    .mappingContentId(updateBanner.getBannerId())
                    .build();

            mediaRepository.save(newPromotionMedia);
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

        // 수정된 프로모션 배너 호출
        Banner newBanner = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.bannerId.eq(bannerId))
                .fetchOne();

        // 수정된 프로모션 이미지 호출
        assert newBanner != null;
        Media newImage = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(newBanner.getBannerId())
                        .and(media.type.contains("promotion")))
                .fetchOne();

        // 반환 객체에 수정 배너 내용과 이미지 정보 저장
        assert newImage != null;
        BannerMediaResponseDto newPromotionImgResponseDto = BannerMediaResponseDto.builder()
                .mediaId(newImage.getMediaId())
                .imgUploadUrl(newImage.getImgUploadUrl())
                .imgUrl(newImage.getImgUrl())
                .imgTitle(newImage.getImgTitle())
                .imgUuidTitle(newImage.getImgUuidTitle())
                .type(newImage.getType())
                .mappingContentId(newImage.getMappingContentId())
                .build();

        return PromotionBannerUpdateResponseDto.builder()
                .title(newBanner.getTitle())
                .linkUrl(newBanner.getLinkUrl())
                .expressionOrder(newBanner.getExpressionOrder())
                .startPostDate(newBanner.getStartPostDate())
                .endPostDate(newBanner.getEndPostDate())
                .bannerImage(newPromotionImgResponseDto)
                .build();
    }


    // 메인 페이지 프로모션 배너 리스트
    public TotalMainPagePromotionBannerResponseDto listUpPromotionBanner() {

        // 프로모션 배너 리스트
        List<MainPagePromotionBannerResponseDto> promotionBannerList = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.expressionCheck.eq("Y") // 노출 체크가 Y인 배너에 한하여
                        .and(banner.bannerId.in(
                                jpaQueryFactory
                                        .select(media.mappingContentId)
                                        .from(media)
                                        .where(media.type.contains("promotionbanner"))
                                        .fetch())
                        ) // 프로모션 배너 이미지를 가진 배너들
                )
                .orderBy(banner.expressionOrder.asc(), banner.createdAt.desc())
                .fetch()
                .stream()
                .map(eachPromotionBanner -> {
                    // 프로모션 배너 이미지 정보 호출
                    Media promotionMedia = jpaQueryFactory
                            .selectFrom(media)
                            .where(media.mappingContentId.eq(eachPromotionBanner.getBannerId())
                                    .and(media.type.eq("promotionbanner")))
                            .fetchOne();

                    assert promotionMedia != null;

                    // 프로모션 배너 이미지 정보 반환 객체에 저장
                    BannerMediaResponseDto promotionBannerImgInfo = BannerMediaResponseDto.builder()
                            .mediaId(promotionMedia.getMediaId())
                            .imgUploadUrl(promotionMedia.getImgUploadUrl())
                            .imgUrl(promotionMedia.getImgUrl())
                            .imgTitle(promotionMedia.getImgTitle())
                            .imgUuidTitle(promotionMedia.getImgUuidTitle())
                            .type(promotionMedia.getType())
                            .mappingContentId(promotionMedia.getMappingContentId())
                            .build();

                    // 최종 반환 프로모션 배너 리스트 객체에 저장
                    return MainPagePromotionBannerResponseDto.builder()
                            .bannerId(eachPromotionBanner.getBannerId())
                            .title(eachPromotionBanner.getTitle())
                            .linkUrl(eachPromotionBanner.getLinkUrl())
                            .expressionOrder(eachPromotionBanner.getExpressionOrder())
                            .startPostDate(eachPromotionBanner.getStartPostDate())
                            .endPostDate(eachPromotionBanner.getEndPostDate())
                            .bannerImage(promotionBannerImgInfo)
                            .build();
                })
                .collect(Collectors.toList());

        return TotalMainPagePromotionBannerResponseDto.builder()
                .totalBannerCount((long) promotionBannerList.size())
                .bannerList(promotionBannerList)
                .build();
    }


    // 프로모션 배너 삭제
    public boolean deletePromotionBanner(Long deleteBannerId) {

        // 프로모션 배너 삭제
        jpaQueryFactory
                .delete(banner)
                .where(banner.bannerId.eq(deleteBannerId))
                .execute();

        // 같이 삭제될 Media 데이터 호출
        List<Media> deleteMedias = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(deleteBannerId)
                        .and(media.type.eq("promotionbanner"))
                )
                .fetch();

        // Media 데이터 존재 검증
        assert deleteMedias != null;

        if (!deleteMedias.isEmpty()) {
            for (Media eachImage : deleteMedias) {
                // 같이 삭제할 이미지 파일 호출
                File deleteImgfile = new File(eachImage.getImgUploadUrl());

                // 이미지 파일 삭제 처리
                if (deleteImgfile.delete()) {
                    log.info("업로드된 이미지 파일 삭제");
                }

                // 연관된 Media 데이터 삭제
                jpaQueryFactory
                        .delete(media)
                        .where(media.mediaId.eq(eachImage.getMediaId()))
                        .execute();
            }
        }

        entityManager.flush();
        entityManager.clear();

        return false;
    }


    // 관리자 배너 리스트 호출
    public TotalBannerResponseDto adminGetTotalMainBannerList(int page) {

        List<BannerDataResponseDto> bannerList = jpaQueryFactory
                .selectFrom(banner)
                .where(banner.bannerId.in(
                                jpaQueryFactory
                                        .select(media.mappingContentId)
                                        .from(media)
                                        .where(media.type.like("%banner%").and(media.type.notLike("%promotion%")))
                                        .groupBy(media.mappingContentId)
                                        .fetch()
                        )
                )
                .fetch()
                .stream()
                .map(eachBanner -> {

                    AtomicReference<String> appBannerImgUrl = new AtomicReference<>("");
                    AtomicReference<String> webBannerImgUrl = new AtomicReference<>("");
                    AtomicReference<String> slideBannerImgUrl = new AtomicReference<>("");

                    jpaQueryFactory
                            .selectFrom(media)
                            .where(media.mappingContentId.eq(eachBanner.getBannerId())
                                    .and(media.type.like("%banner%").and(media.type.notLike("%promotion%")))
                            )
                            .fetch()
                            .forEach(eachMedia -> {
                                if (eachMedia.getType().equals("appbanner")) {
                                    appBannerImgUrl.set(eachMedia.getImgUrl());
                                } else if (eachMedia.getType().equals("webbanner")) {
                                    webBannerImgUrl.set(eachMedia.getImgUrl());
                                } else {
                                    slideBannerImgUrl.set(eachMedia.getImgUrl());
                                }
                            });

                    return BannerDataResponseDto.builder()
                            .bannerId(eachBanner.getBannerId())
                            .title(eachBanner.getTitle())
                            .linkUrl(eachBanner.getLinkUrl())
                            .expressionOrder(eachBanner.getExpressionOrder())
                            .expressionCheck(eachBanner.getExpressionCheck())
                            .appBannerImgUrl(String.valueOf(appBannerImgUrl))
                            .webBannerImgUrl(String.valueOf(webBannerImgUrl))
                            .slideBannerImgUrl(String.valueOf(slideBannerImgUrl))
                            .startPostDate(eachBanner.getStartPostDate())
                            .endPostDate(eachBanner.getEndPostDate())
                            .imgType("banner")
                            .build();
                })
                .collect(Collectors.toList());

        Long totalBannerCount = (long) bannerList.size();

        // 추출한 브랜드관 제품들 페이징
        if (bannerList.size() >= 10) {
            if ((page * 10) <= bannerList.size()) {
                bannerList = bannerList.subList((page * 10) - 10, page * 10);
            } else {
                bannerList = bannerList.subList((page * 10) - 10, bannerList.size());
            }
        } else {
            bannerList = bannerList.subList((page * 10) - 10, bannerList.size());
        }

        return TotalBannerResponseDto.builder()
                .totalBannerCount(totalBannerCount)
                .responseBannerList(bannerList)
                .build();
    }


}
