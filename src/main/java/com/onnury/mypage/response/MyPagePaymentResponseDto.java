package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class MyPagePaymentResponseDto {
    private Long productOrderId;
    private String orderNumber;
    private List<MyPageOrderInProductOfPaymentResponseDto> orderInProductList;
    private LocalDateTime createdDate;
    private String receiver;
    private String receiverPhone;
    private String email;
    private String address;
    private String paymentType;
    private String bankAccount;
    private String paymentResult;
    private Long totalPaymentPrice;
}
