package com.onnury.query.supplier;

import com.onnury.configuration.AES128Config;
import com.onnury.supplier.domain.Supplier;
import com.onnury.supplier.request.SupplierUpdateRequestDto;
import com.onnury.supplier.response.SupplierDataResponseDto;
import com.onnury.supplier.response.SupplierListUpResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.onnury.supplier.domain.QSupplier.supplier;
import static com.onnury.admin.domain.QAdminAccount.adminAccount;


@Slf4j
@RequiredArgsConstructor
@Component
public class SupplierQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final AES128Config aes128Config;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Supplier updateSupplier(Long supplierId, SupplierUpdateRequestDto supplierInfo) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        jpaQueryFactory
                .update(supplier)
                .set(supplier.supplierCompany, supplierInfo.getSupplierCompany())
                .set(supplier.businessNumber, supplierInfo.getBusinessNumber())
                .set(supplier.frcNumber, supplierInfo.getFrcNumber())
                .set(supplier.represent, supplierInfo.getRepresent())
                .set(supplier.address, supplierInfo.getAddress())
                .set(supplier.recalladdress, supplierInfo.getRecallAddress())
                .set(supplier.tel, supplierInfo.getTel())
                .set(supplier.cscall, supplierInfo.getCsCall())
                .set(supplier.csInfo, supplierInfo.getCsInfo())
                .set(supplier.personInCharge, supplierInfo.getPersonInCharge())
                .set(supplier.contactCall, supplierInfo.getContactCall())
                .set(supplier.email, supplierInfo.getEmail())
                .set(supplier.status, supplierInfo.getStatus())
                .set(supplier.onnuryCommission, supplierInfo.getOnnuryCommission())
                .set(supplier.creditCommission, supplierInfo.getCreditCommission())
                .set(supplier.bcryptPassword, passwordEncoder.encode(supplierInfo.getPassword()))
                .where(supplier.supplierId.eq(supplierId))
                .execute();

        entityManager.flush();
        entityManager.clear();

        Supplier updateSupplier = jpaQueryFactory
                .selectFrom(supplier)
                .where(supplier.supplierId.eq(supplierId))
                .fetchOne();

        assert updateSupplier != null;

        if(updateSupplier.getAdminAccountId() != null){
            jpaQueryFactory
                    .update(adminAccount)
                    .set(adminAccount.password, aes128Config.encryptAes(supplierInfo.getPassword()))
                    .set(adminAccount.loginId, supplierInfo.getLoginId())
                    .where(adminAccount.adminAccountId.eq(updateSupplier.getAdminAccountId()))
                    .execute();

            entityManager.flush();
            entityManager.clear();
        }

        return updateSupplier;
    }

    // 공급사 삭제
    public boolean deleteSupplier(Long supplierId) {

        jpaQueryFactory
                .update(supplier)
                .set(supplier.status, "N")
                .where(supplier.supplierId.eq(supplierId))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return false;
    }

    // 관리자 공급사 페이지 리스트업
    public SupplierListUpResponseDto listUpSupplier(int page) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Long total = 0L;
        List<Supplier> result = new ArrayList<>();
        List<SupplierDataResponseDto> supplierList = new ArrayList<>();

        total = jpaQueryFactory
                .select(supplier.count())
                .from(supplier)
                .where(supplier.status.eq("Y"))
                .fetchOne();

        result = jpaQueryFactory
                .selectFrom(supplier)
                .where(supplier.status.eq("Y"))
                .orderBy(supplier.createdAt.desc())
                .limit(10)
                .offset(paging(page))
                .fetch();

        for (Supplier eachSupplierInfo : result) {
            if(eachSupplierInfo.getAdminAccountId() != null){
                Tuple adminAccountInfo = jpaQueryFactory
                        .select(adminAccount.password, adminAccount.loginId)
                        .from(adminAccount)
                        .where(adminAccount.adminAccountId.eq(eachSupplierInfo.getAdminAccountId()))
                        .fetchOne();

                String realPassword = "";

                if(adminAccountInfo != null){
                    if(adminAccountInfo.get(adminAccount.password) != null){
                        if(!adminAccountInfo.get(adminAccount.password).contains("bcrypt")){
                            realPassword = aes128Config.decryptAes(adminAccountInfo.get(adminAccount.password));

                            supplierList.add(
                                    SupplierDataResponseDto.builder()
                                            .supplierId(eachSupplierInfo.getSupplierId())
                                            .supplierCompany(eachSupplierInfo.getSupplierCompany())
                                            .frcNumber(eachSupplierInfo.getFrcNumber())
                                            .businessNumber(eachSupplierInfo.getBusinessNumber())
                                            .represent(eachSupplierInfo.getRepresent())
                                            .address(eachSupplierInfo.getAddress())
                                            .recallAddress(eachSupplierInfo.getRecalladdress())
                                            .tel(eachSupplierInfo.getTel())
                                            .csCall(eachSupplierInfo.getCscall())
                                            .csInfo(eachSupplierInfo.getCsInfo())
                                            .personInCharge(eachSupplierInfo.getPersonInCharge())
                                            .contactCall(eachSupplierInfo.getContactCall())
                                            .email(eachSupplierInfo.getEmail())
                                            .status(eachSupplierInfo.getStatus())
                                            .onnuryCommission(eachSupplierInfo.getOnnuryCommission())
                                            .creditCommission(eachSupplierInfo.getCreditCommission())
                                            .loginId(adminAccountInfo.get(adminAccount.loginId))
                                            .password(realPassword)
                                            .build()
                            );

                        }else{
                            realPassword = "단방향 해시 암호";

                            supplierList.add(
                                    SupplierDataResponseDto.builder()
                                            .supplierId(eachSupplierInfo.getSupplierId())
                                            .supplierCompany(eachSupplierInfo.getSupplierCompany())
                                            .frcNumber(eachSupplierInfo.getFrcNumber())
                                            .businessNumber(eachSupplierInfo.getBusinessNumber())
                                            .represent(eachSupplierInfo.getRepresent())
                                            .address(eachSupplierInfo.getAddress())
                                            .recallAddress(eachSupplierInfo.getRecalladdress())
                                            .tel(eachSupplierInfo.getTel())
                                            .csCall(eachSupplierInfo.getCscall())
                                            .csInfo(eachSupplierInfo.getCsInfo())
                                            .personInCharge(eachSupplierInfo.getPersonInCharge())
                                            .contactCall(eachSupplierInfo.getContactCall())
                                            .email(eachSupplierInfo.getEmail())
                                            .status(eachSupplierInfo.getStatus())
                                            .onnuryCommission(eachSupplierInfo.getOnnuryCommission())
                                            .creditCommission(eachSupplierInfo.getCreditCommission())
                                            .loginId(adminAccountInfo.get(adminAccount.loginId))
                                            .password(realPassword)
                                            .build()
                            );
                        }
                    }
                }else{
                    supplierList.add(
                            SupplierDataResponseDto.builder()
                                    .supplierId(eachSupplierInfo.getSupplierId())
                                    .supplierCompany(eachSupplierInfo.getSupplierCompany())
                                    .frcNumber(eachSupplierInfo.getFrcNumber())
                                    .businessNumber(eachSupplierInfo.getBusinessNumber())
                                    .represent(eachSupplierInfo.getRepresent())
                                    .address(eachSupplierInfo.getAddress())
                                    .recallAddress(eachSupplierInfo.getRecalladdress())
                                    .tel(eachSupplierInfo.getTel())
                                    .csCall(eachSupplierInfo.getCscall())
                                    .csInfo(eachSupplierInfo.getCsInfo())
                                    .personInCharge(eachSupplierInfo.getPersonInCharge())
                                    .contactCall(eachSupplierInfo.getContactCall())
                                    .email(eachSupplierInfo.getEmail())
                                    .status(eachSupplierInfo.getStatus())
                                    .onnuryCommission(eachSupplierInfo.getOnnuryCommission())
                                    .creditCommission(eachSupplierInfo.getCreditCommission())
                                    .build()
                    );
                }

            }else{
                supplierList.add(
                        SupplierDataResponseDto.builder()
                                .supplierId(eachSupplierInfo.getSupplierId())
                                .supplierCompany(eachSupplierInfo.getSupplierCompany())
                                .frcNumber(eachSupplierInfo.getFrcNumber())
                                .businessNumber(eachSupplierInfo.getBusinessNumber())
                                .represent(eachSupplierInfo.getRepresent())
                                .address(eachSupplierInfo.getAddress())
                                .recallAddress(eachSupplierInfo.getRecalladdress())
                                .tel(eachSupplierInfo.getTel())
                                .csCall(eachSupplierInfo.getCscall())
                                .csInfo(eachSupplierInfo.getCsInfo())
                                .personInCharge(eachSupplierInfo.getPersonInCharge())
                                .contactCall(eachSupplierInfo.getContactCall())
                                .email(eachSupplierInfo.getEmail())
                                .status(eachSupplierInfo.getStatus())
                                .onnuryCommission(eachSupplierInfo.getOnnuryCommission())
                                .creditCommission(eachSupplierInfo.getCreditCommission())
                                .build()
                );
            }
        }

        return SupplierListUpResponseDto.builder()
                .supplierDataResponseDto(supplierList)
                .total(total)
                .build();
    }

    // page 계산
    private int paging(int page) {
        if (page > 0) {
            // 검색 요청 키워드에서 텍스트 서칭이 가능하도록 키워드에 % 기호 적용
            return (page - 1) * 10;
        }
        return 0;
    }

    public Supplier getSupplier(String supplierLoginId) {

        Long existAdminAccountIndex = jpaQueryFactory
                .select(adminAccount.adminAccountId)
                .from(adminAccount)
                .where(adminAccount.loginId.eq(supplierLoginId))
                .fetchOne();

        if(existAdminAccountIndex != null){
            return jpaQueryFactory
                    .selectFrom(supplier)
                    .where(supplier.adminAccountId.eq(existAdminAccountIndex))
                    .fetchOne();
        }else{
            return null;
        }

    }

}