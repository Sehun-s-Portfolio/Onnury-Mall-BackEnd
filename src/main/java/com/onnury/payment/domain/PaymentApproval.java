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
@org.springframework.data.relational.core.mapping.Table
@Entity
public class PaymentApproval extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long paymentApprovalId;

    @Column(nullable = false)
    private String merchantOrderDt; // 가맹점 주문 일자

    @Column(nullable = false)
    private String merchantOrderID; // 가맹점 주문 번호

    @Column(nullable = false)
    private String tid; // 결제 거래 고유 번호

    @Column(nullable = false)
    private String productName; // 상품 명

    @Column(nullable = false)
    private int quantity; // 상품 수량

    @Column(nullable = false)
    private int totalAmount; // 전체 결제 금액

    @Column
    private int taxFreeAmount; // 비과세 금액

    @Column
    private int vatAmount; // 부가세 금액

    @Column(nullable = false)
    private String approvedAt; // 결제 승인 시각

    @Column(nullable = false)
    private String bankCd; // 결제 계좌 은행 코드

    @Column(nullable = false)
    private String accountNo; // 결제 계좌 뒷 번호 4자리

    @Column(nullable = false)
    private String payMeasureTp; // 결제 수단 구분 (14 : 온누리 페이)

    @Column(nullable = false)
    private String payZppNote; // 결제 수단 구분 (14일 때 대표 상품권 명)

}
