package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CancelPaymentResponseDto {
    private String orderNumber; // 주문번호
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String seq;
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private LocalDateTime orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String message;
    private int deliveryPrice;
    private int dangerPlacePrice;
    private LocalDateTime completePaymentAt;
    private String receiverPhone;
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액
    private int totalApprovalPrice; // 총 결제 승인 금액
    private String transportNumber; // 운송장 번호
    private String parcelName; // 택배사
    private String receiver; // 수령자
    private String address; // 주소

    @QueryProjection
    public CancelPaymentResponseDto(String orderNumber, String detailOptionTitle,
                                    String seq, String productClassificationCode, String productName, int quantity,
                                    LocalDateTime orderedAt, String buyMemberLoginId,
                                    String message, int deliveryPrice, int dangerPlacePrice, LocalDateTime completePaymentAt,
                                    String receiverPhone, String creditStatementNumber, int creditApprovalPrice,
                                    String onNuryStatementNumber, int onNuryApprovalPrice,
                                    int totalApprovalPrice, String transportNumber, String parcelName,
                                    String receiver, String address) {

        this.orderNumber = orderNumber;
        this.detailOptionTitle = detailOptionTitle;
        this.seq = seq;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.message = message;
        this.deliveryPrice = deliveryPrice;
        this.dangerPlacePrice = dangerPlacePrice;
        this.completePaymentAt = completePaymentAt;
        this.receiverPhone = receiverPhone;
        this.creditStatementNumber = creditStatementNumber;
        this.creditApprovalPrice = creditApprovalPrice;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.onNuryApprovalPrice = onNuryApprovalPrice;
        this.totalApprovalPrice = totalApprovalPrice;
        this.transportNumber = transportNumber;
        this.parcelName = parcelName;
        this.receiver = receiver;
        this.address = address;
    }
}
