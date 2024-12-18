package com.onnury.mypage.service;

import com.onnury.exception.mypage.MyPageExceptionInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.member.domain.Member;
import com.onnury.mypage.request.ConfirmPaymentRequestDto;
import com.onnury.mypage.request.MyPageChangePasswordRequestDto;
import com.onnury.mypage.request.MyPageUpdateInfoRequestDto;
import com.onnury.mypage.request.UserCancleRequestDto;
import com.onnury.mypage.response.*;
import com.onnury.query.mypage.MyPageQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyPageService{

    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final JwtTokenProvider jwtTokenProvider;
    private final MyPageQueryData myPageQueryData;
    private final MyPageExceptionInterface myPageExceptionInterface;

    // 마이페이지 회원 정보 service
    public MyPageInfoResponseDto getMyPageInfo(HttpServletRequest request){
        log.info("마이페이지 회원 정보 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 마이페이지 정보를 조회할 로그인 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return MyPageInfoResponseDto.builder()
                .type(authMember.getType())
                .memberId(authMember.getMemberId())
                .loginId(authMember.getLoginId())
                .userName(authMember.getUserName())
                .birth(authMember.getBirth())
                .address(authMember.getAddress())
                .detailAddress(authMember.getDetailAddress())
                .postNumber(authMember.getPostNumber())
                .email(authMember.getEmail())
                .phone(authMember.getPhone())
                .businessNumber(authMember.getBusinessNumber())
                .manager(authMember.getManager())
                .build();
    }


    // 마이페이지 비밀번호 재설정 service
    @Transactional
    public String changeMyPassword(HttpServletRequest request, MyPageChangePasswordRequestDto myPageChangePasswordRequestDto){
        log.info("마이페이지 비밀번호 재설정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 마이페이지 정보를 조회할 로그인 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        if(myPageExceptionInterface.checkChangePasswordInfo(authMember, myPageChangePasswordRequestDto)){
            return null;
        }else{
            return myPageQueryData.chageMyPassword(authMember, myPageChangePasswordRequestDto);
        }
    }


    // 마이페이지 회원 탈퇴 service
    @Transactional
    public String withdrawalAccount(HttpServletRequest request){
        log.info("마이페이지 회원 탈퇴 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 탈퇴할 로그인 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.withdrawalAccount(authMember);
    }


    // 마이페이지 회원 정보 수정 service
    @Transactional
    public MyPageUpdateInfoResponseDto updateAccountInfo(
            HttpServletRequest request, MyPageUpdateInfoRequestDto myPageUpdateInfoRequestDto){
        log.info("마이페이지 회원 정보 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 탈퇴할 로그인 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.updateAccountInfo(authMember, myPageUpdateInfoRequestDto);
    }


    // 마이페이지 문의 내역 리스트 조회 service
    public TotalInquiryListResponseDto getMyInquiryList(HttpServletRequest request, int page){
        log.info("마이페이지 문의 내역 리스트 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 문의 내역을 조회할 로그인 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.getMyInquiryList(authMember, page);
    }


    // 마이페이지 자신이 작성한 문의 내용 상세 조회 service
    public MyPageInquiryDetailResponseDto getMyInquiryDetail(HttpServletRequest request, Long inquiryId){
        log.info("마이페이지 자신이 작성한 문의 내용 상세 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 문의 내역을 조회할 로그인 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.getMyInquiryDetail(authMember, inquiryId);
    }


    // 마이페이지 구매 이력 리스트 조회 service
    public JSONObject getMyPaymentList(HttpServletRequest request, int page, String startDate, String endDate) {
        log.info("마이페이지 구매 이력 리스트 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.getMyPaymentList(authMember, page, startDate, endDate);
    }

    // 마이페이지 취소 이력 리스트 조회 service
    public JSONObject getMyCancleList(HttpServletRequest request, int page, String startDate, String endDate) {
        log.info("마이페이지 취소 이력 리스트 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.getMyCancleList(authMember, page, startDate, endDate);
    }
    // 마이페이지 구매 이력 리스트 조회 service
    public JSONObject getMyCancleRequest(UserCancleRequestDto userCancleRequestDto) {
        log.info("마이페이지 구매 이력 리스트 조회 service");



        return myPageQueryData.getMyCancleRequest(userCancleRequestDto);
    }


    // 마이페이지 결제 주문 확정 service
    @Transactional
    public ConfirmPaymentResponseDto confirmMyPayment(
            HttpServletRequest request, ConfirmPaymentRequestDto confirmPaymentRequestDto){
        log.info("마이페이지 결제 주문 확정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return myPageQueryData.confirmMyPayment(authMember, confirmPaymentRequestDto);
    }
}
