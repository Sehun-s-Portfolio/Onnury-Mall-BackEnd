package com.onnury.jwt;

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
@Entity
public class JwtToken extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long tokenId;

    @Column(nullable = false)
    private String grantType; // 권한 타입

    @Column(nullable = false)
    private String accessToken; // 액세스 토큰

    @Column(nullable = false)
    private String refreshToken; // 리프레쉬 토큰

    @Column(nullable = false)
    private String mappingAccount; // 매핑된 계정 구분 (형식 - AdminAccount id + ":" + AdminAccount loginId)

    @Column
    private String type; // jwt 발급 계정 유형 (A - 어드민, S - 공급사, C - 일반 회원, B - 기업 회원)

}
