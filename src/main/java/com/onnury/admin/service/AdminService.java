package com.onnury.admin.service;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.admin.response.AdminAccountLoginResponseDto;
import com.onnury.admin.response.DashBoardResponseDto;
import com.onnury.common.util.LogUtil;
import com.onnury.exception.admin.AdminException;
import com.onnury.admin.repository.AdminRepository;
import com.onnury.admin.request.AdminAccountRegisterRequestDto;
import com.onnury.admin.response.AdminAccountRegisterResponseDto;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.jwt.JwtToken;
import com.onnury.jwt.JwtTokenDto;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.jwt.JwtTokenRepository;
import com.onnury.member.domain.Member;
import com.onnury.query.admin.AdminQueryData;
import com.onnury.query.member.MemberQueryData;
import com.onnury.share.MailService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.onnury.admin.domain.QAdminAccount.adminAccount;
import static com.onnury.member.domain.QMember.member;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.supplier.domain.QSupplier.supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminException adminException;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final AdminQueryData adminQueryData;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRepository jwtTokenRepository;
    private final JwtTokenException jwtTokenException;
    private final MemberQueryData memberQueryData;
    private final JPAQueryFactory jpaQueryFactory;
    private final MailService mailService;
    private final EntityManager entityManager;

    // 관리자 계정 회원가입
    public AdminAccountRegisterResponseDto adminRegister(AdminAccountRegisterRequestDto adminAccountRegisterRequestDto){
        log.info("관리자 계정 회원가입 service");

        // 회원가입 정보 확인 후 옳바르지 않은 정보라면 예외 처리
        if(adminException.checkAdminRegisterInfo(adminAccountRegisterRequestDto)){
            log.info("회원가입 시도 정보들이 옳바르지 않음");
            LogUtil.logError("회원가입 시도 정보들이 옳바르지 않음", adminAccountRegisterRequestDto);
            return null;
        }

        // 이미 동일한 계정이 존재할 경우 예외 처리
        if(adminQueryData.checkDuplicateAdminLoginId(adminAccountRegisterRequestDto.getLoginId()) != null){
            log.info("이미 존재한 계정");
            LogUtil.logError("이미 존재한 계정", adminAccountRegisterRequestDto);
            return null;
        }

        // 관리자 권한
        List<String> roles = new ArrayList<>();
        roles.add("admin");

        // 회원가입 정보 입력
        AdminAccount adminAccount = AdminAccount.builder()
                .loginId(adminAccountRegisterRequestDto.getLoginId())
                .password(passwordEncoder.encode(adminAccountRegisterRequestDto.getPassword()))
                .type("admin")
                .roles(roles)
                .build();

        // 회원가입
        adminRepository.save(adminAccount);

        return AdminAccountRegisterResponseDto.builder()
                .loginId(adminAccount.getLoginId())
                .passwword(adminAccount.getPassword().substring(0,8))
                .build();
    }


    // 관리자 계정 로그인
    @Transactional
    public AdminAccountLoginResponseDto adminLogin(HttpServletResponse response, String loginId, String password, HashMap<String, String> requestParam) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("관리자 계정 로그인 service");

        if(adminException.checkLoginInfo(loginId, password)){
            log.info("로그인 시도 시 해당 계정 아이디를 가진 계정이 존재하지 않음");
            LogUtil.logError("로그인 시도 시 해당 계정 아이디를 가진 계정이 존재하지 않음", requestParam);
            return null;
        }

        // 관리자 계정 호출
        AdminAccount account = jpaQueryFactory
                .selectFrom(adminAccount)
                .where(adminAccount.loginId.eq(loginId))
                .fetchOne();

        if(account == null){
            log.info("관리자 계정이 존재하지 않습니다.");
            LogUtil.logError("관리자 계정이 존재하지 않습니다.", requestParam);
            return null;
        }

        String role = account.getType();

        // 토큰을 구분할 mappingAccount 변수 저장
        String mappingAccount = account.getAdminAccountId() + ":" + account.getLoginId();;

        // 토큰을 발급하고 Dto 개체에 저장하는 과정
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(account.getType().equals("supplier") ? "S-" + loginId : "A-" + loginId, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(authentication, role);

        // 발급된 토큰 정보를 토대로 Token 엔티티에 input
        JwtToken token = JwtToken.builder()
                .grantType(jwtTokenDto.getGrantType())
                .accessToken(jwtTokenDto.getAccessToken())
                .refreshToken(jwtTokenDto.getRefreshToken())
                .mappingAccount(mappingAccount)
                .type(account.getType().equals("supplier") ? "S" : "A")
                .build();

        // 토큰 저장
        jwtTokenRepository.save(token);

        // Response Header에 액세스 토큰 리프레시 토큰, 토큰 만료일 input
        response.addHeader("Authorization", "Bearer " + token.getAccessToken());
        response.addHeader("RefreshToken", token.getRefreshToken());
        response.addHeader("AccessTokenExpireTime", jwtTokenDto.getAccessTokenExpiresIn().toString());

        return AdminAccountLoginResponseDto.builder()
                .adminAccountId(account.getAdminAccountId())
                .loginId(account.getLoginId())
                .type(account.getType())
                .supplierId(account.getType().equals("admin") ? null : jpaQueryFactory
                        .select(supplier.supplierId)
                        .from(supplier)
                        .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                        .fetchOne())
                .build();
    }


    // 대시 보드 service
    public DashBoardResponseDto adminDashBoard(HttpServletRequest request, String startDate, String endDate, List<Long> brandIdList, List<Long> supplierIdList, HashMap<String, String> requestParam){
        log.info("대시 보드 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request, requestParam);
            return null;
        }

        AdminAccount account = jwtTokenProvider.getAdminAccountFromAuthentication();

        return adminQueryData.adminDashBoardCase2(account, startDate, endDate, brandIdList, supplierIdList);
    }


    // 관리자 유저 비밀번호 재설정 service
    public String adminChangeUserPassword(HttpServletRequest request, Long memberId, HashMap<String, String> requestParam){
        log.info("관리자 유저 비밀번호 재설정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request, requestParam);
            return null;
        }

        // 비밀번호를 재설정할 유저 정보 호출
        Member updateMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.memberId.eq(memberId))
                .fetchOne();

        if(updateMember != null){
            // 유저 비밀번호 재설정
            String immediatePassword = memberQueryData.updateImmediatePassword(updateMember.getMemberId(), updateMember.getLoginId(), updateMember.getEmail(), updateMember.getPhone());

            // 메일 전송
            mailService.sendPasswordEmail(updateMember, immediatePassword);

            return "비밀번호 재설정이 완료되었습니다.";
        }else{
            LogUtil.logError("비밀번호 재설정이 실패하였습니다.", request, requestParam);
            return null;
        }

    }


    @Transactional
    public boolean emergencyUpdateOrderProduct(){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        String nowDate = LocalDateTime.now().toString().replace('T', ' '); // 날짜 시간 문자열 중간에 있는 T 문자 삭제
        String[] dateSplit = nowDate.split("\\."); // 포맷 형식에 맞게끔 . 기호를 기준으로 필요한 문자열만 추출
        LocalDateTime nowDateTime = LocalDateTime.parse(dateSplit[0], formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 설치 제품들
        List<String> buildProducts = jpaQueryFactory
                .select(product.classificationCode)
                .from(product)
                .where(product.deliveryType.eq("S"))
                .fetch();

        // 배송 제품들
        List<String> deliveryProducts = jpaQueryFactory
                .select(product.classificationCode)
                .from(product)
                .where(product.deliveryType.eq("D"))
                .fetch();

        // 설치 제품 구매 이력들
        jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.cancelAmount.eq(0)
                        .and(orderInProduct.productClassificationCode.in(buildProducts))
                        .and(orderInProduct.completePurchaseCheck.eq("N"))
                        .and(orderInProduct.transportNumber.ne(""))
                )
                .fetch()
                .forEach(eachBuildOrderProduct -> {
                    String buildProductCreatedDate = eachBuildOrderProduct.getCreatedAt().toString().replace('T', ' '); // 날짜 시간 문자열 중간에 있는 T 문자 삭제
                    String[] buildProductCreatedDateSplit = buildProductCreatedDate.split("\\."); // 포맷 형식에 맞게끔 . 기호를 기준으로 필요한 문자열만 추출
                    LocalDateTime buildProductNowDateTime = LocalDateTime.parse(buildProductCreatedDateSplit[0], formatter);

                    // 설치 제품들 주문 확정 자동화 (20일 기준)
                    if (buildProductNowDateTime.isBefore(nowDateTime.minusDays(20)) ||
                            (buildProductNowDateTime.getYear() == nowDateTime.minusDays(20).getYear() &&
                                    buildProductNowDateTime.getMonthValue() == nowDateTime.minusDays(20).getMonthValue() &&
                                    buildProductNowDateTime.getDayOfMonth() == nowDateTime.minusDays(20).getDayOfMonth())) {

                        log.info("설치 제품 구매 확정 처리 진입 ");
                        log.info("주문 번호 : {}", eachBuildOrderProduct.getOrderNumber());
                        log.info("제품 코드 : {}", eachBuildOrderProduct.getProductClassificationCode());

                        jpaQueryFactory
                                .update(orderInProduct)
                                .set(orderInProduct.completePurchaseAt, LocalDateTime.now())
                                .set(orderInProduct.completePurchaseCheck, "Y")
                                .where(orderInProduct.orderInProductId.eq(eachBuildOrderProduct.getOrderInProductId()))
                                .execute();

                        log.info("설치 제품 최종 확정");
                    }
                });



        // 배송 제품 구매 이력들
        jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.cancelAmount.eq(0)
                        .and(orderInProduct.productClassificationCode.in(deliveryProducts))
                        .and(orderInProduct.completePurchaseCheck.eq("N"))
                        .and(orderInProduct.transportNumber.ne(""))
                )
                .fetch()
                .forEach(eachDeliveryOrderProduct -> {
                    String deliveryProductCreatedDate = eachDeliveryOrderProduct.getCreatedAt().toString().replace('T', ' '); // 날짜 시간 문자열 중간에 있는 T 문자 삭제
                    String[] deliveryProductCreatedDateSplit = deliveryProductCreatedDate.split("\\."); // 포맷 형식에 맞게끔 . 기호를 기준으로 필요한 문자열만 추출
                    LocalDateTime deliveryProductNowDateTime = LocalDateTime.parse(deliveryProductCreatedDateSplit[0], formatter);

                    // 배송 제품들 주문 확정 자동화 (7일 기준)
                    if (deliveryProductNowDateTime.isBefore(nowDateTime.minusDays(7)) ||
                            (deliveryProductNowDateTime.getYear() == nowDateTime.minusDays(7).getYear() &&
                                    deliveryProductNowDateTime.getMonthValue() == nowDateTime.minusDays(7).getMonthValue() &&
                                    deliveryProductNowDateTime.getDayOfMonth() == nowDateTime.minusDays(7).getDayOfMonth())) {

                        log.info("배송 제품 구매 확정 처리 진입 ");
                        log.info("주문 번호 : {}", eachDeliveryOrderProduct.getOrderNumber());
                        log.info("제품 코드 : {}", eachDeliveryOrderProduct.getProductClassificationCode());

                        jpaQueryFactory
                                .update(orderInProduct)
                                .set(orderInProduct.completePurchaseAt, LocalDateTime.now())
                                .set(orderInProduct.completePurchaseCheck, "Y")
                                .where(orderInProduct.orderInProductId.eq(eachDeliveryOrderProduct.getOrderInProductId()))
                                .execute();
                        
                        log.info("배송 제품 최종 확정");
                    }
                });


        entityManager.flush();
        entityManager.clear();


        return true;
    }

}
