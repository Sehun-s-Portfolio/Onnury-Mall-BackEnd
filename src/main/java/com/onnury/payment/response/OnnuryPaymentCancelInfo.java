package com.onnury.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OnnuryPaymentCancelInfo {
    private String merchantCancelDt; // 가맹점 취소 일자
    private String merchantCancelID; // 가맹점 취소 번호
    private String merchantOrderDt; // 가맹점 주문 일자
    private String merchantOrderID; // 가맹점 주문 번호
    private String tid; // 원 결제 거래 고유 번호
    private int totalAmount; // 결제 금액
    private int totalCancelAmount; // 취소 금액 (partYn = ‘Y’ -> 취소 상품 정보 취소 금액의 합 | partYn = ‘N’ -> 결제 금액과 동일)
    private int cancelTaxFreeAmount; // 상품 부가세 금액
    private int cancelVatAmount; // 취소 비과세 금액
    private String partYn; // 부분 취소 여부 (Y : 부분 취소, N : 전체 취소)
    private String status;
    private List<OnnuryPaymentCancelProductItem> productItems; // 취소 상품 정보 (productItem 배열)
}
