package com.onnury.payment.response;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Builder
@Getter
public class AdminSupplierPaymentResponseDto {

    private Long orderInProductId;
    private String orderNumber; // 주문번호

    private String supplierCode; // 공급사 코드
    private String supplierName; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private String completePurchaseCheck; // 구매 확정 여부
    private LocalDateTime completePurchaseAt; // 구매 확정 일자
    private LocalDateTime orderedAt; // 주문 일자

    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액
    private int totalApprovalPrice; // 총 결제 승인 금액
}
