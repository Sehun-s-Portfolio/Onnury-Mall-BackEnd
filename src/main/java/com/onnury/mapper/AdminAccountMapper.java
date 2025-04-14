package com.onnury.mapper;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.jwt.JwtToken;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AdminAccountMapper {
    // 관리자 계정 조회
    AdminAccount getAdminAccount(String accountId) throws Exception;
}