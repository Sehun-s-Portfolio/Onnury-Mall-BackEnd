package com.onnury.mapper;

import com.onnury.category.response.RelatedCategoryDataResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    // 중분류 제품 리스트 페이지에 같이 노출될 연관 하위 카테고리 리스트 정보
    List<RelatedCategoryDataResponseDto> getMiddleCategoryRelatedDownCategoryList(
            @Param("relatedCategoryInBrandIdList") List<Long> relatedCategoryInBrandIdList) throws Exception;
}