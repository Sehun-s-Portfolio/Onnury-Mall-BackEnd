package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminCancleOrderResponseQDto {


    private String orderNumber; // 주문번호
    private String supplierId; // 공급사명
    private String productName; // 제품 명
    private String productClassificationCode; // 제품 구분 코드
    private String detailOptionTitle; // 상세 옵션 타이틀

    private int canclecancelAmount; // 수량
    private String cancleonNuryStatementNumber; // 신용/체크 카드 전표 번호
    private int cancleonNuryCanclePrice; // 신용/체크 카드 결제 승인 금액
    private String canclecreditStatementNumber; // 신용/체크 카드 전표 번호
    private int canclecreditCanclePrice; // 신용/체크 카드 결제 승인 금액
    private LocalDateTime cancelRequestAt;

    @QueryProjection
    public AdminCancleOrderResponseQDto(String orderNumber, String supplierId, String detailOptionTitle, String productClassificationCode,
                                        String productName,int canclecancelAmount, String cancleonNuryStatementNumber, int cancleonNuryCanclePrice,
                                        String canclecreditStatementNumber, int canclecreditCanclePrice, LocalDateTime cancelRequestAt
                                            ) {

        this.orderNumber = orderNumber;
        this.supplierId = supplierId;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.canclecancelAmount = canclecancelAmount;
        this.cancleonNuryStatementNumber = cancleonNuryStatementNumber;
        this.cancleonNuryCanclePrice = cancleonNuryCanclePrice;
        this.canclecreditStatementNumber = canclecreditStatementNumber;
        this.canclecreditCanclePrice = canclecreditCanclePrice;
        this.cancelRequestAt = cancelRequestAt;

    }
}
