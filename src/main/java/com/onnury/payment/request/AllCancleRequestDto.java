package com.onnury.payment.request;

import lombok.Getter;

import java.util.List;

@Getter
public class AllCancleRequestDto {

    private String orderNumber;
    private int onnuryCancelPay;
    private int pgCancelPay;
    private List<Long> cancelRequestIdList;
}
