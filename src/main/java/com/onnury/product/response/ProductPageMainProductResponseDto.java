package com.onnury.product.response;

import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.media.response.MediaResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductPageMainProductResponseDto {
    private Long supplierId; // 공급사 id
    private Long brandId;
    private String brand; // 브랜드
    private Long upCategoryId;
    private String upCategory; // 상위 카테고리
    private Long middleCategoryId;
    private String middleCategory; // 중간 카테고리
    private Long downCategoryId;
    private String downCategory;// 하위 카테고리
    private Long productId;
    private String productName; // 제품 명
    private String classificationCode;
    private List<LabelDataResponseDto> labelList; // 매핑된 라벨 리스트
    private String modelNumber; // 모델 번호
    private String deliveryType; // 배송 유형
    private String sellClassification; // 판매 구분 (전체 - A / 기업 - B / 일반 - C)
    private String expressionCheck; // 노출 여부 (Y/N)
    private int normalPrice; // 정상 가격
    private int sellPrice; // 판매 가격
    private int deliveryPrice; // 배달비
    private int purchasePrice; // 구입 가격 (매입 단가)
    private LocalDateTime eventStartDate; // 이벤트 시작 날짜
    private LocalDateTime eventEndDate; // 이벤트 끝 날짜
    private String eventDescription; // 이벤트 비고
    private String optionCheck; // 옵션 사용 여부 (Y / N)
    private List<ProductOptionCreateResponseDto> productOptionList; // 제품에 해당되는 옵션
    private String productDetailInfo; // 제품 상세 정보
    private List<MediaResponseDto> mediaList; // 제품에 연관된 이미지들
    private String manufacturer; // 제조사
    private String madeInOrigin; // 원산지
    private String consignmentStore; // 위탁점
    private String memo; // 상품 메모
    private String status; // 제품 상태
}
