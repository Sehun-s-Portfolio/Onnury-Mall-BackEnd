package com.onnury.mapper;

import com.onnury.jwt.JwtToken;
import com.onnury.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MemberMapper {
    // 회원 조회
    Member getMemberByLoginId(String loginId) throws Exception;
}