package com.onnury.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OnnuryPaymentApprovalInfo {
    private String merchantOrderDt;
    private String merchantOrderID;
    private String tid;
    private int totalAmount;
    private String token;
    private String status;
}
