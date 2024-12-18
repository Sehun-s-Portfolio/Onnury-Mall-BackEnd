package com.onnury.product.request;

import lombok.Getter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
public class ProductUpdateRequestDto {
    @Min(1)
    private Long productId; // 수정할 제품 id

    @Min(1)
    private Long supplierId; // 공급사 id - O

    @Min(1)
    private Long brandId; // 브랜드 id  - O

    @Min(1)
    private Long upCategoryId; // 상위 카테고리 id - O

    @Min(1)
    private Long middleCategoryId; // 중간 카테고리 id - O

    @Min(1)
    private Long downCategoryId; // 하위 카테고리 id - O

    @NotBlank
    private String productName; // 제품 명 - O

    private List<Long> labelList; // 매핑할 라벨 리스트 - O

    @NotBlank
    private String modelNumber; // 모델 번호  - O

    private String deliveryType; // 배송 유형

    @NotBlank
    private String sellClassification; // 판매 구분 (전체 - A / 기업 - B / 일반 - C)  - O

    @NotBlank
    private String expressionCheck; // 노출 여부 (Y/N)

    @Min(0)
    private int normalPrice; // 정상 가격  - O

    @Min(0)
    private int sellPrice; // 판매 가격  - O

    @Min(0)
    private int deliveryPrice; // 배달비  - O

    @Min(0)
    private int purchasePrice; // 구입 가격 (매입 단가)  - O

    private int eventPrice; // 이벤트 가격 - O

    private String eventStartDate; // 이벤트 시작 날짜 - O

    private String eventEndDate; // 이벤트 끝 날짜 - O

    private String eventDescription; // 이벤트 비고 - O

    @NotBlank
    private String optionCheck; // 옵션 사용 여부 (Y / N) - O

    private List<ProductOptionUpdateRequestDto> productOptionList; // 옵션 리스트 - O

    private String productDetailInfo; // 제품 상세 정보 - O
    private List<Long> remainMediaIdList; // 기존 유지 이미지들의 id를 정렬된 순서로 요청받는 리스트
    private List<Long> deleteImageIds; // 삭제할 기존 이미지
    private List<Integer> addImgIndexList; // 새롭게 추가되는 이미지들이 위치할 리스트의 인덱스 리스트

    @Min(0)
    private int representImageIndex; // 대표 이미지로 설정할 이미지의 인덱스

    private List<Long> productDetailImageIds; // 제품 상세 정보에 들어갈 이미지들 id 리스트

    private String manufacturer; // 제조사

    private String madeInOrigin; // 원산지

    private String consignmentStore; // 위탁점

    private String memo; // 상품 메모

    private String status; // 상품 상태
}
