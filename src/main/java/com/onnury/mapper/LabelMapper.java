package com.onnury.mapper;

import com.onnury.label.response.LabelResponseDto;
import com.onnury.label.response.NewReleaseProductLabelResponseDto;
import com.onnury.product.domain.Product;
import com.onnury.product.response.NewReleaseProductInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface LabelMapper {

    // 신 상품에 연관된 라벨 정보 리스트
    List<NewReleaseProductLabelResponseDto> getNewReleaseProductLabelInfo(Long productId) throws Exception;

    // 중,소분류 제품들이 가지고 있는 필터링 라벨 데이터 호출
    List<LabelResponseDto> getMiddleAndDownCategoryProductsRelatedLabelList(List<Long> productIdList) throws Exception;
}