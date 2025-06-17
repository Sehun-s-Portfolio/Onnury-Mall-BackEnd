package com.onnury.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface CategoryInBrandMapper {

    // 중,소분류 카테고리에 연관되는 CategoryInBrand 매핑 정보들 호출
    List<Long> getMiddleOrDownCategoryrRelatedCategorInBrandIdList(
            @Param("categoryGroup") int categoryGroup, @Param("categoryId") Long categoryId,
            @Param("relatedDownCategoryIdList") List<Long> relatedDownCategoryIdList, @Param("brandIdList") List<Long> brandIdList,
            @Param("searchKeyword") String searchKeyword
    ) throws Exception;
}
