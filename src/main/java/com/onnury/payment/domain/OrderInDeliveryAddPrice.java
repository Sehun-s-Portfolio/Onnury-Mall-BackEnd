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
public class OrderInDeliveryAddPrice extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long orderInDeliveryAddPriceId;

    @Column(nullable = false)
    private String orderNumber; // 주문 번호 (묶음 번호)

    @Column(nullable = false)
    private String seq; // 주문 번호 (묶음 번호)

    @Column(nullable = false)
    private String productName; // 제품 명

    @Column
    private String supplierId; // 공급사 코드

    @Column
    private int onnuryPay; // 온누리 사용금액

    @Column
    private int creditPay; // 신용 카드 사용금액

    @Column
    private int amount;

    @Column
    private String cancleStatus;

    @Column
    private String frcNumber; // 온누리 가맹 코드

    @Column
    private String businessNumber; // 사업자 번호


}
