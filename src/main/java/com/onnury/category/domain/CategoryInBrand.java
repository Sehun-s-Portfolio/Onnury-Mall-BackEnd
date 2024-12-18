package com.onnury.category.domain;

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
public class CategoryInBrand extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long categoryInBrandId;

    @Column(nullable = false)
    private Long brandId; // 브랜드

    @Column(nullable = false)
    private Long category1Id; // 대분류 카테고리

    @Column(nullable = false)
    private Long category2Id; // 중분류 카테고리

    @Column(nullable = false)
    private Long category3Id; // 소분류 카테고리

}
