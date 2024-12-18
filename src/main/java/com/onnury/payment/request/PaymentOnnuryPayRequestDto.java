package com.onnury.payment.request;

import lombok.Getter;

import java.util.List;

@Getter
public class PaymentOnnuryPayRequestDto {
    private String merchantOrderID;
    private String merchantUserKey;
    private String productName;
    private int quantity;
    private int totalAmount;
    private List<ProductItemRequestDto> productItems;
}
