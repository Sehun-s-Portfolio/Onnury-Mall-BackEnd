package com.onnury.mypage.request;

import lombok.Getter;

@Getter
public class ConfirmPaymentRequestDto {
    private String orderNumber; // 주문 확정할 주문 번호
    private String seq; // 주문 확정할 제품 seq
}
