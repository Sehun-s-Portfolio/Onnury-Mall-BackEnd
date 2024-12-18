package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashBoardTop10ProductResponseDto {
    private String productClassficationCode; // 제품 id
    private String productName; // 제품 명
    private Long totalSalesQuantity; // 제품 매출 수량
    private Long totalSalesAmount; // 제품 매출 금액
}
