package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminPaymentResponse2Dto {
    private String orderNumber; // 주문번호
    private String linkCompany;
    private String supplierCode; // 공급사 코드
    private String supplierName; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private String transportNumber; // 운송장 번호
    private String parcelName; // 택배사
    private String receiver; // 수령자
    private String Address; // 주소
    private String orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String buyMemberName; // 주문자 명
    private int cancelAmount;
    private int creditCommissionPrice;
    private int onnuryCommissionPrice;
    private String eventCheck;
    private String eventInfo;
    private String completePurchaseAt;
    private String completePurchaseCheck;

    @QueryProjection
    public AdminPaymentResponse2Dto(String orderNumber, String linkCompany, String supplierCode, String supplierName, String detailOptionTitle,
                                    String productClassificationCode, String productName, int quantity, String transportNumber, String parcelName,
                                    String receiver, String Address, String orderedAt, String buyMemberLoginId, String buyMemberName, int cancelAmount,
                                    int creditCommissionPrice, int onnuryCommissionPrice, String eventCheck, String eventInfo, String completePurchaseAt, String completePurchaseCheck) {

        this.orderNumber = orderNumber;
        this.linkCompany = linkCompany;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.transportNumber = transportNumber;
        this.parcelName = parcelName;
        this.receiver = receiver;
        this.Address = Address;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.buyMemberName = buyMemberName;
        this.cancelAmount = cancelAmount;
        this.creditCommissionPrice = creditCommissionPrice;
        this.onnuryCommissionPrice = onnuryCommissionPrice;
        this.eventCheck = eventCheck;
        this.eventInfo = eventInfo;
        this.completePurchaseAt = completePurchaseAt;
        this.completePurchaseCheck = completePurchaseCheck;
    }
}
