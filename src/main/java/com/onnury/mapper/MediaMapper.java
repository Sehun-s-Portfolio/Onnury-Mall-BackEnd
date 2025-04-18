package com.onnury.mapper;

import com.onnury.media.response.MediaResponseDto;
import com.onnury.product.response.ProductDetailImageInfoResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface MediaMapper {
    // 신 상품 정보에 들어있는 연관 이이미지 id 리스트 정보를 통한 연관 이미지 리스트 조회
    List<MediaResponseDto> getProductImagesByRelateImgIds(@Param("productId") Long productId, @Param("relateImgIdList") List<Long> relateImgIdList) throws Exception;

    // 신 상품 정보에 들어있는 연관 이미지 id 리스트 정보가 존재하지 않을 시 제품 이미지 정보 리스트 추출
    List<MediaResponseDto> getProductImagesByProductIdAndType(Long productId) throws Exception;

    // 신 상품 정보에 들어있는 연관 이미지 id 리스트 정보가 존재하지 않을 시 제품 상세 이미지 정보 리스트 추출
    List<ProductDetailImageInfoResponseDto> getProductDetailInfoImageByProductIdAndType(Long productId) throws Exception;
}