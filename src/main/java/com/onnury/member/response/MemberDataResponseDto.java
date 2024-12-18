package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class MemberDataResponseDto {
    private Long memberId;
    private String userName;
    private String loginId; // 로그인아이디
    private String password; // 비밀번호
    private String birth; // 생년월일
    private String postNumber; // 우편번호
    private String address; // 주소
    private String detailAddress; // 상세주소
    private String email; // 이메일 / 담당자 이메일
    private String phone; // 연락처 / 담당자 연락처
    private String type; // 회원유형(일반 - C, 기업 - B)
    private String businessNumber; // 사업자번호
    private String manager; // 담당자명
    private String createdAt; // 생성 일자
}