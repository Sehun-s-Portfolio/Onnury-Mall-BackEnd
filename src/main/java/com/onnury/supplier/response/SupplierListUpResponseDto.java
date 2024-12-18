package com.onnury.supplier.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SupplierListUpResponseDto {

    private List<SupplierDataResponseDto> supplierDataResponseDto; // 페이지 조건에따른 리스트
    private Long total ; // 데이터 총 갯수

}