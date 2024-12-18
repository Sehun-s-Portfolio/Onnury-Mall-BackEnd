package com.onnury.payment.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class PaymentZppReq extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long paymentZppReqId;

    @Column(nullable = false)
    private int seq; // 일련 번호

    @Column(nullable = false)
    private String zppID; // 상품권 ID

    @Column(nullable = false)
    private String zppName; // 상품권 명

    @Column(nullable = false)
    private String sellerID; // 상품권 판매처 ID

    @Column(nullable = false)
    private String sellerName; // 상품권 판매처 명

    @Column(nullable = false)
    private int amount; // 상품권 결제 금액

    @Column(nullable = false)
    private Long paymentApprovalId; // 연관된 결제 승인 정보 엔티티 id
}
