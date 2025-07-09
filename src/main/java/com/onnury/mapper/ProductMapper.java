package com.onnury.mapper;

import com.onnury.brand.response.BrandDataResponseDto;
import com.onnury.category.response.RelatedCategoryDataResponseDto;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.label.response.LabelResponseDto;
import com.onnury.media.response.MediaResponseDto;
import com.onnury.product.domain.Product;
import com.onnury.product.response.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ProductMapper {
    // 일반 유저 신 제품들 조회
    List<Product> getNewProductsByCustomer() throws Exception;

    // 기업 및 관리자 신 제품들 조회
    List<Product> getNewProductsByBusiness() throws Exception;

    // 신 상품 제품 정보
    List<NewReleaseProductInfo> getProductInfo(String loginMemberType) throws Exception;

    // 신 상품 옵션 리스트
    List<NewReleaseProductOptionDto> getNewReleaseProductOptionList(Long productId) throws Exception;

    // 신 상품의 각 옵션에 해당되는 상세 옵션 정보 리스트
    List<NewReleaseProductDetailOptionDto> getNewReleaseProductDetailOptionList(Long productOptionId) throws Exception;

    // 선택한 대분류 카테고리 + 검색 조건이 적용된 제품 정보 리스트 조회
    List<ReadyProductPageMainProductResponseDto> getSelectUpCategoryAndConditionRelateProductList(
            @Param("upCategoryId") Long upCategoryId, @Param("brandIdList") List<Long> brandIdList,
            @Param("searchBrandKeyword") String searchBrandKeyword, @Param("middleCategoryIdList") List<Long> middleCategoryIdList,
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice,
            @Param("sort") int sort, @Param("page") int page) throws Exception;

    // 대분류 제품들 총 갯수
    int getAllUpCategoryProducts(
            @Param("upCategoryId") Long upCategoryId, @Param("brandIdList") List<Long> brandIdList,
            @Param("searchBrandKeyword") String searchBrandKeyword, @Param("middleCategoryIdList") List<Long> middleCategoryIdList,
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice) throws Exception;

    // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
    int getMaxPriceInUpCategoryProducts(
            @Param("upCategoryId") Long upCategoryId, @Param("brandIdList") List<Long> brandIdList,
            @Param("searchBrandKeyword") String searchBrandKeyword, @Param("middleCategoryIdList") List<Long> middleCategoryIdList,
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice) throws Exception;

    // 선택한 대분류 제품들에 해당되는 브랜드, 카테고리, 제품 id 리스트 정보
    List<ProductRelatedBrandAndCategoryInfoDto> getUpCategoryProductsRelatedBrandsAndDownCategories(
            @Param("upCategoryId") Long upCategoryId, @Param("brandIdList") List<Long> brandIdList,
            @Param("searchBrandKeyword") String searchBrandKeyword, @Param("middleCategoryIdList") List<Long> middleCategoryIdList,
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice
    ) throws Exception;

    // 각 대분류 제품이 가지고 있는 라벨 정보
    List<LabelDataResponseDto> getEachUpCategoryProductLabelInfo(Long productId) throws Exception;

    // 각 대분류 제품이 가지고 있는 옵션 정보
    List<ProductOptionCreateResponseDto> getEachUpCategoryProductOptionInfo(Long productId) throws Exception;

    // 각 대분류 제품이 가지고 있는 옵션의 상세 옵션 정보
    List<ProductDetailOptionCreateResponseDto> getEachUpCategoryProductDetailOptionInfo(Long productOptionId) throws Exception;

    // 각 대분류 제품이 가지고 있는 제품 이미지 정보
    List<MediaResponseDto> getEachUpCategoryProductMediaInfo(Long productId) throws Exception;

    // 선택한 대분류 카테고리 + 검색 조건이 적용된 제품들 총 수량 조회
    int getSelectUpCategoryProductsCount(
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice, @Param("sort") int sort) throws Exception;

    // 선택한 대분류 카테고리 제품들이 해당되는 브랜드 리스트
    List<BrandDataResponseDto> getSelectUpCategoryProductsRelatedBrand(
            @Param("relatedBrandIdList") List<Long> relatedBrandIdList,
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice,
            @Param("sort") int sort) throws Exception;

    // 선택한 대분류 카테고리 제품들이 해당되는 중분류 카테고리 정보 리스트
    List<RelatedCategoryDataResponseDto> getSelectUpCategoryProductsRelatedMiddleCategory(
            @Param("relatedMiddleCategoryIdList") List<Long> relatedMiddleCategoryIdList,
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice,
            @Param("sort") int sort) throws Exception;

    // 선택한 대분류 카테고리 제품들의 연관된 라벨 리스트 정보
    List<LabelResponseDto> getSelectUpCategoryProductsRelatedLabel(
            @Param("loginMemberType") String loginMemberType, @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice, @Param("endRangePrice") int endRangePrice,
            @Param("sort") int sort) throws Exception;

    // 중/소분류 기준 제품 페이지 메인 제품 총 갯수
    int middleAndDownCategoryPageMainProductsCount(
            @Param("loginMemberType") String loginMemberType,
            @Param("categoryId") Long categoryId,
            @Param("relatedDownCategoryIdList") List<Long> relatedDownCategoryIdList,
            @Param("brandIdList") List<Long> brandIdList,
            @Param("searchKeyword") String searchKeyword,
            @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice,
            @Param("endRangePrice") int endRangePrice,
            @Param("sort") int sort) throws Exception;

    // 중/소분류 기준 제품 페이지 메인 제품 리스트 호출
    List<ReadyProductPageMainProductResponseDtoV2> middleAndDownCategoryPageMainProducts(
            @Param("loginMemberType") String loginMemberType,
            @Param("categoryId") Long categoryId,
            @Param("relatedDownCategoryIdList") List<Long> relatedDownCategoryIdList,
            @Param("brandIdList") List<Long> brandIdList,
            @Param("searchKeyword") String searchKeyword,
            @Param("labelIdList") List<Long> labelIdList,
            @Param("startRangePrice") int startRangePrice,
            @Param("endRangePrice") int endRangePrice,
            @Param("sort") int sort,
            @Param("page") int page) throws Exception;
}