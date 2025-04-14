package com.onnury.supplier.service;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.admin.repository.AdminRepository;
import com.onnury.common.util.LogUtil;
import com.onnury.configuration.AES128Config;
import com.onnury.exception.supplier.SupplierException;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.query.admin.AdminQueryData;
import com.onnury.supplier.domain.Supplier;
import com.onnury.supplier.repository.SupplierRepository;
import com.onnury.supplier.request.SupplierCreateRequestDto;
import com.onnury.supplier.request.SupplierUpdateRequestDto;
import com.onnury.supplier.response.SupplierCreateResponseDto;
import com.onnury.supplier.response.SupplierListUpResponseDto;
import com.onnury.supplier.response.SupplierUpdateResponseDto;
import com.onnury.query.supplier.SupplierQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
//import javax.transaction.Transactional;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupplierService {

    private final JwtTokenException jwtTokenException;
    private final SupplierException supplierException;
    private final SupplierRepository supplierRepository;
    private final SupplierQueryData supplierQueryData;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final AdminQueryData adminQueryData;
    private final AES128Config aes128Config;


    //공급사 생성
    public SupplierCreateResponseDto createSupplier(HttpServletRequest request, SupplierCreateRequestDto supplierInfo) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("공급사 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 생성하고자 하는 공급사의 정보가 옳바른지 확인
        if (supplierException.checkCreateSupplierInfo(supplierInfo)) {
            log.info("공급사 생성 요청 정보가 옳바르지 않음");
            LogUtil.logError("공급사 생성 요청 정보가 옳바르지 않음", request, supplierInfo);
            return null;
        }

        if (supplierInfo.getLoginId() != null) {
            // 이미 동일한 계정이 존재할 경우 예외 처리
            if(adminQueryData.checkDuplicateAdminLoginId(supplierInfo.getLoginId()) != null){
                log.info("이미 존재한 계정");
                LogUtil.logError("이미 존재한 계정", request, supplierInfo);
                return null;
            }
        }

        // 공급사 권한
        List<String> roles = new ArrayList<>();
        roles.add("supplier");

        // 공급사 계정 생성
        AdminAccount adminAccount = AdminAccount.builder()
                .loginId(supplierInfo.getLoginId() != null ? supplierInfo.getLoginId() : supplierInfo.getSupplierCompany())
                .password(supplierInfo.getPassword() != null ? aes128Config.encryptAes(supplierInfo.getPassword()) : aes128Config.encryptAes(supplierInfo.getSupplierCompany()))
                .type("supplier")
                .roles(roles)
                .build();

        adminRepository.save(adminAccount);

        //공급사 정보 저장
        Supplier supplier = Supplier.builder()
                .supplierCompany(supplierInfo.getSupplierCompany())
                .businessNumber(supplierInfo.getBusinessNumber())
                .frcNumber(supplierInfo.getFrcNumber())
                .address(supplierInfo.getAddress())
                .represent(supplierInfo.getRepresent())
                .recalladdress(supplierInfo.getRecallAddress())
                .tel(supplierInfo.getTel())
                .cscall(supplierInfo.getCsCall())
                .csInfo(supplierInfo.getCsInfo())
                .personInCharge(supplierInfo.getPersonInCharge())
                .contactCall(supplierInfo.getContactCall())
                .email(supplierInfo.getEmail())
                .status("Y")
                .onnuryCommission(supplierInfo.getOnnuryCommission() != 0.0 ? supplierInfo.getOnnuryCommission() : 0.0)
                .creditCommission(supplierInfo.getCreditCommission() != 0.0 ? supplierInfo.getCreditCommission() : 0.0)
                .adminAccountId(adminAccount.getAdminAccountId())
                .bcryptPassword(passwordEncoder.encode(supplierInfo.getPassword()))
                .build();

        supplierRepository.save(supplier);

        return SupplierCreateResponseDto.builder()
                .supplierCompany(supplier.getSupplierCompany())
                .businessNumber(supplier.getBusinessNumber())
                .frcNumber(supplierInfo.getFrcNumber())
                .address(supplier.getAddress())
                .represent(supplier.getRepresent())
                .recallAddress(supplier.getRecalladdress())
                .tel(supplier.getTel())
                .csCall(supplier.getCscall())
                .csInfo(supplier.getCsInfo())
                .personInCharge(supplier.getPersonInCharge())
                .contactCall(supplier.getContactCall())
                .email(supplier.getEmail())
                .status(supplier.getStatus())
                .onnuryCommission(supplier.getOnnuryCommission())
                .creditCommission(supplier.getCreditCommission())
                .build();
    }

    // 공급사 수정
    public SupplierUpdateResponseDto updateSupplier(HttpServletRequest request, Long supplierId, SupplierUpdateRequestDto supplierInfo) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("공급사 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }
        // 수정하고자 하는 공급사의 정보가 옳바른지 확인
        if (supplierException.checkUpdateSupplierInfo(supplierInfo)) {
            log.info("공급사 수정 요청 정보가 옳바르지 않음");
            LogUtil.logError("공급사 수정 요청 정보가 옳바르지 않음", request, supplierInfo);
            return null;
        }

        //수정한 공급사 정보 추출
        Supplier newSupplier = supplierQueryData.updateSupplier(supplierId, supplierInfo);

        return SupplierUpdateResponseDto.builder()
                .supplierCompany(newSupplier.getSupplierCompany())
                .businessNumber(newSupplier.getBusinessNumber())
                .frcNumber(newSupplier.getFrcNumber())
                .address(newSupplier.getAddress())
                .represent(newSupplier.getRepresent())
                .recallAddress(newSupplier.getRecalladdress())
                .tel(newSupplier.getTel())
                .csCall(newSupplier.getCscall())
                .csInfo(newSupplier.getCsInfo())
                .personInCharge(newSupplier.getPersonInCharge())
                .contactCall(newSupplier.getContactCall())
                .email(newSupplier.getEmail())
                .status(newSupplier.getStatus())
                .onnuryCommission(newSupplier.getOnnuryCommission())
                .creditCommission(newSupplier.getCreditCommission())
                .build();
    }

    //공급사 삭제
    @Transactional(transactionManager = "MasterTransactionManager")
    public boolean deleteSupplier(HttpServletRequest request, Long supplierid) {
        log.info("배너 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return true;
        }

        return supplierQueryData.deleteSupplier(supplierid);
    }

    // 관리자 공급사 페이지 리스트업
    public SupplierListUpResponseDto listUpSupplier(HttpServletRequest request, int page) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("관리자 공급사 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return supplierQueryData.listUpSupplier(page);
    }


    // 로그인한 공급사 id 중복 체크 service
    public boolean checkDuplicateSupplierLoginId(String checkSupplierLoginId) {
        log.info("로그인한 공급사 id 중복 체크 service");

        if (supplierException.checkExistSupplierLoginId(checkSupplierLoginId)) {
            log.info("이미 존재한 계정 아이디이므로 다른 계정 아이디를 입력해주십시오.");
            return true;
        } else {
            log.info("가입 가능한 계정 아이디 입니다.");
            return false;
        }
    }

}

