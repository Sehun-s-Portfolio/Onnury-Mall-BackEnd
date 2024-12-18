package com.onnury.share;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.exception.admin.AdminExecptionInterface;
import com.onnury.exception.member.MemberExceptionInterface;
import com.onnury.member.domain.Member;
import com.onnury.query.admin.AdminQueryData;
import com.onnury.query.member.MemberQueryData;
import com.onnury.query.supplier.SupplierQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminExecptionInterface adminExecptionInterface;
    private final MemberExceptionInterface memberExceptionInterface;
    private final AdminQueryData adminQueryData;
    private final MemberQueryData memberQueryData;
    private final SupplierQueryData supplierQueryData;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        String[] loginAccount = loginId.split("-");

        if(loginAccount[0].equals("A") || loginAccount[0].equals("S")){
            String checkAdminAccount = adminExecptionInterface.checkExistAdminLoginId(loginId);

            log.info("어드민 로그인 : {}", checkAdminAccount);

            if(checkAdminAccount != null){
                log.info("어드민 계정 존재 : {}", checkAdminAccount);

                return createUserDetailsOfAdmin(adminQueryData.getAdminAccount(loginAccount[0], loginAccount[1]));
            }
        }else{
            String checkMemberAccount = memberExceptionInterface.checkExistMemberLoginId(loginId);

            log.info("유저 로그인 : {}", checkMemberAccount);

            if (checkMemberAccount.equals("client")) {
                log.info("유저 계정 존재 (고객) : {}", checkMemberAccount);
                return createUserDetailsOfMember(memberQueryData.getLoginAccount(loginId));
            } else if (checkMemberAccount.equals("business")) {
                log.info("유저 계정 존재 (기업) : {}", checkMemberAccount);
                return createUserDetailsOfMember(memberQueryData.getLoginAccount(loginId));
            }
        }

        return null;
    }

    private UserDetails createUserDetailsOfAdmin(AdminAccount adminAccount) {
        log.info("관리자용 createUserDetails 실행");

        return User.builder()
                .username(adminAccount.getLoginId())
                .password(adminAccount.getType().equals("supplier") ? supplierQueryData.getSupplier(adminAccount.getLoginId()).getBcryptPassword() : adminAccount.getPassword())
                .roles(adminAccount.getRoles().toArray(new String[0]))
                .build();
    }


    private UserDetails createUserDetailsOfMember(Member member) {
        log.info("회원용 createUserDetails 실행");

        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles(member.getRoles().toArray(new String[0]))
                .build();
    }

}