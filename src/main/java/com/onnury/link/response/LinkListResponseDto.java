package com.onnury.link.response;

import com.onnury.link.domain.Link;
import com.onnury.supplier.response.SupplierDataResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class LinkListResponseDto {

    private List<Link> list; // 페이지 조건에따른 리스트
    private Long total ; // 데이터 총 갯수
}
