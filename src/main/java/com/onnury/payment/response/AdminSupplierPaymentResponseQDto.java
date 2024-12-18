package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminSupplierPaymentResponseQDto {


    private String orderNumber; // 주문번호
    private String supplierId; // 공급사명
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private LocalDateTime orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액

    private String cancleorderNumber; // 주문번호
    private String canclesupplierId; // 공급사명
    private String cancleproductName; // 상세 옵션 타이틀
    private String cancleproductClassificationCode; // 제품 구분 코드
    private String cancledetailOptionTitle; // 제품 명
    private int canclecancelAmount; // 수량
    private String cancleonNuryStatementNumber; // 신용/체크 카드 전표 번호
    private int cancleonNuryCanclePrice; // 신용/체크 카드 결제 승인 금액
    private String canclecreditStatementNumber; // 신용/체크 카드 전표 번호
    private int canclecreditCanclePrice; // 신용/체크 카드 결제 승인 금액
    private LocalDateTime cancelRequestAt;

    @QueryProjection
    public AdminSupplierPaymentResponseQDto(String orderNumber, String supplierId, String detailOptionTitle, String productClassificationCode,
                                            String productName, int quantity,LocalDateTime orderedAt, String buyMemberLoginId, String creditStatementNumber,
                                            int creditApprovalPrice, String onNuryStatementNumber, int onNuryApprovalPrice,

                                            String cancleorderNumber, String canclesupplierId, String cancleproductName,String cancleproductClassificationCode,
                                            String cancledetailOptionTitle, int canclecancelAmount, String cancleonNuryStatementNumber, int cancleonNuryCanclePrice,
                                            String canclecreditStatementNumber, int canclecreditCanclePrice, LocalDateTime cancelRequestAt
                                            ) {

        this.orderNumber = orderNumber;
        this.supplierId = supplierId;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.creditStatementNumber = creditStatementNumber;
        this.creditApprovalPrice = creditApprovalPrice;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.onNuryApprovalPrice = onNuryApprovalPrice;

        this.cancleorderNumber = cancleorderNumber;
        this.canclesupplierId = canclesupplierId;
        this.cancleproductName = cancleproductName;
        this.cancleproductClassificationCode = cancleproductClassificationCode;
        this.cancledetailOptionTitle = cancledetailOptionTitle;
        this.canclecancelAmount = canclecancelAmount;
        this.cancleonNuryStatementNumber = cancleonNuryStatementNumber;
        this.cancleonNuryCanclePrice = cancleonNuryCanclePrice;
        this.canclecreditStatementNumber = canclecreditStatementNumber;
        this.canclecreditCanclePrice = canclecreditCanclePrice;
        this.cancelRequestAt = cancelRequestAt;

    }
}
