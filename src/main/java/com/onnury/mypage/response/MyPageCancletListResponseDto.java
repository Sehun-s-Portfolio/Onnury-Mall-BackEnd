package com.onnury.mypage.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MyPageCancletListResponseDto {


    private String orderNumber; // 주문 번호 (묶음 번호)
    private String productName; // 제품 명
    private String productImgurl;

    private String productClassificationCode; // 제품 구분 코드
    private String detailOptionTitle; // 상세 옵션 타이틀
    private int cancelAmount; // 제품가격(이벤트 기간 시 이벤트 가격)
    private LocalDateTime cancelAt; // 취소 요청 일자
    private String cancelCheck; // 출처

    private int onNuryCanclePrice; // 제품옵션 추가 가격()
    private int creditCanclePrice; // 배송비
    private LocalDateTime cancelRequestAt; // 도서 산간 비용




    @QueryProjection
    public MyPageCancletListResponseDto(String orderNumber, String productName, String productImgurl,
                                        String productClassificationCode, String detailOptionTitle, int cancelAmount, LocalDateTime cancelAt,
                                        String cancelCheck, int onNuryCanclePrice, int creditCanclePrice, LocalDateTime cancelRequestAt) {

        this.orderNumber = orderNumber;
        this.productName = productName;
        this.productImgurl = productImgurl;
        this.productClassificationCode = productClassificationCode;
        this.detailOptionTitle = detailOptionTitle;
        this.cancelAmount = cancelAmount;
        this.cancelAt = cancelAt;
        this.cancelCheck = cancelCheck;
        this.onNuryCanclePrice = onNuryCanclePrice;
        this.creditCanclePrice = creditCanclePrice;
        this.cancelRequestAt = cancelRequestAt;

    }

}
