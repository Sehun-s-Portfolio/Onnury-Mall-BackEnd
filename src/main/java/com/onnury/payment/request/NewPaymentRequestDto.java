package com.onnury.payment.request;

import lombok.Getter;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Getter
public class NewPaymentRequestDto {

    private String orderNumber; // 주문 번호 (묶음 번호)
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String receiver; // 수령자
    private String postNumber; // 우편 번호
    private String address; // 주소
    private String message; // 배송 메세지
    private String receiverPhone; // 수령자 전화번호
    private String linkCompany; // 출처
    private int totalApprovalPrice; // 총 결제 승인 금액


}
