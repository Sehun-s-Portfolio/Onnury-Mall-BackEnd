package com.onnury.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OnnuryPaymentFailResponseDto {
    private String errorCode; // 에러 코드 (M01 : 전통 시장 가맹점 검증 오류)
    private String errorMsg; // 에러 메세지
    private List<OnnuryPaymentFailMerchant> failMerchants; // 검증 실패 가맹점
    private String status;
}
