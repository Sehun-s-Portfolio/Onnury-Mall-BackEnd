package com.onnury.banner.domain;

import com.onnury.share.TimeStamped;
import io.micrometer.core.lang.Nullable;
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
public class Banner extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long bannerId;

    @Column(nullable = false)
    private String title; // 배너 명

    @Column(nullable = false)
    private String linkUrl; // 배너 클릭 시 이동 페이지 url

    @Column(nullable = false)
    private int expressionOrder; // 배너 노출 순서

    @Column(nullable = false)
    private String expressionCheck; // 배너 노출 확인

    @Column(nullable = false)
    private String startPostDate; // 배너 게시 시작 일

    @Column(nullable = false)
    private String endPostDate; // 배너 게시 마지막 일

}
