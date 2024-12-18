package com.onnury.exception.token;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public interface JwtTokenExceptionInterface {

    // 정합성이 검증된 토큰인지 확인
    boolean checkAccessToken(HttpServletRequest request);
}
