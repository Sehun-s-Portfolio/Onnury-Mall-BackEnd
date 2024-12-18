package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashBoardMonthlySalesResponseDto {
    private int year; // 년도
    private int month; // 월
    private Long totalSalesOrderCount; // 주문 건 수
    private Long totalSalesAmount; // 매출 금액 합계
    private Long totalPurchaseAmount; // 매입 금액 합계
    private Long totalSalesAmountProfit; // 매출 이익
    private String totalSalesProfitMargin; // 매출 이익률
}
