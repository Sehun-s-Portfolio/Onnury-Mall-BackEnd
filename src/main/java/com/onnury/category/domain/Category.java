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
public class Category extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long categoryId;

    @Column(nullable = false)
    private int categoryGroup; // 카테고리 분류(대 - 0, 중 - 1, 소 - 2)

    @Column(nullable = false)
    private String motherCode; // 상위 카테고리 코드

    @Column(nullable = false)
    private String classficationCode; // 자코드

    @Column(nullable = false)
    private String categoryName; // 카테고리 이름

    @Column(nullable = false)
    private String imgUrl; // 카테고리 이미지 url

}
