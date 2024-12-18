package com.onnury.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OnnuryPaymentFailMerchant {
    private String biz_no; // 온라인 전통 시장 사업자 코드 (판매자 사업자 번호)
    private String biz_nm; // 온라인 전통 시장관 명
}
