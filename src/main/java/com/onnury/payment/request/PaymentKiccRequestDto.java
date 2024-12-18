package com.onnury.payment.request;

import lombok.Getter;

@Getter
public class PaymentKiccRequestDto {
    private int pgtotalAmount;
    private int onnurytotalamount;
    private String orderNumber; // 온누리, EasyPay Redis 키 값
    private String productName;
    private String deviceType;
    private String merchantUserNm;
    private int deliveryPrice;
}
