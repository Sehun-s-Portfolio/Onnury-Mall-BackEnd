package com.onnury.exception.token;

import com.onnury.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenException implements JwtTokenExceptionInterface{

    private final JwtTokenProvider jwtTokenProvider;

    // 정합성이 검증된 토큰인지 확인
    @Override
    public boolean checkAccessToken(HttpServletRequest request) {
        if(!jwtTokenProvider.validateToken(request.getHeader("RefreshToken"))){
            request.getSession().invalidate();
            return true;
        }

        return false;
    }
}
