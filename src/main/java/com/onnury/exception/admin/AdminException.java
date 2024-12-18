package com.onnury.exception.admin;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.admin.request.AdminAccountRegisterRequestDto;
import com.onnury.configuration.AES128Config;
import com.onnury.query.admin.AdminQueryData;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.onnury.admin.domain.QAdminAccount.adminAccount;
import static com.onnury.supplier.domain.QSupplier.supplier;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminException implements AdminExecptionInterface{

    private final AdminQueryData adminQueryData;
    private final JPAQueryFactory jpaQueryFactory;
    private final PasswordEncoder passwordEncoder;
    private final AES128Config aes128Config;

    // 입력한 관리자 회원가입 정보 확인
    @Override
    public boolean checkAdminRegisterInfo(AdminAccountRegisterRequestDto adminAccountRegisterRequestDto) {

        if(adminAccountRegisterRequestDto.getLoginId().isEmpty() || adminAccountRegisterRequestDto.getPassword().isEmpty()){
            return true;
        }

        return false;
    }

    // 관리자 로그인 아이디 기존에 이미 존재하는지 확인
    @Override
    public String checkExistAdminLoginId(String loginId) {
        String[] adminLoginAccount = loginId.split("-");
        AdminAccount loginAdminAccount = adminQueryData.getAdminAccount(adminLoginAccount[0], adminLoginAccount[1]);

        if(loginAdminAccount == null){
            return null;
        }else{
            if(loginAdminAccount.getType().equals("admin")){
                return "admin";
            }else if(loginAdminAccount.getType().equals("supplier")){
                return "supplier";
            }
        }

        return null;
    }

    // 로그인 시도한 관리자의 계정이 존재하지 않는지 확인
    @Override
    public boolean checkLoginInfo(String loginId, String password) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException{
        // 입력한 로그인 아이디 기준 계정 정보 조회
        AdminAccount account = jpaQueryFactory
                .selectFrom(adminAccount)
                .where(adminAccount.loginId.eq(loginId))
                .fetchOne();

        // 로그인 아이디를 가진 계정 확인
        if(account == null){
            log.info("계정이 존재하지 않음");
            return true;
        }

        if(account.getType().equals("supplier")){

            String bcryptPassword = jpaQueryFactory
                    .select(supplier.bcryptPassword)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            if(bcryptPassword != null){
                if(!passwordEncoder.matches(password, bcryptPassword)){
                    log.info("비밀번호 일치하지 않음");
                    return true;
                }else{
                    return false;
                }
            }

            if(account.getPassword().contains("bcrypt")){
                if(!passwordEncoder.matches(password, account.getPassword())){
                    log.info("비밀번호 일치하지 않음");
                    return true;
                }else{
                    return false;
                }
            }else{
                //aes128Config.init();

                if(!aes128Config.decryptAes(account.getPassword()).equals(password)){
                    log.info("비밀번호 일치하지 않음");
                    return true;
                }else{
                    return false;
                }
            }

        }else if(account.getType().equals("admin")){
            // 비밀번호가 일치하는지 확인
            if(!passwordEncoder.matches(password, account.getPassword())){
                log.info("비밀번호 일치하지 않음");
                return true;
            }
        }

        return false;
    }
}
