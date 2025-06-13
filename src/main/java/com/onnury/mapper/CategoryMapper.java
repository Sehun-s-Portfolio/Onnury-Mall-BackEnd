package com.onnury.mapper;

import com.onnury.brand.response.BrandDataResponseDto;
import com.onnury.category.response.RelatedCategoryDataResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    // 중분류, 소분류 기준 제품 페이지 제품 리스트 조회에 필요한 요청 카테고리 그룹값 조회
    Integer getCategoryGroupValue(Long categoryId) throws Exception;

    // 필터링 조건으로 노출시킬 중,소분류 제품들의 카테고리 정보 리스트
    List<RelatedCategoryDataResponseDto> getMiddleAndDownProductsRelatedCategoryList(List<Long> categoryInBrandIdList) throws Exception;
}