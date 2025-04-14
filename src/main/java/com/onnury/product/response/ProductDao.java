package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class ProductDao {
    private Long productId;
    private String productName; // 제품 명
    private String modelNumber; // 모델 번호
    private String classificationCode; // 제품 자기 구분 코드
    private String deliveryType; // 배송 유형 (배송, 설치)
    private String sellClassification; // 판매 구분 (전체 - A / 기업 - B / 일반 - C)
    private String expressionCheck; // 노출 여부 (Y/N)
    private int normalPrice; // 정상 가격
    private int sellPrice; // 판매 가격
    private int purchasePrice; // 구입 가격
    private int eventPrice; // 이벤트 가격
    private LocalDateTime eventStartDate; // 이벤트 시작 날짜
    private LocalDateTime eventEndDate; // 이벤트 끝 날짜
    private String optionCheck; // 옵션 사용 여부
    private int deliveryPrice; // 배달비
    private String relateImgIds; // 정렬된 연관된 제품 이미지들 id들
    private String eventDescription; // 이벤트 비고
    private Long supplierId; // 공급사 id
    private Long categoryInBrandId; // 제품이 속한 카테고리 + 브랜드 id
    private String manufacturer; // 제조사
    private String madeInOrigin; // 원산지
    private String consignmentStore; // 위탁점
    private String memo; // 상품 메모
    private String status; // 제품 상태
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
