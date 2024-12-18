package com.onnury.payment.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class EncryptReadyApprovalDataRequestDto {
    private String merchantOrderDt; // 가맹점 주문일자
    private String merchantOrderID; // 가맹점 주문번호
    private String merchantUserKey; // 가맹점 회원키 또는 CI (가맹점에서 관리하는 사용자 식별 고유정보(예:ID))
    private String productName; // 상품 표시 명
    private int quantity; // 상품 총 수량
    private int totalAmount; // 전체결제금액 (상품정보 결제금액 총합)
    private int taxFreeAmount; // 상품 비과세 금액
    private int vatAmount; // 상품 부가세 금액
    private List<EncryptReadyProductDataRequestDto> productItems; // 상품 정보 productItem 배열
    private String approvalURL; // 결제 성공 시 return url
    private String cancelURL; // 결제 취소 시 return url
    private String failURL; // 결제 실패 시 return url
    private String complexYn; // 복합 결제 여부
    private String zipNo; // 우편 번호
}
