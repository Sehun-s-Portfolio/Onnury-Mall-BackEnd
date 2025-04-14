package com.onnury.query.category;

import com.onnury.brand.domain.Brand;
import com.onnury.category.domain.Category;
import com.onnury.category.domain.CategoryInBrand;
import com.onnury.category.request.CategoryUpdateRequestDto;
import com.onnury.category.response.*;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.member.domain.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
//import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.media.domain.QMedia.media;
import static com.onnury.category.domain.QCategory.category;
import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;
import static com.onnury.brand.domain.QBrand.brand;
import static com.onnury.product.domain.QProduct.product;

@Slf4j
@RequiredArgsConstructor
@Component
public class CategoryQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;


    // 카테고리 수정
    @Transactional(transactionManager = "MasterTransactionManager")
    public Category updateCategory(Long categoryId, MultipartFile categoryImg, CategoryUpdateRequestDto updateCategoryInfo) throws IOException {

        // 수정하고자 하는 카테고리 호출
        Category updateCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(categoryId))
                .fetchOne();

        // 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause clause = jpaQueryFactory
                .update(category)
                .where(category.categoryId.eq(categoryId));

        // 수정할 내용이 있는지 확인하기 위한 boolean 변수
        boolean existUpdateContent = false;
        assert updateCategory != null;

        // 카테고리 이름 수정 세팅
        if (!updateCategoryInfo.getCategoryName().isEmpty() && !updateCategory.getCategoryName().equals(updateCategoryInfo.getCategoryName())) {
            existUpdateContent = true;
            clause.set(category.categoryName, updateCategoryInfo.getCategoryName());
        }


        // 이미지 수정
        if (categoryImg != null) {

            // 수정할 카테고리 정보에 업로드 이미지 경로가 존재하는지 확인
            if (!updateCategory.getImgUrl().isEmpty()) {

                // 같이 삭제될 중분류 카테고리 이미지 데이터 호출
                Media deleteMedia = jpaQueryFactory
                        .selectFrom(media)
                        .where(media.mappingContentId.eq(updateCategory.getCategoryId())
                                .and(media.type.eq("category")))
                        .fetchOne();

                // Media 데이터 존재 검증
                assert deleteMedia != null;

                // 같이 삭제할 실제 이미지 파일 호출
                File deleteImgfile = new File(deleteMedia.getImgUploadUrl());

                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                if (deleteImgfile.delete()) {
                    existUpdateContent = true;

                    // 기존 이미지에 대한 Media 데이터 삭제
                    jpaQueryFactory
                            .delete(media)
                            .where(media.mediaId.eq(deleteMedia.getMediaId()))
                            .execute();

                    log.info("기존에 이미 이미지가 존재할 경우 삭제");

                    // 수정할 이미지 파일을 기준으로 업로드
                    HashMap<String, String> newUpdateBannerImgInfo = mediaUploadInterface.uploadCategoryImage(categoryImg);

                    // 이미지 데이터 저장
                    Media media = Media.builder()
                            .imgUploadUrl(newUpdateBannerImgInfo.get("imgUploadUrl"))
                            .imgUrl(newUpdateBannerImgInfo.get("imgUrl"))
                            .imgTitle(newUpdateBannerImgInfo.get("imgTitle"))
                            .imgUuidTitle(newUpdateBannerImgInfo.get("imgUuidTitle"))
                            .representCheck("N")
                            .type("category")
                            .mappingContentId(updateCategory.getCategoryId())
                            .build();

                    mediaRepository.save(media);

                    // 동적 수정 clause 조건에 이미지 수정 경로 추가
                    clause.set(category.imgUrl, newUpdateBannerImgInfo.get("imgUrl"));
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
                .selectFrom(category)
                .where(category.categoryId.eq(categoryId))
                .fetchOne();
    }


    // 카테고리 삭제
    public boolean deleteCategory(Long deleteCategoryId) {

        // 삭제할 카테고리 호출
        Category deleteCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(deleteCategoryId))
                .fetchOne();

        assert deleteCategory != null;

        // 만약 삭제하고자 하는 카테고리가 대분류 카테고리일 경우
        if (deleteCategory.getCategoryGroup() == 0) {

            // 한번에 삭제할 대분류, 중분류 이미지들을 담을 리스트 생성
            List<Long> deleteImageIds = new ArrayList<>();

            // 대분류 카테고리에 속한 중분류 카테고리를 조회한 뒤, 해당 중분류에 속한 소분류 카테고리들을 삭제하고 해당 중분류 카테고리도 삭제 처리
            jpaQueryFactory
                    .selectFrom(category)
                    .where(category.motherCode.eq(deleteCategory.getClassficationCode())
                            .and(category.categoryGroup.eq(1)))
                    .fetch()
                    .forEach(eachMiddleCategory -> {

                        // 중분류 카테고리에 속한 소분류 카테고리들과 해당 중분류 카테고리를 일괄 삭제
                        jpaQueryFactory
                                .delete(category)
                                .where(category.motherCode.eq(eachMiddleCategory.getClassficationCode())
                                        .or(category.categoryId.eq(eachMiddleCategory.getCategoryId())))
                                .execute();

                    });

            // 같이 삭제될 대분류 카테고리 이미지 데이터 호출
            Media deleteUpCategoryMedia = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(deleteCategory.getCategoryId())
                            .and(media.type.eq("category")))
                    .fetchOne();

            // Media 데이터 존재 검증
            assert deleteUpCategoryMedia != null;

            // 한 번에 삭제할 리스트에 대분류 카테고리 이미지 id 저장
            deleteImageIds.add(deleteUpCategoryMedia.getMediaId());

            // 같이 삭제할 이미지 파일 호출
            File deleteImgfile = new File(deleteUpCategoryMedia.getImgUploadUrl());

            // 이미지 파일 삭제 처리
            if (deleteImgfile.delete()) {
                log.info("업로드된 이미지 파일 삭제");
            }

            // 대분류 카테고리 이미지 + 연관된 중분류 카테고리 이미지 데이터 삭제
            jpaQueryFactory
                    .delete(media)
                    .where(media.mediaId.in(deleteImageIds))
                    .execute();

            // 대분류 카테고리 삭제
            jpaQueryFactory
                    .delete(category)
                    .where(category.categoryId.eq(deleteCategory.getCategoryId()))
                    .execute();

        } else if (deleteCategory.getCategoryGroup() == 1) { // 만약 삭제하고자 하는 카테고리가 중분류 카테고리일 경우

            // 같이 삭제될 중분류 카테고리 이미지 데이터 호출
            Media deleteMiddleCategoryMedia = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.mappingContentId.eq(deleteCategory.getCategoryId())
                            .and(media.type.eq("category")))
                    .fetchOne();

            // Media 데이터 존재 검증
            if (deleteMiddleCategoryMedia != null) {
                // 같이 삭제할 이미지 파일 호출
                File deleteImgfile = new File(deleteMiddleCategoryMedia.getImgUploadUrl());

                // 이미지 파일 삭제 처리
                if (deleteImgfile.delete()) {
                    log.info("업로드된 이미지 파일 삭제");
                }

                // 대분류 카테고리 이미지 + 연관된 중분류 카테고리 이미지 데이터 삭제
                jpaQueryFactory
                        .delete(media)
                        .where(media.mediaId.eq(deleteMiddleCategoryMedia.getMediaId()))
                        .execute();
            }


            // 중분류에 속한 소분류 및 해당 중분류 카테고리 일괄 삭제
            jpaQueryFactory
                    .delete(category)
                    .where((category.motherCode.eq(deleteCategory.getClassficationCode())
                            .and(category.categoryGroup.eq(2)))
                            .or(category.classficationCode.eq(deleteCategory.getClassficationCode())))
                    .execute();

        } else { // 만약 삭제하고자 하는 카테고리가 소분류일 경우

            // 소분류는 이미지 정보가 없으므로 카테고리 데이터만 삭제
            jpaQueryFactory
                    .delete(category)
                    .where(category.categoryId.eq(deleteCategory.getCategoryId()))
                    .execute();

        }

        entityManager.flush();
        entityManager.clear();

        return false;
    }


    // 관리자 대분류 리스트업
    public List<CategoryDataResponseDto> listUpCategoryOneDepth() {
        List<Category> result = new ArrayList<>();

        List<CategoryDataResponseDto> categoryList = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(0))
                .fetch()
                .stream()
                .map(eachOneCategory -> {

                    List<Long> categoryInBrandIds = jpaQueryFactory
                            .select(categoryInBrand.categoryInBrandId)
                            .from(categoryInBrand)
                            .where(categoryInBrand.category1Id.eq(eachOneCategory.getCategoryId()))
                            .fetch();

                    Long productCount = jpaQueryFactory
                            .select(product.count())
                            .from(product)
                            .where(product.categoryInBrandId.in(categoryInBrandIds))
                            .fetchOne();

                    assert productCount != null;

                    return CategoryDataResponseDto.builder()
                            .categoryId(eachOneCategory.getCategoryId())
                            .categoryGroup(eachOneCategory.getCategoryGroup())
                            .categoryName(eachOneCategory.getCategoryName())
                            .classificationCode(eachOneCategory.getClassficationCode())
                            .motherCode(eachOneCategory.getMotherCode())
                            .imgUrl(eachOneCategory.getImgUrl())
                            .productCount(productCount)
                            .build();

                })
                .collect(Collectors.toList());

        return categoryList;
    }

    //.and(category.motherCode.eq(MotherCode))
    // 관리자 중분류 리스트업
    public List<CategoryDataResponseDto> listUpCategoryTwoDepth(String MotherCode) {

        List<CategoryDataResponseDto> categoryList = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(1).and(category.motherCode.eq(MotherCode)))
                .fetch()
                .stream()
                .map(eachTwoCategory -> {

                    List<Long> categoryInBrandIds = jpaQueryFactory
                            .select(categoryInBrand.categoryInBrandId)
                            .from(categoryInBrand)
                            .where(categoryInBrand.category2Id.eq(eachTwoCategory.getCategoryId()))
                            .fetch();

                    Long productCount = jpaQueryFactory
                            .select(product.count())
                            .from(product)
                            .where(product.categoryInBrandId.in(categoryInBrandIds))
                            .fetchOne();

                    assert productCount != null;


                    return CategoryDataResponseDto.builder()
                            .categoryId(eachTwoCategory.getCategoryId())
                            .categoryGroup(eachTwoCategory.getCategoryGroup())
                            .categoryName(eachTwoCategory.getCategoryName())
                            .classificationCode(eachTwoCategory.getClassficationCode())
                            .motherCode(eachTwoCategory.getMotherCode())
                            .imgUrl(eachTwoCategory.getImgUrl())
                            .productCount(productCount)
                            .build();

                })
                .collect(Collectors.toList());

        return categoryList;
    }

    // 관리자 소분류 리스트업
    public List<CategoryDataResponseDto> listUpCategoryThreeDepth(String MotherCode) {
        List<Category> result = new ArrayList<>();
        List<CategoryDataResponseDto> categoryList = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(2).and(category.motherCode.eq(MotherCode)))
                .fetch()
                .stream()
                .map(eachThreeCategory -> {

                    List<Long> categoryInBrandIds = jpaQueryFactory
                            .select(categoryInBrand.categoryInBrandId)
                            .from(categoryInBrand)
                            .where(categoryInBrand.category3Id.eq(eachThreeCategory.getCategoryId()))
                            .fetch();

                    Long productCount = jpaQueryFactory
                            .select(product.count())
                            .from(product)
                            .where(product.categoryInBrandId.in(categoryInBrandIds))
                            .fetchOne();

                    assert productCount != null;

                    return CategoryDataResponseDto.builder()
                            .categoryId(eachThreeCategory.getCategoryId())
                            .categoryGroup(eachThreeCategory.getCategoryGroup())
                            .categoryName(eachThreeCategory.getCategoryName())
                            .classificationCode(eachThreeCategory.getClassficationCode())
                            .motherCode(eachThreeCategory.getMotherCode())
                            .imgUrl(eachThreeCategory.getImgUrl())
                            .productCount(productCount)
                            .build();

                })
                .collect(Collectors.toList());

        return categoryList;
    }


    // 카테고리와 매핑된 브랜드 정보 호출
    public CategoryInBrand getCategoryInBrand(
            Long brandId, Long upCategoryId, Long middleCategoryId, Long downCategoryId) {

        return jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.brandId.eq(brandId)
                        .and(categoryInBrand.category1Id.eq(upCategoryId))
                        .and(categoryInBrand.category2Id.eq(middleCategoryId))
                        .and(categoryInBrand.category3Id.eq(downCategoryId)))
                .limit(1)
                .fetchOne();

    }


    // 네비게이션 카테고리 및 브랜드
    public List<UpCategoryInfoResponseDto> getNavigationCategories(String authMemberType) {

        List<CategoryInBrand> relateCategoryInBrandList = jpaQueryFactory
                .select(product.categoryInBrandId)
                .from(product)
                .where(product.productId.goe(1L)
                        .and(product.status.eq("Y"))
                        .and(checkProductType(authMemberType))
                )
                .groupBy(product.categoryInBrandId)
                .fetch()
                .stream()
                .map(eachCategoryInBrandId ->
                        jpaQueryFactory
                                .selectFrom(categoryInBrand)
                                .where(categoryInBrand.categoryInBrandId.eq(eachCategoryInBrandId))
                                .fetchOne()
                )
                .collect(Collectors.toList());


        List<UpCategoryInfoResponseDto> allCategories = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(0))
                .orderBy(category.categoryName.asc())
                .fetch()
                .stream()
                .map(eachcategory -> {

                    List<RelatedBrandResponseDto> relatedBrandsResult = new ArrayList<>();

                    if (!relateCategoryInBrandList.isEmpty()) {
                        List<RelatedBrandResponseDto> relatedBrands = new ArrayList<>();

                        relateCategoryInBrandList.forEach(eachCategoryInBrand -> {
                            if (eachCategoryInBrand.getCategory1Id() == eachcategory.getCategoryId()) {
                                Brand brandInfo = jpaQueryFactory
                                        .selectFrom(brand)
                                        .where(brand.brandId.eq(eachCategoryInBrand.getBrandId()))
                                        .fetchOne();

                                assert brandInfo != null;

                                if (relatedBrands.isEmpty()) {
                                    relatedBrands.add(RelatedBrandResponseDto.builder()
                                            .brandId(brandInfo.getBrandId())
                                            .brandName(brandInfo.getBrandTitle())
                                            .build());
                                } else {
                                    if (!relatedBrands.stream()
                                            .map(RelatedBrandResponseDto::getBrandId)
                                            .distinct()
                                            .collect(Collectors.toList())
                                            .contains(brandInfo.getBrandId())) {
                                        relatedBrands.add(RelatedBrandResponseDto.builder()
                                                .brandId(brandInfo.getBrandId())
                                                .brandName(brandInfo.getBrandTitle())
                                                .build());
                                    }
                                    /**
                                     if(relatedBrands.stream().noneMatch(eachRelateBrand -> eachRelateBrand.getBrandId().equals(brandInfo.getBrandId()))){
                                     relatedBrands.add(RelatedBrandResponseDto.builder()
                                     .brandId(brandInfo.getBrandId())
                                     .brandName(brandInfo.getBrandTitle())
                                     .build());
                                     }
                                     **/
                                }
                            }
                        });

                        if (!relatedBrands.isEmpty()) {
                            relatedBrandsResult = relatedBrands;
                        }
                    }

                    List<MiddleCategoryInfoResponseDto> subcategory = jpaQueryFactory
                            .select(category.classficationCode, category.categoryId, category.categoryName)
                            .from(category)
                            .where(category.motherCode.eq(eachcategory.getClassficationCode()))
                            .orderBy(category.categoryName.asc())
                            .fetch()
                            .stream()
                            .map(eachcategory2 -> {
                                List<DownCategoryInfoResponseDto> subcategory2 = jpaQueryFactory
                                        .select(category.categoryId, category.categoryName)
                                        .from(category)
                                        .where(category.motherCode.eq(eachcategory2.get(category.classficationCode)))
                                        .orderBy(category.categoryName.asc())
                                        .fetch()
                                        .stream()
                                        .map(eachDownCategoryInfo ->
                                                DownCategoryInfoResponseDto.builder()
                                                        .downCategoryId(eachDownCategoryInfo.get(category.categoryId))
                                                        .downCategoryName(eachDownCategoryInfo.get(category.categoryName))
                                                        .build()
                                        )
                                        .collect(Collectors.toList());


                                return MiddleCategoryInfoResponseDto.builder()
                                        .middleCategoryId(eachcategory2.get(category.categoryId))
                                        .middleCategoryName(eachcategory2.get(category.categoryName))
                                        .relatedDownCategories(subcategory2)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return UpCategoryInfoResponseDto.builder()
                            .upCategoryId(eachcategory.getCategoryId())
                            .upCategoryName(eachcategory.getCategoryName())
                            .relatedMiddleCategories(subcategory)
                            .relatedBrands(relatedBrandsResult)
                            .build();
                })
                .collect(Collectors.toList());

        return allCategories;
    }


    // 로그인한 고객의 유형에 따른 제품 노출 유형 조건
    private BooleanExpression checkProductType(String memberType) {
        if (memberType.equals("C")) {
            return product.sellClassification.eq("C");
        }

        return null;
    }


    // 메인 페이지 대분류 카테고리 리스트 호출
    public List<CategoryDataResponseDto> mainPageQuickUpCategory() {

        List<Category> quickUpCategories = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(0))
                .orderBy(category.categoryName.asc())
                .fetch();

        return quickUpCategories.stream()
                .map(eachUpCategory ->
                        CategoryDataResponseDto.builder()
                                .categoryId(eachUpCategory.getCategoryId())
                                .categoryGroup(eachUpCategory.getCategoryGroup())
                                .motherCode(eachUpCategory.getMotherCode())
                                .classificationCode(eachUpCategory.getClassficationCode())
                                .categoryName(eachUpCategory.getCategoryName())
                                .imgUrl(eachUpCategory.getImgUrl())
                                .build()
                )
                .collect(Collectors.toList());
    }


    // 제품 페이지 대분류 기준 중분류 카테고리 리스트 조회
    public List<CategoryDataResponseDto> middleCategoryByUpCategory(Long upCategoryId) {

        // 대분류 카테고리 id 기준으로 카테고리 및 브랜드 매핑 정보들 중 중분류 id들을 추출하여 리스트 화
        List<Long> relatedMiddleCategories = jpaQueryFactory
                .select(categoryInBrand.category2Id)
                .from(categoryInBrand)
                .where(categoryInBrand.category1Id.eq(upCategoryId))
                .groupBy(categoryInBrand.category2Id)
                .fetch();

        // 중분류 id 리스트들을 기준으로 실제 중분류 카테고리 데이터 추출 및 리스트 화
        List<Category> middleCategories = relatedMiddleCategories.stream()
                .map(eachMiddleCategoryId ->
                        jpaQueryFactory
                                .selectFrom(category)
                                .where(category.categoryId.eq(eachMiddleCategoryId))
                                .fetchOne()
                )
                .collect(Collectors.toList());

        // 실제 카테고리 리스트 데이터들을 조회하여 반환 객체로 매핑하여 반환
        return middleCategories.stream()
                .map(eachCategory ->
                        CategoryDataResponseDto.builder()
                                .categoryId(eachCategory.getCategoryId())
                                .categoryGroup(eachCategory.getCategoryGroup())
                                .motherCode(eachCategory.getMotherCode())
                                .classificationCode(eachCategory.getClassficationCode())
                                .categoryName(eachCategory.getCategoryName())
                                .imgUrl(eachCategory.getImgUrl())
                                .build()
                )
                .collect(Collectors.toList());
    }

}
