package com.onnury.admin.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.relational.core.mapping.Table
@Entity
public class AdminAccount extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long adminAccountId; // 관리자 계정 인덱스

    @Column(nullable = false)
    private String loginId; // 관리자 계정 로그인 아이디

    @Column(nullable = false)
    private String password; // 관리자 계정 비밀번호

    @Column
    private String type; // 계정 타입

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();
}
