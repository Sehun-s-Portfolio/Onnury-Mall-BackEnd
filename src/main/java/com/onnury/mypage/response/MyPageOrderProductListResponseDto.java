package com.onnury.mypage.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MyPageOrderProductListResponseDto {

    private String orderNumber; // 주문 번호 (묶음 번호)
    private String seq; // 주문 번호 (묶음 번호)
    private String productName; // 제품 명
    private String productImgurl;
    private String productClassificationCode; // 제품 구분 코드
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String supplierName;
    private Long supplierId;
    private int productAmount; // 제품가격(이벤트 기간 시 이벤트 가격)
    private int productOptionAmount; // 제품옵션 추가 가격()
    private int quantity; // 수량
    private int deliveryPrice; // 배송비
    private int dangerPlacePrice; // 도서 산간 비용
    private int onnurypay; // 온누리 사용금액
    private int productTotalAmount; // 총 상품 금액 ((상품 + 옵션 추가 가격 ) * 수량) + 배송비 + 도서산간비
    private String memo; // 이벤트 가격시  - 이벤트 비고
    private String transportNumber; // 운송장 번호
    private String parcelName; // 택배사
    private String completePurchaseCheck; // 구매 확정 여부
    private LocalDateTime completePurchaseAt; // 구매 확정 일자
    private int cancelAmount; // 취소 수량


    @QueryProjection
    public MyPageOrderProductListResponseDto(
            String orderNumber, String seq, String productName, String productImgurl,
            String productClassificationCode, String detailOptionTitle, String supplierName, Long supplierId, int productAmount,
            int productOptionAmount, int quantity, int deliveryPrice, int dangerPlacePrice, int onnurypay, int productTotalAmount,
            String memo, String transportNumber,
            String parcelName, String completePurchaseCheck, LocalDateTime completePurchaseAt, int cancelAmount) {

        this.orderNumber = orderNumber;
        this.seq = seq;
        this.productName = productName;
        this.productImgurl = productImgurl;
        this.productClassificationCode = productClassificationCode;
        this.detailOptionTitle = detailOptionTitle;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
        this.productAmount = productAmount;
        this.productOptionAmount = productOptionAmount;
        this.quantity = quantity;
        this.deliveryPrice = deliveryPrice;
        this.dangerPlacePrice = dangerPlacePrice;
        this.onnurypay = onnurypay;
        this.productTotalAmount = productTotalAmount;
        this.memo = memo;
        this.transportNumber = transportNumber;
        this.parcelName = parcelName;
        this.completePurchaseCheck = completePurchaseCheck;
        this.completePurchaseAt = completePurchaseAt;
        this.cancelAmount = cancelAmount;
    }

}
