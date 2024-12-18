package com.onnury.brand.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class BrandListUpResponseDto {

    private List<BrandDataResponseDto> brandDataResponseDto; // 페이지 조건에따른 리스트
    private Long total ; // 데이터 총 갯수

}