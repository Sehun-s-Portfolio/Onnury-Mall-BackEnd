package com.onnury.payment.response;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Builder
@Getter
public class AdminPaymentResponseDto {

    private Long orderInProductId;
    private String orderNumber; // 주문번호
    private String supplierCode; // 공급사 코드
    private String supplierName; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private int useOnnuryPayAmount; // 사용 온누리 페이 금액
    private String completePurchaseCheck; // 구매 확정 여부
    private LocalDateTime completePurchaseAt; // 구매 확정 일자
    private String transportNumber; // 운송장 번호
    private String parcelName; // 택배사
    private int deliveryPrice; // 배송비
    private int dangerPlacePrice; // 도서 산간 비용
    private int totalPrice; // 총 상품 금액 (상품 + 옵션 추가 가격)
    private String postNumber; // 우편 번호
    private String Address; // 주소
    private LocalDateTime orderedAt; // 주문 일자
}
