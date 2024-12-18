package com.onnury.exception.member;

import com.onnury.member.domain.Member;
import com.onnury.member.request.MemberRegistRequestDto;
import com.onnury.query.member.MemberQueryData;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.onnury.member.domain.QMember.member;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberException implements MemberExceptionInterface {

    private final JPAQueryFactory jpaQueryFactory;
    private final PasswordEncoder passwordEncoder;
    private final MemberQueryData memberQueryData;

    /**
     * 회원가입 정보 정합성 검증
     **/
    @Override
    public String checkRegistMemberInfo(MemberRegistRequestDto memberRegistRequestDto) {

        if (memberRegistRequestDto.getType().equals("C")) {

            if (memberRegistRequestDto.getLoginId().isEmpty() || memberRegistRequestDto.getPassword().isEmpty()
                    || memberRegistRequestDto.getUserName().isEmpty() || memberRegistRequestDto.getBirth().isEmpty()
                    || memberRegistRequestDto.getAddress().isEmpty() || memberRegistRequestDto.getDetailAddress().isEmpty()
                    || memberRegistRequestDto.getEmail().isEmpty() || memberRegistRequestDto.getPhone().isEmpty()) {
                return "회원가입 하고자 하는 일반 고객의 입력 정보가 옳바르지 않습니다.";

            }

        } else if (memberRegistRequestDto.getType().equals("B")) {

            if (memberRegistRequestDto.getLoginId().isEmpty() || memberRegistRequestDto.getPassword().isEmpty()
                    || memberRegistRequestDto.getManager().isEmpty() || memberRegistRequestDto.getBusinessNumber().isEmpty()
                    || memberRegistRequestDto.getAddress().isEmpty() || memberRegistRequestDto.getDetailAddress().isEmpty()
                    || memberRegistRequestDto.getEmail().isEmpty() || memberRegistRequestDto.getPhone().isEmpty()
                    || memberRegistRequestDto.getLinkCompany().isEmpty()) {
                return "회원가입 하고자 하는 기업 고객의 입력 정보가 옳바르지 않습니다.";
            }

        }

        if (jpaQueryFactory
                .selectFrom(member)
                .where(member.email.eq(memberRegistRequestDto.getEmail()))
                .fetchOne() != null) {

            return "이미 존재하는 이메일입니다.";
        }

        return null;
    }


    /**
     * 회원가입 시 비밀번호와 재확인용 비밀번호 일치 여부 검증
     **/
    @Override
    public String checkRightPassword(String password, String checkPassword) {

        if (!password.equals(checkPassword)) {
            return "비밀번호와 재확인용 비밀번호 일치하지 않습니다.";
        }

        return null;
    }


    /**
     * 이미 존재한 계정인지 확인
     **/
    @Override
    public String checkAlreadyExistAccount(String loginId) {

        Member checkMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.loginId.eq(loginId))
                .fetchOne();

        if (checkMember != null) {
            return "이미 존재한 계정입니다.";
        }

        return null;
    }


    /**
     * 로그인 시도 시 해당 계정이 존재하는지 확인
     **/
    @Override
    public boolean checkLoginAccount(String loginId, String password) {

        // 입력한 로그인 아이디 기준 계정 정보 조회
        Member loginMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.loginId.eq(loginId))
                .fetchOne();

        // 로그인 아이디를 가진 계정 확인
        if (loginMember == null) {
            return true;
        }

        // 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(password, loginMember.getPassword())) {
            return true;
        }

        return false;
    }

    /**
     * 고객 로그인 계정이 존재하는지 확인
     **/
    @Override
    public String checkExistMemberLoginId(String loginId) {
        String[] memberLoginAccount = loginId.split("-");
        Member loginMember = memberQueryData.getLoginMemberAccount(memberLoginAccount[1]);

        if (loginMember == null) {
            return null;
        }

        if (loginMember.getType().equals("C")) {
            return "client";
        } else if (loginMember.getType().equals("B")) {
            return "business";
        }

        return null;
    }
}
