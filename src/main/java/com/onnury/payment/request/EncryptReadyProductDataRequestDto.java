package com.onnury.payment.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EncryptReadyProductDataRequestDto {
    private String seq; // 일련 번호 (중복 불가)
    private String category; // 상품 카테고리 (주1)
    private String frc_cd; // 온라인 전통 시장 가맹점 코드
    private String biz_no; // 온라인 전통 시장 사업자 코드 (판매자 사업자 번호)
    private String name; // 상품 명
    private int count; // 수량
    private int amount; // 결제 금액
}
