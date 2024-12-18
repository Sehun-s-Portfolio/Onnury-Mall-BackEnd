package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminPaymentDeriveryResponseDto {

    private String orderNumber; // 주문 번호 (묶음 번호)
    private String seq; // 주문 번호 (묶음 번호)
    private String productName; // 제품 명
    private String supplierId; // 공급사 코드
    private String supplierName;
    private int onnuryPay; // 온누리 사용금액
    private int creditPay;
    private int amount;
    private String cancleStatus;
    private String frcNumber; // 온누리 가맹 코드
    private String businessNumber; // 사업자 번호

    @QueryProjection
    public AdminPaymentDeriveryResponseDto(String orderNumber, String seq, String productName,
                                           String supplierId, String supplierName, int onnuryPay, int creditPay, int amount, String cancleStatus,
                                           String frcNumber, String businessNumber) {
        this.orderNumber = orderNumber;
        this.seq = seq;
        this.productName = productName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.onnuryPay = onnuryPay;
        this.creditPay = creditPay;
        this.amount = amount;
        this.cancleStatus = cancleStatus;
        this.frcNumber = frcNumber;
        this.businessNumber = businessNumber;

    }
}
