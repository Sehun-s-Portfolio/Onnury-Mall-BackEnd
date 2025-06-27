package com.onnury.mapper;

import com.onnury.label.response.LabelResponseDto;
import com.onnury.label.response.NewReleaseProductLabelResponseDto;
import com.onnury.product.domain.Product;
import com.onnury.product.response.NewReleaseProductInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface LabelMapper {

    // 신 상품에 연관된 라벨 정보 리스트
    List<NewReleaseProductLabelResponseDto> getNewReleaseProductLabelInfo(Long productId) throws Exception;

    // 즁/소분류 제품 리스트 페이지에 같이 노출될 라벨 리스트 정보
    List<LabelResponseDto> getMiddleAndDownCategoryProductsRelatedLabelList(
            @Param("productIdList") List<Long> productIdList) throws Exception;
}