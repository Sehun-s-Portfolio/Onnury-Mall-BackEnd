package com.onnury.payment.service;


import com.onnury.admin.domain.AdminAccount;
import com.onnury.common.util.LogUtil;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.payment.request.TransportInfoRequestDto;
import com.onnury.payment.response.AdminPaymentDetailResponseDto;
import com.onnury.payment.response.AdminPaymentList3ResponseDto;
import com.onnury.payment.response.AdminPaymentListResponseDto;

import com.onnury.payment.response.AdminSupllierPaymentListResponseDto;
import com.onnury.query.payment.AdminPaymentQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
//import javax.transaction.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminPaymentService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenException jwtTokenException;
    private final AdminPaymentQueryData adminPaymentQueryData;

    // 결제 주문 리스트업 service
    public AdminPaymentListResponseDto paymentListUp(HttpServletRequest request, int page, String searchType, String searchKeyword, String startDate, String endDate) {
        log.info("관리자 공지사항 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패", request);
            return null;
        }

        AdminAccount account = jwtTokenProvider.getAdminAccountFromAuthentication();

        return adminPaymentQueryData.listup(account, page, searchType, searchKeyword, startDate, endDate);
    }

    public AdminPaymentListResponseDto paymentMemberListUp(HttpServletRequest request, int page, String searchType, String searchKeyword, String startDate, String endDate, String memberId) {
        log.info("관리자 공지사항 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패", request);
            return null;
        }

        return adminPaymentQueryData.paymentMemberListUp(page, searchType, searchKeyword, startDate, endDate, memberId);
    }


    // 특정 결제 이력 상세 조회 serivce
    public AdminPaymentDetailResponseDto paymentDetail(HttpServletRequest request, String orderNumber) {
        log.info("관리자 공지사항 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패", request);
            return null;
        }

        AdminAccount loginAdminAccount = jwtTokenProvider.getAdminAccountFromAuthentication();

        return adminPaymentQueryData.detail(loginAdminAccount, orderNumber);
    }


    // 결제 주문 취소 이력 조회 service
    public AdminPaymentList3ResponseDto paymentCancelListUp(HttpServletRequest request, int page) {
        log.info("관리자 공지사항 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패", request);
            return null;
        }

        AdminAccount account = jwtTokenProvider.getAdminAccountFromAuthentication();

        return adminPaymentQueryData.cancelListUp(account, page);
    }


    // 공급사 기준 결제 주문 리스트업 service (정산 관리)
    public AdminSupllierPaymentListResponseDto supplierPaymentListUp(HttpServletRequest request, int page, String supplierId, String searchType, String searchKeyword, String startDate, String endDate) {
        log.info("관리자 공지사항 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패", request);
            return null;
        }

        AdminAccount account = jwtTokenProvider.getAdminAccountFromAuthentication();

        return adminPaymentQueryData.supplierListUp(account, page, supplierId, searchType, searchKeyword, startDate, endDate);
    }


    // 운송장 번호 업데이트 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public String confirmTransportNumber(HttpServletRequest request, List<TransportInfoRequestDto> transportInfoRequestDto) {
        log.info("운송장 번호 업데이트 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패", request);
            return null;
        }

        return adminPaymentQueryData.confirmTransportNumber(transportInfoRequestDto);
    }

}
