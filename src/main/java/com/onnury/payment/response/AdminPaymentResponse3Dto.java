package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminPaymentResponse3Dto {


    private String orderNumber; // 주문번호
    private String supplierId; // 공급사 코드
    private String supplierName; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private String orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String cancelCheck;
    private String cancelAt; // 주문 일자
    private String seq; // 제품 seq

    @QueryProjection
    public AdminPaymentResponse3Dto(String orderNumber, String supplierId, String supplierName, String detailOptionTitle,
                                    String productClassificationCode, String productName, int quantity,
                                    String orderedAt, String buyMemberLoginId,String cancelCheck, String cancelAt,
                                    String seq) {

        this.orderNumber = orderNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.cancelCheck = cancelCheck;
        this.cancelAt = cancelAt;
        this.seq = seq;
    }
}
