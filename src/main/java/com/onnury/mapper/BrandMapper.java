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
public interface BrandMapper {

    // 필터링 조건으로 노출시킬 중,소분류 제품들의 브랜드 정보 리스트
    List<BrandDataResponseDto> getMiddleAndDownProductsBrandList(List<Long> categoryInBrandIdList) throws Exception;
}