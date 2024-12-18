package com.onnury.payment.request;

import lombok.Getter;

import java.util.List;

@Getter
public class PaymentProductRequestDto {
    private List<paymentProductRequestListDto> productInfo;
}
