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
public class ProductOrder extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long proudctOrderId;

    @Column(nullable = false)
    private String orderNumber; // 주문 번호

    @Column(nullable = false)
    private String receiver; // 수령자

    @Column(nullable = false)
    private String postNumber; // 우편 번호

    @Column(nullable = false)
    private String Address; // 주소

    @Column
    private String message; // 배송 메세지

    @Column(nullable = false)
    private String receiverPhone; // 수령자 전화번호

    @Column(nullable = false)
    private LocalDateTime orderedAt; // 주문 일자

    @Column
    private LocalDateTime completePaymentAt; // 결제 완료 일자

    @Column(nullable = false)
    private int totalPurchasePrice; // 총 구매 금액

    @Column(nullable = false)
    private String buyMemberLoginId; // 구매자 로그인 아이디
}
