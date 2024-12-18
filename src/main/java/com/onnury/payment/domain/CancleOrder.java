package com.onnury.payment.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class CancleOrder extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long cancleOrderId;

    @Column(nullable = false)
    private String orderNumber; // 주문 번호 (묶음 번호)

    @Column(nullable = false)
    private String seq; // 주문 번호 (묶음 번호)

    @Column(nullable = false)
    private String productName; // 제품 명

    @Column(nullable = false)
    private String productClassificationCode; // 제품 구분 코드

    @Column
    private String detailOptionTitle; // 상세 옵션 타이틀

    @Column
    private String supplierId; // 공급사 코드

    @Column
    private int productAmount; // 제품가격(이벤트 기간 시 이벤트 가격)

    @Column
    private int productOptionAmount; // 제품옵션 추가 가격()

    @Column
    private int deliveryPrice; // 배송비

    @Column
    private int dangerPlacePrice; // 도서 산간 비용

    @Column
    private int cancelAmount; // 취소 수량

    @Column
    private String linkCompany; // 출처

    @Column(nullable = false)
    private int totalPrice; // 총 상품 금액 ((상품 + 옵션 추가 가격 ) * 취소수량) + 배송비 + 도서산간비

    @Column
    private String onNuryStatementNumber; // 온누리 취소 전표 번호 - tid

    @Column
    private String creditStatementNumber; // 신용/체크 카드 취소전표 번호

    @Column
    private int onNuryCanclePrice; // 온누리 결제 취소 금액

    @Column
    private int creditCanclePrice; // 신용/체크 카드 결제 취소 금액

    @Column
    private String cancelCheck; // 취소 확정 유무 (Y / N )

    @Column
    private LocalDateTime cancelAt; // 취소 요청 일자

    @Column
    private LocalDateTime cancelRequestAt; // 취소 확정 일자

}
