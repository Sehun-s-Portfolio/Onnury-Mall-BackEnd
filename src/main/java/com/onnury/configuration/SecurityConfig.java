package com.onnury.configuration;

import com.onnury.jwt.JwtAuthenticationFilter;
import com.onnury.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@ConditionalOnDefaultWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${active.host}")
    private String activeHost;

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("[ " + activeHost + " ] - APPLICATION ACTIVE");

        http.cors();

        http.csrf().disable()
                .exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll() // 추가
                .antMatchers(
                        "/admin/**",
                        "/api/member/regist",
                        "/api/member/login",
                        "/api/member/duplicatecheck",
                        "/api/member/find/**",
                        "/api/category/navigation",
                        "/api/category/mainlist",
                        "/api/category/page/**",
                        "/api/banner/mainlist",
                        "/api/product/**",
                        "/api/brand/**",
                        "/api/label/**",
                        "/api/product/page/**",
                        "/api/product/search/**",
                        "/api/banner/promotion/mainlist",
                        "/api/link/**",
                        "/api/supplier/duplicatecheck",
                        "/api/supplier/urgent/create/account/**",
                        "/api/notice/test/splice/notice/content/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/image/**",
                        "/page/**"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(corsConfig.corsFilter())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}