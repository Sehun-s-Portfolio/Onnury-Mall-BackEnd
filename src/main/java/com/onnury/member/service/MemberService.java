package com.onnury.member.service;

import com.onnury.common.util.LogUtil;
import com.onnury.exception.member.MemberException;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.jwt.JwtToken;
import com.onnury.jwt.JwtTokenDto;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.jwt.JwtTokenRepository;
import com.onnury.member.domain.Member;
import com.onnury.member.repository.MemberRepository;
import com.onnury.member.request.MemberLoginRequestDto;
import com.onnury.member.request.MemberRegistRequestDto;
import com.onnury.member.response.MemberDashboardResponseDto;
import com.onnury.member.response.MemberListUpResponseDto;
import com.onnury.member.response.MemberLoginResponseDto;
import com.onnury.member.response.MemberRegistResponseDto;
import com.onnury.query.member.MemberQueryData;
import com.onnury.query.token.JwtTokenQueryData;
import com.onnury.share.MailService;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.transaction.Transactional;
import java.util.HashMap;


@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    
    private final JwtTokenException jwtTokenException;
    private final MemberException memberException;
    private final MemberQueryData memberQueryData;
    private final JwtTokenQueryData jwtTokenQueryData;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRepository jwtTokenRepository;
    private final MailService mailService;


    // 관리자 회원 페이지 리스트업
    public MemberListUpResponseDto listUpMember(HttpServletRequest request, int page, String searchtype, String search, String startDate, String endDate, HashMap<String, String> requestParam) {
        log.info("관리자 회원 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request, requestParam);
            return null;
        }

        return memberQueryData.listUpMember(page, searchtype, search, startDate, endDate);
    }


    // 고객 회원가입 service
    public ResponseBody registMember(MemberRegistRequestDto memberRegistRequestDto) {
        log.info("고객 회원가입 service");

        HashMap<String, String> checkData = new HashMap<>();
        String checkInfo = memberException.checkRegistMemberInfo(memberRegistRequestDto);

        // 회원가입 정보 확인 후 옳바르지 않은 정보라면 예외 처리
        if (checkInfo != null) {
            log.info("회원가입 시도 정보들이 옳바르지 않습니다.");
            checkData.put("회원가입 정보", checkInfo);
            LogUtil.logError(StatusCode.CANT_REGIST_MEMBER.getMessage(), memberRegistRequestDto, checkData);
            return new ResponseBody(StatusCode.CANT_REGIST_MEMBER, checkInfo);
        }

        String checkPassword = memberException.checkRightPassword(memberRegistRequestDto.getPassword(), memberRegistRequestDto.getCheckPassword());

        // 회원가입 비밀번호가 옳바르지 않을 경우 예외 처리
        if (checkPassword != null) {
            log.info("비밀번호가 일치하지 않습니다.");
            checkData.put("비밀번호 일치 여부", checkPassword);
            LogUtil.logError(StatusCode.CANT_REGIST_MEMBER.getMessage(), memberRegistRequestDto, checkData);
            return new ResponseBody(StatusCode.CANT_REGIST_MEMBER, checkPassword);
        }

        String checkAccountExist = memberException.checkAlreadyExistAccount(memberRegistRequestDto.getLoginId());

        // 이미 존재한 계정이 있을 경우 예외 처리
        if (checkAccountExist != null) {
            log.info("이미 존재한 계정 아이디 입니다.");
            checkData.put("계정 중복 여부", checkAccountExist);
            LogUtil.logError(StatusCode.CANT_REGIST_MEMBER.getMessage(), memberRegistRequestDto, checkData);
            return new ResponseBody(StatusCode.CANT_REGIST_MEMBER, checkAccountExist);
        }

        // 회원가입 정보 저장
        Member registMember = Member.builder()
                .loginId(memberRegistRequestDto.getLoginId())
                .password(passwordEncoder.encode(memberRegistRequestDto.getPassword()))
                .userName(memberRegistRequestDto.getUserName())
                .birth(memberRegistRequestDto.getBirth())
                .postNumber(memberRegistRequestDto.getPostNumber())
                .address(memberRegistRequestDto.getAddress())
                .detailAddress(memberRegistRequestDto.getDetailAddress())
                .email(memberRegistRequestDto.getEmail())
                .phone(memberRegistRequestDto.getPhone())
                .type(memberRegistRequestDto.getType())
                .businessNumber(memberRegistRequestDto.getBusinessNumber())
                .manager(memberRegistRequestDto.getManager())
                .status("Y")
                .linkCompany(memberRegistRequestDto.getLinkCompany())
                .build();

        memberRepository.save(registMember);

        return new ResponseBody(
                StatusCode.OK,
                MemberRegistResponseDto.builder()
                        .type(registMember.getType())
                        .loginId(registMember.getLoginId())
                        .userName(registMember.getUserName())
                        .birth(registMember.getBirth())
                        .postNumber(registMember.getPostNumber())
                        .businessNumber(registMember.getBusinessNumber())
                        .address(registMember.getAddress())
                        .detailAddress(registMember.getDetailAddress())
                        .email(registMember.getEmail())
                        .phone(registMember.getPhone())
                        .manager(registMember.getManager())
                        .build());
    }


    // 고객 계정 로그인 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public MemberLoginResponseDto loginMember(HttpServletResponse response, MemberLoginRequestDto memberLoginRequestDto) {
        log.info("고객 계정 로그인 service");

        // 로그인 시도 시 해당 계정이 존재하는지 확인
        if (memberException.checkLoginAccount(memberLoginRequestDto.getLoginId(), memberLoginRequestDto.getPassword())) {
            log.info("로그인 시도 시 해당 계정이 존재하지 않습니다.");
            LogUtil.logError("로그인 시도 시 해당 계정이 존재하지 않습니다.", memberLoginRequestDto);
            return null;
        }

        // 고객 계정 호출
        Member loginMember = memberQueryData.getMember(memberLoginRequestDto.getLoginId());

        // 만약 회원탈퇴 상태인 계정의 경우 예외 처리
        if (loginMember.getStatus().equals("N")) {
            log.info("회원 탈퇴된 계정입니다.");
            LogUtil.logError("회원 탈퇴된 계정입니다.", memberLoginRequestDto);
            return null;
        }

        // 토큰을 구분할 mappingAccount 변수 저장
        String mappingAccount = loginMember.getMemberId() + ":" + loginMember.getLoginId();

        // 기존에 이전 토큰이 존재하면 삭제
        jwtTokenQueryData.deletePrevToken(mappingAccount, loginMember.getType());

        // 토큰을 발급하고 Dto 개체에 저장하는 과정
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginMember.getType() + "-" + memberLoginRequestDto.getLoginId(), memberLoginRequestDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        if (loginMember.getType().equals("C")) {

            JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(authentication, "client");

            // 발급된 토큰 정보를 토대로 Token 엔티티에 input
            JwtToken token = JwtToken.builder()
                    .grantType(jwtTokenDto.getGrantType())
                    .accessToken(jwtTokenDto.getAccessToken())
                    .refreshToken(jwtTokenDto.getRefreshToken())
                    .mappingAccount(mappingAccount)
                    .type(loginMember.getType())
                    .build();

            // 토큰 저장
            jwtTokenRepository.save(token);

            // Response Header에 액세스 토큰 리프레시 토큰, 토큰 만료일 input
            response.addHeader("Authorization", "Bearer " + token.getAccessToken());
            response.addHeader("RefreshToken", token.getRefreshToken());
            response.addHeader("AccessTokenExpireTime", jwtTokenDto.getAccessTokenExpiresIn().toString());

        } else if (loginMember.getType().equals("B")) {

            JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(authentication, "business");

            // 발급된 토큰 정보를 토대로 Token 엔티티에 input
            JwtToken token = JwtToken.builder()
                    .grantType(jwtTokenDto.getGrantType())
                    .accessToken(jwtTokenDto.getAccessToken())
                    .refreshToken(jwtTokenDto.getRefreshToken())
                    .mappingAccount(mappingAccount)
                    .type(loginMember.getType())
                    .build();

            // 토큰 저장
            jwtTokenRepository.save(token);

            // Response Header에 액세스 토큰 리프레시 토큰, 토큰 만료일 input
            response.addHeader("Authorization", "Bearer " + token.getAccessToken());
            response.addHeader("RefreshToken", token.getRefreshToken());
            response.addHeader("AccessTokenExpireTime", jwtTokenDto.getAccessTokenExpiresIn().toString());
        }

        return MemberLoginResponseDto.builder()
                .loginId(loginMember.getLoginId())
                .type(loginMember.getType())
                .userName(loginMember.getUserName())
                .build();
    }


    // 로그인 id 중복 체크 service
    public boolean checkDuplicateLoginId(String checkLoginId) {
        log.info("로그인 id 중복 체크 service");

        if (memberException.checkAlreadyExistAccount(checkLoginId) != null) {
            log.info("이미 존재한 계정 아이디이므로 다른 계정 아이디를 입력해주십시오.");
            return true;
        } else {
            log.info("가입 가능한 계정 아이디 입니다.");
            return false;
        }
    }


    // 로그인 id 찾기 service
    public String findLoginId(String email, String phone, HashMap<String, String> requestParam) {
        log.info("로그인 id 찾기 service");

        // 입력한 이메일 기준 찾고자 하는 계정 호출
        Member loginMember = memberQueryData.getLoginAccountAboutEmail(email, phone);

        // 만약 해당 이메일을 가진 계정이 존재하지 않을 경우
        if (loginMember == null) {
            log.info("해당 이메일과 연락처를 가진 계정은 존재하지 않습니다.");
            LogUtil.logError("해당 이메일과 연락처를 가진 계정은 존재하지 않습니다.", requestParam);
            return null;
        }

        StringBuilder expressId = new StringBuilder(loginMember.getLoginId().substring(0, 4));
        String notExpressId = loginMember.getLoginId().substring(4);

        for (int i = 0; i < notExpressId.length(); i++) {
            String wildCard = "*";
            expressId.append(wildCard);
        }

        return "가입하신 아이디는 " + expressId + " 입니다.";
    }


    // 비밀번호 찾기 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public String findPassword(String loginId, String email, String phone, HashMap<String, String> requestParam) {
        log.info("비밀번호 찾기 service");

        Member loginMember = memberQueryData.getLoginAccountAboutLoginIdEmailPhone(loginId, email, phone);

        // 만약 해당 이메일을 가진 계정이 존재하지 않을 경우
        if (loginMember == null) {
            log.info("해당 아이디, 이메일, 연락처를 가진 계정은 존재하지 않습니다.");
            LogUtil.logError("해당 아이디, 이메일, 연락처를 가진 계정은 존재하지 않습니다.", requestParam);
            return null;
        }

        String immediatePassword = memberQueryData.updateImmediatePassword(loginMember.getMemberId(), loginId, email, phone);

        // 메일 전송
        mailService.sendPasswordEmail(loginMember, immediatePassword);

        return "가입하신 이메일로 임시 비밀번호 발급되었습니다";
    }


    // 회원 대시보드 service
    public MemberDashboardResponseDto getDashboard(HttpServletRequest request, String startDate, String endDate, HashMap<String, String> requestParam) {
        log.info("회원 대시보드 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request, requestParam);
            return null;
        }

        return memberQueryData.getDashboard(startDate, endDate);
    }

}

