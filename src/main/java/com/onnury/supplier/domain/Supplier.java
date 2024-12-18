package com.onnury.supplier.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class Supplier extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long supplierId;

    @Column(nullable = false)
    private String supplierCompany; // 공급사명

    @Column(nullable = false)
    private String businessNumber; // 사업자번호

    @Column
    private String frcNumber; // 온누리가맹 번호

    @Column(nullable = false)
    private String represent; // 대표명

    @Column(nullable = false)
    private String address; // 주소

    @Column
    private String recalladdress; // 반품주소

    @Column(nullable = false)
    private String tel; // 일반 전화

    @Column
    private String cscall; // CS전화

    @Column
    private String csInfo; // CS정보

    @Column(nullable = false)
    private String personInCharge; // 담당자 명

    @Column(nullable = false)
    private String contactCall; // 담당자 연락처

    @Column(nullable = false)
    private String email; // 담당자 이메일

    @Column
    private String status; // 공급사 상태

    @Column
    private double onnuryCommission; // 온누리 수수료 (%)

    @Column
    private double creditCommission; // 신용 카드 수수료 (%)

    @Column
    private Long adminAccountId; // 공급사 계정 id

    @Column
    private String bcryptPassword; // 공급사 계정 Bcrypt 암호화 비밀번호

}
