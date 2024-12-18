package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminPaymentMemberPosterResponseDto {


    private String orderNumber; // 주문번호
    private String orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String userName; // 회원 고객 명
    private String birth; // 생년월일
    private String postNumber; // 우편번호
    private String address; // 주소
    private String detailAddress; // 상세주소
    private String email; // 이메일 / 담당자 이메일
    private String phone; // 연락처 / 담당자 연락처
    private String type; // 회원유형(일반 - C, 기업 - B)
    private String businessNumber; // 사업자번호
    private String manager; // 담당자명
    private String receiver; // 수령자
    private String receiverPhone; // 수령자 전화번호
    private String posterAddress; // 주소
    private String message; // 배송 메세지



    @QueryProjection
    public AdminPaymentMemberPosterResponseDto(String orderNumber,String orderedAt ,String buyMemberLoginId, String userName, String birth,
                                               String postNumber, String address, String detailAddress, String email, String phone,
                                               String type, String businessNumber, String manager,
                                               String receiver, String receiverPhone,String posterAddress, String message) {

        this.orderNumber = orderNumber;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.userName = userName;
        this.birth = birth;
        this.postNumber = postNumber;
        this.address = address;
        this.detailAddress = detailAddress;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.businessNumber = businessNumber;
        this.manager = manager;
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
        this.posterAddress = posterAddress;
        this.message = message;
    }
}
