package com.onnury.query.brand;

import com.onnury.brand.domain.Brand;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.brand.response.*;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.member.domain.Member;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.onnury.brand.domain.QBrand.brand;
import static com.onnury.media.domain.QMedia.media;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;


@Slf4j
@RequiredArgsConstructor
@Component
public class BrandQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;


    // 브랜드 수정
    @Transactional
    public BrandUpdateResponseDto updateBrand(Long brandId, BrandUpdateRequestDto brandInfo, MultipartFile updateBrandImage) throws IOException {

        // 수정할 브랜드 호출
        Brand updateBrand = jpaQueryFactory
                .selectFrom(brand)
                .where(brand.brandId.eq(brandId))
                .fetchOne();

        // 새로 업로드 한 배너 이미지를 담는 해시 맵
        HashMap<String, MultipartFile> brandImgUploadInfo = new HashMap<>();
        List<BrandMediaResponseDto> newUpdateBrandInfoList = new ArrayList<>();

        // 앱 이미지 수정
        if (updateBrandImage != null) {

            assert updateBrand != null;

            Media prevBrandImage = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(updateBrand.getBrandId())
                            .and(media.type.eq("brand")))
                    .fetchOne();


            // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
            if (prevBrandImage != null) {
                File deleteImage = new File(prevBrandImage.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImage.delete()) {
                    // 기존 이미지에 대한 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(prevBrandImage.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");
                }
            }

            brandImgUploadInfo.put("brand", updateBrandImage);

            // 수정할 이미지 파일을 기준으로 업로드
            List<HashMap<String, String>> newUpdateBrandImgInfo = mediaUploadInterface.uploadBrandImage(brandImgUploadInfo);

            for (HashMap<String, String> eachImage : newUpdateBrandImgInfo) {
                // 이미지 데이터 저장
                Media media = Media.builder()
                        .imgUploadUrl(eachImage.get("imgUploadUrl"))
                        .imgUrl(eachImage.get("imgUrl"))
                        .imgTitle(eachImage.get("imgTitle"))
                        .imgUuidTitle(eachImage.get("imgUuidTitle"))
                        .representCheck("N")
                        .type(eachImage.get("purpose"))
                        .mappingContentId(updateBrand.getBrandId())
                        .build();

                mediaRepository.save(media);

                // 새로 업로드한 이미지 정보를 반환 객체 리스트에 저장
                newUpdateBrandInfoList.add(
                        BrandMediaResponseDto.builder()
                                .mediaId(media.getMediaId())
                                .imgUploadUrl(media.getImgUploadUrl())
                                .imgUrl(media.getImgUrl())
                                .imgTitle(media.getImgTitle())
                                .imgUuidTitle(media.getImgUuidTitle())
                                .type(media.getType())
                                .mappingContentId(media.getMappingContentId())
                                .build()
                );
            }
        }

        // 브랜드 명 수정
        jpaQueryFactory
                .update(brand)
                .set(brand.brandTitle, brandInfo.getBrandTitle())
                .where(brand.brandId.eq(updateBrand.getBrandId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        // 수정한 브랜드 명 호출
        Brand brandResult = jpaQueryFactory
                .selectFrom(brand)
                .where(brand.brandId.eq(updateBrand.getBrandId()))
                .fetchOne();

        return BrandUpdateResponseDto.builder()
                .brandTitle(brandResult.getBrandTitle())
                .status(brandResult.getStatus())
                .brandImages(newUpdateBrandInfoList)
                .build();
    }


    // 브랜드 삭제
    public boolean deleteBrand(Long brandId) {

        jpaQueryFactory
                .update(brand)
                .set(brand.status, "N")
                .where(brand.brandId.eq(brandId))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return false;
    }


    // 관리자 브랜드 페이지 리스트업
    public BrandListUpResponseDto listUpBrand(int page) {
        Long total = 0L;
        List<Brand> result = new ArrayList<>();
        List<BrandDataResponseDto> brandList = new ArrayList<>();

        // 총 브랜드 수량
        total = jpaQueryFactory
                .select(brand.count())
                .from(brand)
                .where(brand.status.eq("Y"))
                .fetchOne();

        List<Brand> englishBrandList = new ArrayList<>();
        List<Brand> koreanBrandList = new ArrayList<>();

        jpaQueryFactory
                .selectFrom(brand)
                .where(brand.status.eq("Y"))
                .orderBy(brand.brandTitle.asc())
                .fetch()
                .forEach(eachBrand -> {
                    if (Pattern.matches("^[a-zA-Z]*$", eachBrand.getBrandTitle())) {
                        englishBrandList.add(eachBrand);
                    } else {
                        koreanBrandList.add(eachBrand);
                    }
                });

        // 국문 브랜드와 영문 브랜드 총 합친 리스트 생성
        koreanBrandList.addAll(englishBrandList);

        // 총 브랜드 리스트의 수가 10 이상이면 페이징 처리
        if (koreanBrandList.size() >= 10) {
            if (page * 10 > koreanBrandList.size()) {
                result = koreanBrandList.subList(paging(page), koreanBrandList.size());
            } else {
                result = koreanBrandList.subList(paging(page), page * 10);
            }
        } else { // 총 브랜드 리스트의 수가 10 미만이면 리스트 전부 노출
            result = koreanBrandList.subList(paging(page), koreanBrandList.size());
        }

        // 총 브랜드 리스트를 기준으로 반환 객체에 넣어 리스트에 저장
        for (Brand eachBrandList : result) {

            String imgUrl = jpaQueryFactory
                    .select(media.imgUrl)
                    .from(media)
                    .where(media.type.eq("brand")
                            .and(media.mappingContentId.eq(eachBrandList.getBrandId())))
                    .fetchOne();

            brandList.add(
                    BrandDataResponseDto.builder()
                            .brandId(eachBrandList.getBrandId())
                            .brandTitle(eachBrandList.getBrandTitle())
                            .status(eachBrandList.getStatus())
                            .imgUrl(imgUrl)
                            .build()
            );
        }

        return BrandListUpResponseDto.builder()
                .brandDataResponseDto(brandList)
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


    // 메인 페이지 브랜드 리스트
    public List<MainPageBrandResponseDto> mainPageBrandList(String authMemberType) {

        List<MainPageBrandResponseDto> expressBrandList = new ArrayList<>();
        List<MainPageBrandResponseDto> expressEnglishBrandList = new ArrayList<>();

        // (1) 로그인한 유저의 유형에 해당
        // (2) 플랫폼에 따른 브랜드 이미지
        jpaQueryFactory
                .select(brand.brandId, brand.brandTitle, brand.status)
                .from(brand) // 브랜드와 이미지 데이터
                .where(brand.brandId.in( // 현재 로그인한 고객의 유형과 일치하는 판매 제품들의 브랜드 id를 가진 데이터
                                jpaQueryFactory
                                        .select(categoryInBrand.brandId)
                                        .from(categoryInBrand)
                                        .where(categoryInBrand.categoryInBrandId.in( // 호출한 제품들의 브랜드, 카테고리 매핑 정보를 통한 브랜드 id 호출
                                                jpaQueryFactory
                                                        .select(product.categoryInBrandId)
                                                        .from(product)
                                                        .where(product.status.eq("Y")
                                                                .and(checkProductType(authMemberType))) // 로그인한 고객의 유형과 동일한 타입의 제품들
                                                        .groupBy(product.categoryInBrandId)
                                                        .fetch()))
                                        .groupBy(categoryInBrand.brandId)
                                        .fetch())
                        .and(brand.status.eq("Y"))
                )
                .orderBy(brand.brandTitle.asc())
                .groupBy(brand.brandId) // 브랜드 id 기준 중복 제거
                .fetch()
                .forEach(eachBrand -> {
                    // 브랜드와 매핑된 연관 이미지 데이터 호출
                    Tuple mediaInfo = jpaQueryFactory
                            .select(media.mediaId, media.imgUrl)
                            .from(media)
                            .where(media.mappingContentId.eq(eachBrand.get(brand.brandId))
                                    .and(media.type.contains("brand")))
                            .fetchOne();

                    // 브랜드 이미지 데이터가 존재하지 않을 경우 해당 이미지 데이터를 제외한 나머지 브랜드 정보만을 반환
                    if (mediaInfo == null) {
                        MainPageBrandResponseDto brandInfo = MainPageBrandResponseDto.builder()
                                .brandId(eachBrand.get(brand.brandId))
                                .brandTitle(eachBrand.get(brand.brandTitle))
                                .status(eachBrand.get(brand.status))
                                .mediaId(0L)
                                .mediaUrl(null)
                                .build();

                        if (Pattern.matches("^[a-zA-Z]*$", eachBrand.get(brand.brandTitle))) {
                            expressEnglishBrandList.add(brandInfo);
                        } else {
                            expressBrandList.add(brandInfo);
                        }
                    } else { // 브랜드 이미지 데이터가 존재할 경우 해당 이미지 데이터를 포함한 브랜드 정보 반환
                        MainPageBrandResponseDto brandInfo = MainPageBrandResponseDto.builder()
                                .brandId(eachBrand.get(brand.brandId))
                                .brandTitle(eachBrand.get(brand.brandTitle))
                                .status(eachBrand.get(brand.status))
                                .mediaId(mediaInfo.get(media.mediaId))
                                .mediaUrl(mediaInfo.get(media.imgUrl))
                                .build();

                        if (Pattern.matches("^[a-zA-Z]*$", eachBrand.get(brand.brandTitle))) {
                            expressEnglishBrandList.add(brandInfo);
                        } else {
                            expressBrandList.add(brandInfo);
                        }
                    }
                });

        expressBrandList.addAll(expressEnglishBrandList);

        log.info("총 브랜드 수 : {}", expressBrandList.size());
        log.info("총 영문 브랜드 수 : {}", expressEnglishBrandList.size());

        return expressBrandList;
    }


    // 브랜드 이미지가 존재하는지 확인하고 호출하는 동적 조건
    private BooleanExpression checkExistBrandImage() {

        // 브랜드 유형의 이미지 데이터가 존재할 경우 진입
        if (jpaQueryFactory
                .select(media.count())
                .from(media)
                .where(media.type.contains("brand"))
                .fetchOne() > 0L) {

            // 브랜드 유형의 이미지 데이터들에 매핑된 컨텐츠 id 추출
            return brand.brandId.in(
                    jpaQueryFactory
                            .select(media.mappingContentId)
                            .from(media)
                            .where(media.type.contains("brand"))
                            .groupBy(media.mappingContentId)
                            .fetch());
        }

        // 브랜드 유형의 이미지 데이터가 존재하지 않을 경우 총 컨텐츠 id 반환
        return null;
    }


    // 로그인한 고객 유형에 따른 노출 제품 호출 동적 조건
    private BooleanExpression checkProductType(String type) {
        if (type.equals("C")) {
            return product.sellClassification.eq("C");
        }

        return product.productId.goe(1L);
    }
}