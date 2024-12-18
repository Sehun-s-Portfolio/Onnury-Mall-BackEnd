package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConfirmPaymentResponseDto {
    private Long memberId;
    private String name;
    private String orderNumber;
    private String seq;
    private String confirmPurchaseAt;
    private String confirmPurchaseStatus;
}
