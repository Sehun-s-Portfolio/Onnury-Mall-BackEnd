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
public class Payment extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long paymentId;

    @Column(nullable = false)
    private String orderNumber; // 주문 번호 (묶음 번호)

    @Column(nullable = false)
    private String buyMemberLoginId; // 구매자 로그인 아이디

    @Column(nullable = false)
    private String receiver; // 수령자

    @Column(nullable = false)
    private String postNumber; // 우편 번호

    @Column(nullable = false)
    private String address; // 주소

    @Column
    private String message; // 배송 메세지

    @Column(nullable = false)
    private String receiverPhone; // 수령자 전화번호

    @Column
    private String linkCompany; // 출처

    @Column
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid

    @Column
    private String creditStatementNumber; // 신용/체크 카드 전표 번호

    @Column
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액

    @Column
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액

    @Column(nullable = false)
    private int totalApprovalPrice; // 총 결제 승인 금액

    @Column(nullable = false)
    private LocalDateTime orderedAt; // 주문 일자

}
