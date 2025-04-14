package com.onnury.mapper;

import com.onnury.jwt.JwtToken;
import com.onnury.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface TokenMapper {
    // 인증 토큰 조회
    JwtToken getJwtToken() throws Exception;
}