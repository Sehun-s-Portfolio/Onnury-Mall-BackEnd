package com.onnury.member.request;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
public class MemberRegistRequestDto {
    @NotBlank
    private String type; // 고객 유형 (일반 - C, 기업 - B)
    @NotBlank
    @Size(min=6, message="아이디는 최소 6자 이상이여야 합니다.")
    private String loginId; // 로그인 아이디
    @NotBlank
    @Size(min=4, message="비밀번호는 최소 4자 이상이여야 합니다.")
    private String password; // 비밀번호
    @NotBlank
    @Size(min=4, message="비밀번호는 최소 4자 이상이여야 합니다.")
    private String checkPassword; // 재확인용 비밀번호
    private String userName; // 고객명
    private String birth; // 생년월일
    private String businessNumber; // 사업자 번호
    private String postNumber; // 우편 번호
    @NotBlank
    private String address; // 주소
    @NotBlank
    private String detailAddress; // 상세 주소
    private String email; // 이메일 / 담당자 이메일
    private String phone; // 연락처 / 담당자 연락처
    private String manager; // 담당자 명
    private String linkCompany; // 기업 링크 컴퍼니 명

}
