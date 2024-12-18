package com.onnury.exception.mypage;

import com.onnury.member.domain.Member;
import com.onnury.mypage.request.MyPageChangePasswordRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MyPageException implements MyPageExceptionInterface {

    private final PasswordEncoder passwordEncoder;

    /** 마이페이지 비밀번호 재설정 시 기존 비밀번호 및 입력한 새로운 비밀번호의 정보가 옳바른지 확인 **/
    @Override
    public boolean checkChangePasswordInfo(Member authMember, MyPageChangePasswordRequestDto myPageChangePasswordRequestDto) {

        // 새로 입력한 비밀번호와 확인용 비밀번호와 일치하지 않을 경우 예외 처리
        if(!myPageChangePasswordRequestDto.getNewPassword().equals(myPageChangePasswordRequestDto.getNewPasswordCheck())){
            log.info("새롭게 입력하신 비밀번호 정보가 재확인용 비밀번호와 일치하지 않습니다.");
            return true;
        }

        // 이전 비밀번호 확인 시 틀리면 예외 처리
        if(!passwordEncoder.matches(myPageChangePasswordRequestDto.getPresentPassword(), authMember.getPassword())){
            log.info("입력하신 이전 비밀번호가 옳바르지 않습니다.");
            return true;
        }

        // 만약 새로 변경할 비밀번호와 기존 비밀번호가 일치할 경우 예외 처리
        if (passwordEncoder.matches(myPageChangePasswordRequestDto.getNewPassword(), authMember.getPassword())) {
            log.info("이전 비밀번호와 동일한 변경 비밀번호입니다. 다시 입력해주십시오,");
            return true;
        }

        return false;
    }
}
