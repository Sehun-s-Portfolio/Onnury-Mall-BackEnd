package com.onnury.exception.admin;

import com.onnury.admin.request.AdminAccountRegisterRequestDto;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public interface AdminExecptionInterface {

    // 입력한 관리자 회원가입 정보 확인
    boolean checkAdminRegisterInfo(AdminAccountRegisterRequestDto adminAccountRegisterRequestDto);

    // 입력한 관리자 로그인 아이디가 기존에 이미 존재하는지 확인
    String checkExistAdminLoginId(String loginId);

    // 로그인 시도한 관리자의 계정이 존재하지 않는지 확인
    boolean checkLoginInfo(String loginId, String password) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException;
}
