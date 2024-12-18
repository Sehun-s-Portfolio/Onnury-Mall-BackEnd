package com.onnury.product.domain;

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
public class Product extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productId;

    @Column(nullable = false)
    private String productName; // 제품 명

    @Column(nullable = false)
    private String modelNumber; // 모델 번호

    @Column(nullable = false)
    private String classificationCode; // 제품 자기 구분 코드

    @Column
    private String deliveryType; // 배송 유형 (배송, 설치)

    @Column(nullable = false)
    private String sellClassification; // 판매 구분 (전체 - A / 기업 - B / 일반 - C)

    @Column(nullable = false)
    private String expressionCheck; // 노출 여부 (Y/N)

    @Column(nullable = false)
    private int normalPrice; // 정상 가격

    @Column(nullable = false)
    private int sellPrice; // 판매 가격

    @Column(nullable = false)
    private int purchasePrice; // 구입 가격

    @Column(nullable = false)
    private int eventPrice; // 이벤트 가격

    @Column
    private LocalDateTime eventStartDate; // 이벤트 시작 날짜

    @Column
    private LocalDateTime eventEndDate; // 이벤트 끝 날짜

    @Column
    private String optionCheck; // 옵션 사용 여부

    @Column
    private int deliveryPrice; // 배달비

    @Column
    private String relateImgIds; // 정렬된 연관된 제품 이미지들 id들

    @Column(columnDefinition = "LONGTEXT")
    private String eventDescription; // 이벤트 비고

    @Column(nullable = false)
    private Long supplierId; // 공급사 id

    @Column(nullable = false)
    private Long categoryInBrandId; // 제품이 속한 카테고리 + 브랜드 id

    @Column
    private String manufacturer; // 제조사

    @Column
    private String madeInOrigin; // 원산지

    @Column
    private String consignmentStore; // 위탁점

    @Column(columnDefinition = "LONGTEXT")
    private String memo; // 상품 메모

    @Column
    private String status; // 제품 상태
}
