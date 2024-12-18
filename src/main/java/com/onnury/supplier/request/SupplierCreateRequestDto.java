package com.onnury.supplier.request;

import lombok.Getter;

@Getter
public class SupplierCreateRequestDto {
    private String supplierCompany; // 공급사 명
    private String businessNumber; // 사업자 번호
    private String frcNumber; // 온누리 가맹 번호
    private String represent; // 대표명
    private String address; // 주소
    private String recallAddress; // 반품 주소
    private String tel; // 일반 전화
    private String csCall; // CS전화
    private String csInfo; // CS정보
    private String personInCharge; // 담당자 명
    private String contactCall; // 담당자 연락처
    private String email; // 담당자 이메일
    private String loginId; // 공급사 아이디
    private String password; // 공급사 비밀번호
    private double onnuryCommission; // 온누리 수수료
    private double creditCommission; // 신용 카드 수수료
}
