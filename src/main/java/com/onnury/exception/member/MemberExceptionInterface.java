package com.onnury.exception.member;

import com.onnury.member.request.MemberRegistRequestDto;
import org.springframework.stereotype.Component;

@Component
public interface MemberExceptionInterface {

    /** 회원가입 정보 정합성 검증 **/
    String checkRegistMemberInfo(MemberRegistRequestDto memberRegistRequestDto);

    /** 회원가입 시 비밀번호와 재확인용 비밀번호 일치 여부 검증 **/
    String checkRightPassword(String password, String checkPassword);

    /** 이미 존재한 계정인지 확인 **/
    String checkAlreadyExistAccount(String loginId);

    /** 로그인 시도 시 해당 계정이 존재하는지 확인 **/
    boolean checkLoginAccount(String loginId, String password);

    /** 고객 로그인 계정이 존재하는지 확인 **/
    String checkExistMemberLoginId(String loginId);
}
