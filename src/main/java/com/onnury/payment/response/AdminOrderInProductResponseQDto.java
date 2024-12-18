package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
public class AdminOrderInProductResponseQDto {

    private String orderNumber; // 주문번호
    private String linkCompany;
    private String supplierName; // 공급사명
    private Long supplierId; // 공급사 id
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private int quantity; // 수량
    private String orderedAt; // 주문 일자
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private int creditApprovalPrice; // 신용/체크 카드 결제 승인 금액
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private int onNuryApprovalPrice; // 온누리 결제 승인 금액
    private int creditCanclePrice; // 온누리 결제 승인 금액
    private int onNuryCanclePrice; // 온누리 결제 승인 금액
    private double onnuryCommission;
    private double creditCommission;
    private int onnuryCommissionPrice;
    private int creditCommissionPrice;
    private String eventCheck;
    private String eventInfo;
    private int deliveryAddPrice; // 도서 산간 및 추가 배달 비용
    private String completePurchaseAt; // 구매 확정 일자
    private String completePurchaseCheck; // 구매 확정 상태
    private String parcelName; // 택배사 명
    private String transportNumber; // 택배 번호
    private String transportCheck; // 운송장 반영 확인

    @QueryProjection
    public AdminOrderInProductResponseQDto(String orderNumber, String linkCompany, String supplierName, Long supplierId, String detailOptionTitle, String productClassificationCode,
                                           String productName, int quantity, String orderedAt, String creditStatementNumber,
                                           int creditApprovalPrice, String onNuryStatementNumber, int onNuryApprovalPrice, int creditCanclePrice, int onNuryCanclePrice,
                                           double onnuryCommission, double creditCommission, int onnuryCommissionPrice, int creditCommissionPrice, String eventCheck, String eventInfo,
                                           int deliveryAddPrice, String completePurchaseAt, String completePurchaseCheck, String parcelName, String transportNumber, String transportCheck) {
        this.orderNumber = orderNumber;
        this.linkCompany = linkCompany;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
        this.detailOptionTitle = detailOptionTitle;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.creditStatementNumber = creditStatementNumber;
        this.creditApprovalPrice = creditApprovalPrice;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.onNuryApprovalPrice = onNuryApprovalPrice;
        this.creditCanclePrice = creditCanclePrice;
        this.onNuryCanclePrice = onNuryCanclePrice;
        this.onnuryCommission = onnuryCommission;
        this.creditCommission = creditCommission;
        this.onnuryCommissionPrice = onnuryCommissionPrice;
        this.creditCommissionPrice = creditCommissionPrice;
        this.eventCheck = eventCheck;
        this.eventInfo = eventInfo;
        this.deliveryAddPrice = deliveryAddPrice;
        this.completePurchaseAt = completePurchaseAt;
        this.completePurchaseCheck = completePurchaseCheck;
        this.parcelName = parcelName;
        this.transportNumber = transportNumber;
        this.transportCheck = transportCheck;
    }


    public AdminOrderInProductResponseQDto setTransportCheck(AdminOrderInProductResponseQDto adminOrderInProductResponseQDto){
        this.orderNumber = adminOrderInProductResponseQDto.getOrderNumber();
        this.linkCompany = adminOrderInProductResponseQDto.getLinkCompany();
        this.supplierName = adminOrderInProductResponseQDto.getSupplierName();
        this.supplierId = adminOrderInProductResponseQDto.getSupplierId();
        this.detailOptionTitle = adminOrderInProductResponseQDto.getDetailOptionTitle();
        this.productClassificationCode = adminOrderInProductResponseQDto.getProductClassificationCode();
        this.productName = adminOrderInProductResponseQDto.getProductName();
        this.quantity = adminOrderInProductResponseQDto.getQuantity();
        this.orderedAt = adminOrderInProductResponseQDto.getOrderedAt();
        this.creditStatementNumber = adminOrderInProductResponseQDto.getCreditStatementNumber();
        this.creditApprovalPrice = adminOrderInProductResponseQDto.getCreditApprovalPrice();
        this.onNuryStatementNumber = adminOrderInProductResponseQDto.getOnNuryStatementNumber();
        this.onNuryApprovalPrice = adminOrderInProductResponseQDto.getOnNuryApprovalPrice();
        this.creditCanclePrice = adminOrderInProductResponseQDto.getCreditCanclePrice();
        this.onNuryCanclePrice = adminOrderInProductResponseQDto.getOnNuryCanclePrice();
        this.onnuryCommission = adminOrderInProductResponseQDto.getOnnuryCommission();
        this.creditCommission = adminOrderInProductResponseQDto.getCreditCommission();
        this.onnuryCommissionPrice = adminOrderInProductResponseQDto.getOnnuryCommissionPrice();
        this.creditCommissionPrice = adminOrderInProductResponseQDto.getCreditCommissionPrice();
        this.eventCheck = adminOrderInProductResponseQDto.getEventCheck();
        this.eventInfo = adminOrderInProductResponseQDto.getEventInfo();
        this.deliveryAddPrice = adminOrderInProductResponseQDto.getDeliveryAddPrice();
        this.completePurchaseAt = adminOrderInProductResponseQDto.getCompletePurchaseAt();
        this.completePurchaseCheck = adminOrderInProductResponseQDto.getCompletePurchaseCheck();
        this.parcelName = adminOrderInProductResponseQDto.getParcelName();
        this.transportNumber = adminOrderInProductResponseQDto.getTransportNumber();
        this.transportCheck = !adminOrderInProductResponseQDto.getTransportNumber().isEmpty() ? "Y" : "N";

        return this;
    }
}
