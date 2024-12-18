package com.onnury.payment.request;

import lombok.Getter;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PaymentProductListRequestDto {
    private Long cartId; // 장바구니 id
    private String seq; // 주문 번호 (묶음 번호)
    private String productName; // 제품 명
    private String productClassificationCode; // 제품 구분 코드
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String supplierId; // 공급사 코드
    private String frcNumber; // 온누리 가맹 코드
    private String businessNumber; // 사업자 번호
    private int productAmount; // 제품가격(이벤트 기간 시 이벤트 가격)
    private int productOptionAmount; // 제품옵션 추가 가격()
    private int quantity; // 수량
    private int deliveryPrice; // 배송비
    private int dangerPlacePrice; // 도서 산간 비용
    private String onnurypay; // 온누리 사용금액
    private int productTotalAmount; // 총 상품 금액 ((상품 + 옵션 추가 가격 ) * 수량) + 배송비 + 도서산간비
    private String memo; // 이벤트 가격시  - 이벤트 비고

}
