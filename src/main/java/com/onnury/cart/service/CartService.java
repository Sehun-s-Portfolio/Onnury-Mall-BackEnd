package com.onnury.cart.service;

import com.onnury.cart.request.CartAddRequestDto;
import com.onnury.cart.response.CartAddResponseDto;
import com.onnury.cart.response.CartDataResponseDto;
import com.onnury.common.util.LogUtil;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.member.domain.Member;
import com.onnury.query.cart.CartQueryData;
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
public class CartService {

    private final JwtTokenException jwtTokenException;
    private final JwtTokenProvider jwtTokenProvider;
    private final CartQueryData cartQueryData;

    // 장바구니 담기 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public List<CartAddResponseDto> addCart(HttpServletRequest request, List<CartAddRequestDto> cartAddRequestDtoList){
        log.info("장바구니 담기 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 로그인한 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return cartQueryData.addCart(authMember, cartAddRequestDtoList);
    }


    // 장바구니 제품 삭제 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public String deleteCartProduct(HttpServletRequest request, Long cartId){
        log.info("장바구니 제품 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return "FAIL";
        }

        // 로그인한 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return cartQueryData.deleteCartProduct(authMember, cartId);
    }


    // 장바구니 리스트 호출 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public List<CartDataResponseDto> getCartList(HttpServletRequest request, int page){
        log.info("장바구니 리스트 호출 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 로그인한 고객
        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        return cartQueryData.getCartList(authMember, page);
    }
}
