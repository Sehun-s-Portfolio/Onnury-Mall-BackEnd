package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MyPageUpdateInfoResponseDto {
    private String loginId; // 로그인아이디
    private String birth; // (일반 고객 용) 생년 월 일
    private String address; // 주소
    private String detailAddress; // 상세 주소
    private String postNumber; // 우편 번호
    private String userName; // (일반 고객 용) 고객  명
    private String email; // 기업 : 담당자 이메일, 일반 : 고객 이메일
    private String phone; // 기업 : 담당자 연락처, 일반 : 고객 연락처
    private String businessNumber; // (기업 용) 사업자 번호
    private String manager; // (기업 용) 담당자 명
}
