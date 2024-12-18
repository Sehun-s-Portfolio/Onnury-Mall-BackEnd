package com.onnury.supplier.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SupplierDataResponseDto {
    private Long supplierId; // 공급사 id
    private String supplierCompany; // 공급사명
    private String businessNumber; // 사업자번호
    private String frcNumber; // 온누리가맹 번호
    private String represent; // 대표명
    private String address; // 주소
    private String recallAddress; // 반품주소
    private String tel; // 일반 전화
    private String csCall; // CS전화
    private String csInfo; // CS정보
    private String personInCharge; // 담당자 명
    private String contactCall; // 담당자 연락처
    private String email; // 담당자 이메일
    private String status; // 상태
    private double onnuryCommission; // 온누리 수수료 %
    private double creditCommission; // 신용 카드 수수료 %
    private String loginId; // 공급사 계정 아이디
    private String password; // 공급사 계정 비밀번호
}