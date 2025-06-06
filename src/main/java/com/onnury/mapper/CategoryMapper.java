package com.onnury.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {

    // 중분류, 소분류 기준 제품 페이지 제품 리스트 조회에 필요한 요청 카테고리 그룹값 조회
    Integer getCategoryGroupValue(Long categoryId) throws Exception;
}