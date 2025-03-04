package com.onnury.payment.request;

import lombok.Getter;

@Getter
public class TransportInfoRequestDto {
    private String orderNumber; // 주문 번호
    private String seq; // 주문 제품 sequence
    private String parcelName; // 택배사 명
    private String transportNumber; // 운송장 번호
}
