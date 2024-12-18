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
public class EasyPaymentApproval extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long easyPaymentApprovalId;

    @Column
    private String mallId; // KICC 에서 발급한 상점 아이디

    @Column
    private String pgCno; // PG 승인 거래 번호

    @Column
    private String shopTransactionId; // 가맹점 트랜잭션 ID

    @Column
    private String shopOrderNo; // 상점 주문 번호

    @Column
    private Long amount; // 총 승인 금액

    @Column
    private String transactionDate; // 거래 일시

    @Column
    private String statusCode; // 거래 상태 코드

    @Column
    private String statusMessage; // 거래 상태 메시지

    @Column
    private String msgAuthValue; // 메시지 인증 값

    @Column
    private String escrowUsed; // 에스크로 사용 여부

    @Column
    private String payMethodTypeCode; // 결제 수단 코드

    @Column
    private String approvalNo; // 결제 수단의 승인 번호

    @Column
    private String approvalDate; // 결제 수단의 거래 일시

    @Column
    private String cpCode; // 서비스 사 코드

    @Column
    private String multiCardAmount; // 페이코 복합 결제 신용 카드 승인 / 취소 금액

    @Column
    private String multiPntAmount; // 페이코 복합 결제 포인트 승인 / 취소 금액

    @Column
    private String multiCponAmount; // 페이코 복합 결제 쿠폰 승인 / 취소 금액

    @Column
    private String cardNo; // 카드 번호

    @Column
    private String issuerCode; // 발급사 코드

    @Column
    private String issuerName; // 발급사 명

    @Column
    private String acquirerCode; // 매입사 코드

    @Column
    private String acquirerName; // 매입사 명

    @Column
    private String installmentMonth; // 할부 개월

    @Column
    private String freeInstallmentTypeCode; // 무이자 타입

    @Column
    private String cardGubun; // 카드 종류

    @Column
    private String cardBizGubun; // 카드 발급 주체 구분

    @Column
    private String partCancelUsed; // 부분 취소 가능 여부

    @Column
    private Long couponAmount; // 즉시 할인 금액

    @Column
    private String subCardCd; // 빌키 발급 시 BC 제휴사 카드 코드

    @Column
    private String vanSno; // VAN 거래 일련 번호

    @Column
    private String bankCode; // 은행 코드

    @Column
    private String bankName; // 은행 명

    @Column
    private String virtualBankCode; // 가상 계좌 은행 코드

    @Column
    private String virtualBankName; // 가상 계좌 은행 명

    @Column
    private String accountNo; // 채번 계좌 번호 (가상 계좌 일 경우)

    @Column
    private String depositName; // 예금주 성명 (가상 계좌 일 경우)

    @Column
    private String expiryDate; // 계좌 사용 만료 일 (가상 계좌 일 경우)

    @Column
    private String authId; // Phone ID

    @Column
    private String mobBillId; // 인증 번호

    @Column
    private String mobileNo; // 휴대폰 번호

    @Column
    private String mobileAnsimUsed; // 휴대폰 안심 결제 (Y : 사용, N : 미사용)

    @Column
    private String prepaidBillId; // 뱅크 월렛(금결원) 승인 번호

    @Column
    private Long prepaidRemainAmount; // 선불 잔액

    @Column
    private String cashReceiptResCd; // 현금 영수증 결과 코드

    @Column
    private String cashReceiptResMsg; // 현금 영수증 결과 메시지

    @Column
    private String cashReceiptApprovalNo; // 현금 영수증 승인 번호

    @Column
    private String cashReceiptApprovalDate; // 현금 영수증 거래 일시

}
