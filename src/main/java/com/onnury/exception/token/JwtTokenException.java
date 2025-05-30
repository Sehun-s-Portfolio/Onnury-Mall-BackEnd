package com.onnury.exception.token;

import com.onnury.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
        log.info("토큰 정합성 검증 Exception 인터페이스 진입");
        
        if(!jwtTokenProvider.validateToken(request.getHeader("RefreshToken"))){
            log.info("정합성이 옳바르지 않은 JWT 토큰");
            request.getSession().invalidate();
            return true;
        }
        
        log.info("정합성이 옳바른 JWT 토큰");
        return false;
    }
}
