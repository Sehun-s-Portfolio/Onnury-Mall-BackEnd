package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminPaymentProductResponseDto {


    private String seq; // 주문번호
    private String supplierName; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private String orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액
    private int deliveryPrice; // 온누리 결제 승인 금액
    private int dangerPlacePrice; // 온누리 결제 승인 금액
    private int totalApprovalPrice; // 총 결제 승인 금액
    private String parcelName;
    private String transportNumber;
    private int cancelAmount;
    private int creditCommissionPrice;
    private int onnuryCommissionPrice;
    private String eventCheck;
    private String eventInfo;

    @QueryProjection
    public AdminPaymentProductResponseDto(String seq, String supplierName, String detailOptionTitle,
                                          String productClassificationCode, String productName, int quantity, String orderedAt, String buyMemberLoginId,
                                          String creditStatementNumber, int creditApprovalPrice, String onNuryStatementNumber, int onNuryApprovalPrice,
                                          int deliveryPrice,int dangerPlacePrice,
                                          int totalApprovalPrice, String parcelName, String transportNumber,int cancelAmount,
                                          int creditCommissionPrice, int onnuryCommissionPrice, String eventCheck, String eventInfo) {

        this.seq = seq;
        this.supplierName = supplierName;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.creditStatementNumber = creditStatementNumber;
        this.creditApprovalPrice = creditApprovalPrice;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.onNuryApprovalPrice = onNuryApprovalPrice;
        this.deliveryPrice = deliveryPrice;
        this.dangerPlacePrice = dangerPlacePrice;
        this.totalApprovalPrice = totalApprovalPrice;
        this.parcelName = parcelName;
        this.transportNumber = transportNumber;
        this.cancelAmount = cancelAmount;
        this.creditCommissionPrice = creditCommissionPrice;
        this.onnuryCommissionPrice = onnuryCommissionPrice;
        this.eventCheck = eventCheck;
        this.eventInfo = eventInfo;
    }
}
