package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MyPageInfoResponseDto {
    private String type; // 고객 유형
    private Long memberId; // 고객 id
    private String loginId; // 로그인 아이디
    private String userName; // 고객 명
    private String birth; // 생년 월일
    private String address; // 주소
    private String detailAddress; // 상세 주소
    private String postNumber; // 우편 번호
    private String email; // 이메일 (기업 = 담당자 이메일)
    private String phone; // 연락처 (기업 = 담당자 연락처)
    private String businessNumber; // (기업 회원 전용) 사업자 번호
    private String manager; // (기업 회원 전용) 담당자 명
}
