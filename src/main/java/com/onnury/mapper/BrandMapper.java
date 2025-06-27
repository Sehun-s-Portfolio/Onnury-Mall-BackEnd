package com.onnury.mapper;

import com.onnury.brand.response.BrandDataResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper {
    // 중/소분류 제품 리스트 페이지에 같이 노출될 연관 브랜드 리스트 정보
    List<BrandDataResponseDto> getMiddleAndDownCategoryProductsRelatedBrandList(
            @Param("relatedCategoryInBrandIdList") List<Long> relatedCategoryInBrandIdList) throws Exception;
}