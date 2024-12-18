package com.onnury.media.domain;

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
public class Media extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long mediaId;

    @Column(nullable = false)
    private String imgUploadUrl; // 이미지 업로드 경로

    @Column(nullable = false)
    private String imgUrl; // 이미지 호출 경로

    @Column(nullable = false)
    private String imgTitle; // 이미지 원본 명

    @Column(nullable = false)
    private String imgUuidTitle; // 난수화된 이미지 명

    @Column(nullable = false)
    private String representCheck; // 대표 이미지 구분 (Y/N)

    @Column(nullable = false)
    private String type; // 파일 용도 유형

    @Column(nullable = false)
    private Long mappingContentId; // 연관된 엔티티 id
}
