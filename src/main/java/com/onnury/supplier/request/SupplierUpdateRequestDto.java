package com.onnury.supplier.request;

import lombok.Getter;

@Getter
public class SupplierUpdateRequestDto {
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
    private double onnuryCommission; // 수정할 온누리 수수료 %
    private double creditCommission; // 수정할 신용 카드 수수료 %
    private String loginId; // 수정할 공급사 id
    private String password; // 수정할 공급사 비밀번호
}
