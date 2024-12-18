package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminOrderInProductResponseQDto2 {

    private String orderNumber; // 주문번호
    private String linkCompany;
    private String supplierName; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private String orderedAt; // 주문 일자
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액
    private int creditCanclePrice; // 온누리 결제 승인 금액
    private int onNuryCanclePrice; // 온누리 결제 승인 금액
    private int onnuryCommission;
    private int creditCommission;
    private int onnuryCommissionPrice;
    private int creditCommissionPrice;
    private String eventCheck;
    private String eventInfo;

    @QueryProjection
    public AdminOrderInProductResponseQDto2(String orderNumber, String linkCompany, String supplierName, String detailOptionTitle, String productClassificationCode,
                                            String productName, int quantity, String orderedAt, String creditStatementNumber,
                                            int creditApprovalPrice, String onNuryStatementNumber, int onNuryApprovalPrice, int creditCanclePrice, int onNuryCanclePrice,
                                            int onnuryCommission, int creditCommission, int onnuryCommissionPrice, int creditCommissionPrice, String eventCheck, String eventInfo

                               ) {
        this.orderNumber = orderNumber;
        this.linkCompany = linkCompany;
        this.supplierName = supplierName;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.creditStatementNumber = creditStatementNumber;
        this.creditApprovalPrice = creditApprovalPrice;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.onNuryApprovalPrice = onNuryApprovalPrice;
        this.creditCanclePrice = creditCanclePrice;
        this.onNuryCanclePrice = onNuryCanclePrice;
        this.onnuryCommission = onnuryCommission;
        this.creditCommission = creditCommission;
        this.onnuryCommissionPrice = onnuryCommissionPrice;
        this.creditCommissionPrice = creditCommissionPrice;
        this.eventCheck = eventCheck;
        this.eventInfo = eventInfo;
    }
}
