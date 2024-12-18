package com.onnury.exception.mypage;

import com.onnury.member.domain.Member;
import com.onnury.mypage.request.MyPageChangePasswordRequestDto;
import org.springframework.stereotype.Component;

@Component
public interface MyPageExceptionInterface {

    /** 마이페이지 비밀번호 재설정 시 기존 비밀번호 및 입력한 새로운 비밀번호의 정보가 옳바른지 확인 **/
    boolean checkChangePasswordInfo(Member authMember, MyPageChangePasswordRequestDto myPageChangePasswordRequestDto);

}
