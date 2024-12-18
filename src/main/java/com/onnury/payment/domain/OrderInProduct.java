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
public class OrderInProduct extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long orderInProductId;

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
    private String frcNumber; // 온누리 가맹 코드

    @Column
    private String businessNumber; // 사업자 번호

    @Column
    private int productAmount; // 제품가격(이벤트 기간 시 이벤트 가격)

    @Column
    private int productOptionAmount; // 제품옵션 추가 가격()

    @Column(nullable = false)
    private int quantity; // 수량

    @Column
    private int deliveryPrice; // 배송비

    @Column
    private int dangerPlacePrice; // 도서 산간 비용

    @Column
    private int onnurypay; // 온누리 사용금액

    @Column
    private int onnuryCommissionPrice; // 온누리 수수료 금액

    @Column
    private int creditCommissionPrice; // 신용 카드 수수료 금액

    @Column(nullable = false)
    private int productTotalAmount; // 총 상품 금액 ((상품 + 옵션 추가 가격 ) * 수량) + 배송비 + 도서산간비

    @Column
    private String memo; // 이벤트 가격시  - 이벤트 비고

    @Column
    private String transportNumber; // 운송장 번호

    @Column
    private String parcelName; // 택배사

    @Column
    private String completePurchaseCheck; // 구매 확정 여부

    @Column
    private LocalDateTime completePurchaseAt; // 구매 확정 일자

    @Column
    private int cancelAmount; // 취소 수량

    @Column
    private String eventCheck; // 이벤트 유무

    @Column(columnDefinition = "LONGTEXT")
    private String eventInfo; // 이벤트 비고

    @Column
    private Long cartId; // 장바구니에 담겼을 경우 판단하기 위한 장바구니 id 필드

}
