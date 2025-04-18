package com.onnury.jwt;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.common.util.LogUtil;
import com.onnury.mapper.AdminAccountMapper;
import com.onnury.mapper.MemberMapper;
import com.onnury.mapper.TokenMapper;
import com.onnury.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Resource;



import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
//    private final JPAQueryFactory jpaQueryFactory;

    @Resource(name = "tokenMapper")
    private TokenMapper tokenMapper;

    @Resource(name = "adminAccountMapper")
    private AdminAccountMapper adminAccountMapper;

    @Resource(name = "memberMapper")
    private MemberMapper memberMapper;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
//        this.jpaQueryFactory = queryFactory;
    }

    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public JwtTokenDto generateToken(Authentication authentication, String accountType) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        log.info("권한 정보 확인 : {}" , authorities);

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + 86400000);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", accountType)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }


    // Spring Security에 허용되고 토큰이 발급된 관리자 계정
    public AdminAccount getAdminAccountFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return null;
        }

        try {
            String accountId = ((UserDetails)authentication.getPrincipal()).getUsername();
            log.info("authentication 계정 아이디 - {}", accountId);

            //        return jpaQueryFactory
            //                .selectFrom(adminAccount)
            //                .where(adminAccount.loginId.eq(accountId))
            //                .fetchOne();

            return adminAccountMapper.getAdminAccount(accountId);
        }catch(Exception e){
            LogUtil.logException(e);
            return null;
        }
    }

    // Spring Security에 허용되고 토큰이 발급된 고객 계정
    public Member getMemberFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return null;
        }

        try{
            String loginId = ((UserDetails)authentication.getPrincipal()).getUsername();
            log.info("authentication 계정 아이디 - {}", loginId);

//            Member authMember = jpaQueryFactory
//                    .selectFrom(member)
//                    .where(member.loginId.eq(loginId))
//                    .fetchOne();
//
//            return authMember;

            return memberMapper.getMemberByLoginId(loginId);
        }catch(Exception e){
            LogUtil.logException(e);
            return null;
        }
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}