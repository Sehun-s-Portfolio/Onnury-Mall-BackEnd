package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberRegistResponseDto {
    private String type; // 고객 유형 (일반 - C, 기업 - B)
    private String loginId; // 로그인 아이디
    private String userName; // 고객명
    private String birth; // 생년월일
    private String postNumber; // 우편번호
    private String businessNumber; // 사업자 번호
    private String address; // 주소
    private String detailAddress; // 상세 주소
    private String email; // 이메일 / 담당자 이메일
    private String phone; // 연락처 / 담당자 연락처
    private String manager; // 담당자 명
}
