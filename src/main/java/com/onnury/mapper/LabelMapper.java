package com.onnury.mapper;

import com.onnury.label.response.NewReleaseProductLabelResponseDto;
import com.onnury.product.domain.Product;
import com.onnury.product.response.NewReleaseProductInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface LabelMapper {

    // 신 상품에 연관된 라벨 정보 리스트
    List<NewReleaseProductLabelResponseDto> getNewReleaseProductLabelInfo(Long productId) throws Exception;

}