package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminCancelPaymentResponseDto {
    private Long cancleOrderId; // 취소 요청 이력 id
    private String orderNumber; // 주문번호
    private String seq;
    private String productName; // 제품 명
    private String productClassificationCode; // 제품 구분 코드
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String supplierId;
    private String supplierName;
    private int productAmount; // 제품가격(이벤트 기간 시 이벤트 가격)
    private int productOptionAmount; // 제품옵션 추가 가격()
    private int deliveryPrice; // 배송비
    private int dangerPlacePrice; // 도서 산간 비용
    private int cancelAmount; // 취소 수량
    private int totalPrice; // 총 상품 금액 ((상품 + 옵션 추가 가격 ) * 취소수량) + 배송비 + 도서산간비
    private String onNuryStatementNumber; // 온누리취소 전표 번호 - tid
    private String creditStatementNumber; // 신용/체크 카드 취소전표 번호
    private int onNuryCanclePrice; // 온누리 결제 취소 금액
    private int creditCanclePrice; // 신용/체크 카드 결제 취소 금액
    private String cancelCheck; // 취소 확정 유무 (Y / N )
    private String cancelAt; // 취소 요청 일자
    private String cancelRequestAt; // 취소 확정 일자

    @QueryProjection
    public AdminCancelPaymentResponseDto(Long cancleOrderId, String orderNumber, String seq,
                                         String productName, String productClassificationCode, String detailOptionTitle,String supplierId,
                                         String supplierName, int productAmount, int productOptionAmount,
                                         int deliveryPrice, int dangerPlacePrice, int cancelAmount, int totalPrice,
                                         String onNuryStatementNumber, String creditStatementNumber, int onNuryCanclePrice,
                                         int creditCanclePrice, String cancelCheck, String cancelAt, String cancelRequestAt) {
        this.cancleOrderId = cancleOrderId;
        this.orderNumber = orderNumber;
        this.detailOptionTitle = detailOptionTitle;
        this.seq = seq;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.deliveryPrice = deliveryPrice;
        this.dangerPlacePrice = dangerPlacePrice;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.productAmount = productAmount;
        this.creditStatementNumber = creditStatementNumber;
        this.productOptionAmount = productOptionAmount;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.cancelAmount = cancelAmount;
        this.totalPrice = totalPrice;
        this.onNuryCanclePrice = onNuryCanclePrice;
        this.creditCanclePrice = creditCanclePrice;
        this.cancelCheck = cancelCheck;
        this.cancelAt = cancelAt;
        this.cancelRequestAt = cancelRequestAt;
    }
}
