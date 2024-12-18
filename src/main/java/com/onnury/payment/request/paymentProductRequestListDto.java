package com.onnury.payment.request;

import lombok.Getter;

import javax.persistence.Column;

@Getter
public class paymentProductRequestListDto {
    private String seq;
    private String detailOptionTitle; // 상세 옵션 타이틀
    private int productTotalAmount; // 제품별 옵션 포함 가격
    private String supplierId; //공급사 ID
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    //private String parcelName; // 택배사
    private int quantity; // 수량
    private int useOnnuryPayAmount; // 사용 온누리 페이 금액
    private int deliveryPrice; // 배송비
    private int dangerPlacePrice; //도서산간 추가비용

}
