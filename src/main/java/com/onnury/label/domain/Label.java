package com.onnury.label.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class Label extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long labelId;

    @Column(nullable = false)
    private String labelTitle; // 라벨 명

    @Column(nullable = false)
    private String colorCode; // 라벨컬러코드

    @Column(nullable = false)
    private LocalDateTime startPostDate; // 라벨 게시일

    @Column(nullable = false)
    private LocalDateTime endPostDate; // 라벨 종료일

    @Column(nullable = false)
    private String imgUrl; // 라벨 이미지 호출 경로

    @Column(nullable = false)
    private String topExpression; // 상위 노출 (Y / N)

}
