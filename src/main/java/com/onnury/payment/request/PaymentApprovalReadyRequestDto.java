package com.onnury.payment.request;

import lombok.Getter;

@Getter
public class PaymentApprovalReadyRequestDto {
    private String merchantOrderDt; // 가맹점 주문 일자
    private String merchantOrderID; // 가맹점 주문 번호
    private String tid; // 결제 거래 고유 번호
    private String token; // 결제 승인 요청을 인증하는 토큰
    private int totalAmount; // 전체 결제 금액
}
