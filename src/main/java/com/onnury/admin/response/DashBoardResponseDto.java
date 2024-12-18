package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class DashBoardResponseDto {
    private Long totalSalesOrderCount; // 총 주문 건 수 (배송비, 도서산간 비용, 환불 같은 내용도 전부 별도의 요청 건 수로 구분한 주문 건 수)
    private Long totalSalesAmount; // 총 매출 금액 (배송비, 도서산간 비용, 환불 같은 내용을 전부 합한 총 매출 금액)
    private Long totalPurchasePrice; // 총 매입 급액
    private String totalSalesProfitMargin; // 매출 이익률 ((매출 금액 - 이익(판매가 - 매입가)) / 매출 금액 = 매출 이익률)
    private List<DashBoardMiddleCategoryOrderResponseDto> totalMiddleCategoryOrderCountInfo; // 중분류 카테고리 기준 주문 건 수
    private List<DashBoardUpCategoryOrderResponseDto> totalUpCategoryOrderCountInfo; // 대분류 카테고리 기준 주문 건 수
    private List<DashBoardMonthlySalesResponseDto> totalMonthlySalesSituation; // 월별 총 매출 금액, 총 매출 이익 금액, 총 매출 이익률
    private List<DashBoardTop10ProductResponseDto> totalTop10ProductsInfo; // 판매 매출 상위 10 제품 정보
}
