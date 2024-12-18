package com.onnury.member.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class Member extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long memberId;

    @Column(nullable = false)
    private String loginId; // 로그인아이디

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column
    private String userName; // 회원 고객 명

    @Column
    private String birth; // 생년월일

    @Column
    private String postNumber; // 우편번호

    @Column(nullable = false)
    private String address; // 주소

    @Column(nullable = false)
    private String detailAddress; // 상세주소

    @Column(nullable = false)
    private String email; // 이메일 / 담당자 이메일

    @Column(nullable = false)
    private String phone; // 연락처 / 담당자 연락처

    @Column(nullable = false)
    private String type; // 회원유형(일반 - C, 기업 - B)

    @Column
    private String businessNumber; // 사업자번호

    @Column
    private String manager; // 담당자명

    @Column(nullable = false)
    private String status; // 계정 운영 상태

    @Column
    private String linkCompany; // 기업 링크 구분 명


    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();
}
