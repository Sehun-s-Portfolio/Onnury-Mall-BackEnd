package com.onnury.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OnnuryPaymentCancelProductItem {
    private String seq; // 일련 번호 (결제 준비 productItems 일련 번호와 동일)
    private String frc_cd; // 온라인 전통 시장 가맹점 코드
    private String biz_no; // 온라인 전통 시장 사업자 코드 (판매자 사업자 번호)
    private int amount; // 결제 금액 (결제 준비 productItems 결제 금액과 동일)
    private int cancelAmount; // 취소 금액
}
