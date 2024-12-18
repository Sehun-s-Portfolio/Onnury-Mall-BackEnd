package com.onnury.product.request;

import lombok.Getter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
public class ProductCreateRequestDto {

    private Long supplierId; // 공급사 id

    @Min(1)
    private Long brandId; // 브랜드 id

    @Min(1)
    private Long upCategoryId; // 상위 카테고리 id

    @Min(1)
    private Long middleCategoryId; // 중간 카테고리 id

    @Min(1)
    private Long downCategoryId; // 하위 카테고리 id

    @NotBlank
    private String productName; // 제품 명

    private List<Long> labelList; // 매핑할 라벨 리스트

    @NotBlank
    private String modelNumber; // 모델 번호

    private String deliveryType; // 배송 유형

    @NotBlank
    private String sellClassification; // 판매 구분 (전체 - A / 기업 - B / 일반 - C)

    @NotBlank
    private String expressionCheck; // 노출 여부 (Y/N)

    @Min(0)
    private int normalPrice; // 정상 가격

    @Min(0)
    private int sellPrice; // 판매 가격

    @Min(0)
    private int deliveryPrice; // 배달비

    @Min(0)
    private int purchasePrice; // 구입 가격 (매입 단가)

    private int eventPrice; // 이벤트 가격

    private String eventStartDate; // 이벤트 시작 날짜

    private String eventEndDate; // 이벤트 끝 날짜

    private String eventDescription; // 이벤트 비고

    @NotBlank
    private String optionCheck; // 옵션 사용 여부 (Y / N)

    private List<ProductOptionCreateRequestDto> productOptionList;

    @NotBlank
    private String productDetailInfo; // 제품 상세 정보

    @Min(0)
    private int representImageIndex; // 대표 이미지로 설정할 이미지의 인덱스

    private List<Long> productDetailImageIds; // 제품 상세 정보에 들어갈 이미지들 id 리스트

    private String manufacturer; // 제조사

    private String madeInOrigin; // 원산지

    private String consignmentStore; // 위탁점

    private String memo; // 상품 메모
}
