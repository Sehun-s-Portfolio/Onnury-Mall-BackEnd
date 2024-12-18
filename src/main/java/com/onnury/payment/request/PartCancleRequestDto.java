package com.onnury.payment.request;

import lombok.Getter;

import java.util.List;

@Getter
public class PartCancleRequestDto {
    private String orderNumber;
    private int onnuryCancelPay;
    private int pgCancelPay;
    private String seq;
    private int quantity;
    private Long cancelRequestId;
}
