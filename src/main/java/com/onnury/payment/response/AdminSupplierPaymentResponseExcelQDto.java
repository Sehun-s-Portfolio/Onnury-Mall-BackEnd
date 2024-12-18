package com.onnury.payment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
public class AdminSupplierPaymentResponseExcelQDto {

    private String orderNumber; // 주문번호
    private String linkCompany;
    private String supplierName; // 공급사명
    private Long supplierId; // 공급사 id
    private String detailOptionTitle; // 상세 옵션 타이틀
    private String seq;
    private String productClassificationCode; // 제품 구분 코드
    private String productName; // 제품 명
    private String consignmentStore;
    private int quantity; // 수량
    private String orderedAt; // 주문 일자
    private String buyMemberLoginId; // 구매자 로그인 아이디
    private String message;
    private int deliveryPrice;
    private int dangerPlacePrice;
    private String phone;
    private String receiverPhone;
    private String creditStatementNumber; // 신용/체크 카드 전표 번호
    private String onNuryStatementNumber; // 온누리 전표 번호 - tid
    private int onnurypay; // 온누리 결제 승인 금액
    private int creditpay; //
    private int productTotalAmount; // 총 결제 승인 금액
    private String transportNumber; // 운송장 번호
    private String parcelName; // 택배사
    private String receiver; // 수령자
    private String address; // 주소
    private int cancelAmount; // 온누리 결제 승인 금액
    private int creditCanclePrice; // 총 결제 승인 금액
    private int onNuryCanclePrice; // 온누리 결제 승인 금액
    private String completePurchaseAt; // 구매 확정 일자
    private String completePurchaseCheck; // 구매 확정 상태
    private double onnuryCommission;
    private double creditCommission;
    private int onnuryCommissionPrice;
    private int creditCommissionPrice;
    private String eventCheck;
    private String eventInfo;
    private int deliveryAddPrice; // 도서 산간 및 추가 배달 비용
    private String transportCheck; // 운송장 반영 확인


    @QueryProjection
    public AdminSupplierPaymentResponseExcelQDto(String orderNumber,String linkCompany, String supplierName, Long supplierId, String detailOptionTitle,
                                                 String seq,String productClassificationCode, String productName, String consignmentStore, int quantity,
                                                 String orderedAt, String buyMemberLoginId,
                                                 String message,int deliveryPrice,int dangerPlacePrice,
                                                 String phone,String receiverPhone, String creditStatementNumber,
                                                 String onNuryStatementNumber, int onnurypay, int creditpay,
                                                 int productTotalAmount,String transportNumber, String parcelName,
                                                 String receiver, String address, int cancelAmount, int creditCanclePrice, int onNuryCanclePrice,
                                                 String completePurchaseAt, String completePurchaseCheck,
                                                 double onnuryCommission, double creditCommission, int onnuryCommissionPrice, int creditCommissionPrice,
                                                 String eventCheck, String eventInfo, int deliveryAddPrice, String transportCheck) {

        this.orderNumber = orderNumber;
        this.linkCompany = linkCompany;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
        this.detailOptionTitle = detailOptionTitle;
        this.seq = seq;
        this.productClassificationCode = productClassificationCode;
        this.productName = productName;
        this.consignmentStore = consignmentStore;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
        this.buyMemberLoginId = buyMemberLoginId;
        this.message = message;
        this.deliveryPrice = deliveryPrice;
        this.dangerPlacePrice = dangerPlacePrice;
        this.phone = phone;
        this.receiverPhone = receiverPhone;
        this.creditStatementNumber = creditStatementNumber;
        this.onNuryStatementNumber = onNuryStatementNumber;
        this.onnurypay = onnurypay;
        this.productTotalAmount = productTotalAmount;
        this.transportNumber = transportNumber;
        this.parcelName = parcelName;
        this.receiver = receiver;
        this.address = address;
        this.cancelAmount = cancelAmount;
        this.creditCanclePrice = creditCanclePrice;
        this.onNuryCanclePrice = onNuryCanclePrice;
        this.creditpay = creditpay;
        this.completePurchaseAt = completePurchaseAt;
        this.completePurchaseCheck = completePurchaseCheck;
        this.onnuryCommission = onnuryCommission;
        this.creditCommission = creditCommission;
        this.onnuryCommissionPrice = onnuryCommissionPrice;
        this.creditCommissionPrice = creditCommissionPrice;
        this.eventCheck = eventCheck;
        this.eventInfo = eventInfo;
        this.deliveryAddPrice = deliveryAddPrice;
        this.transportCheck = transportCheck;
    }


    public AdminSupplierPaymentResponseExcelQDto setTransportCheck(AdminSupplierPaymentResponseExcelQDto adminSupplierPaymentResponseExcelQDto){
        this.orderNumber = adminSupplierPaymentResponseExcelQDto.getOrderNumber();
        this.linkCompany = adminSupplierPaymentResponseExcelQDto.getLinkCompany();
        this.supplierName = adminSupplierPaymentResponseExcelQDto.getSupplierName();
        this.supplierId = adminSupplierPaymentResponseExcelQDto.getSupplierId();
        this.detailOptionTitle = adminSupplierPaymentResponseExcelQDto.getDetailOptionTitle();
        this.seq = adminSupplierPaymentResponseExcelQDto.getSeq();
        this.productClassificationCode = adminSupplierPaymentResponseExcelQDto.getProductClassificationCode();
        this.productName = adminSupplierPaymentResponseExcelQDto.getProductName();
        this.consignmentStore = adminSupplierPaymentResponseExcelQDto.getConsignmentStore();
        this.quantity = adminSupplierPaymentResponseExcelQDto.getQuantity();
        this.orderedAt = adminSupplierPaymentResponseExcelQDto.getOrderedAt();
        this.buyMemberLoginId = adminSupplierPaymentResponseExcelQDto.getBuyMemberLoginId();
        this.message = adminSupplierPaymentResponseExcelQDto.getMessage();
        this.deliveryPrice = adminSupplierPaymentResponseExcelQDto.getDeliveryPrice();
        this.dangerPlacePrice = adminSupplierPaymentResponseExcelQDto.getDangerPlacePrice();
        this.phone = adminSupplierPaymentResponseExcelQDto.getPhone();
        this.receiverPhone = adminSupplierPaymentResponseExcelQDto.getReceiverPhone();
        this.creditStatementNumber = adminSupplierPaymentResponseExcelQDto.getCreditStatementNumber();
        this.onNuryStatementNumber = adminSupplierPaymentResponseExcelQDto.getOnNuryStatementNumber();
        this.onnurypay = adminSupplierPaymentResponseExcelQDto.getOnnurypay();
        this.productTotalAmount = adminSupplierPaymentResponseExcelQDto.getProductTotalAmount();
        this.transportNumber = adminSupplierPaymentResponseExcelQDto.getTransportNumber();
        this.parcelName = adminSupplierPaymentResponseExcelQDto.getParcelName();
        this.receiver = adminSupplierPaymentResponseExcelQDto.getReceiver();
        this.address = adminSupplierPaymentResponseExcelQDto.getAddress();
        this.cancelAmount = adminSupplierPaymentResponseExcelQDto.getCancelAmount();
        this.creditCanclePrice = adminSupplierPaymentResponseExcelQDto.getCreditCanclePrice();
        this.onNuryCanclePrice = adminSupplierPaymentResponseExcelQDto.getOnNuryCanclePrice();
        this.creditpay = adminSupplierPaymentResponseExcelQDto.getCreditpay();
        this.completePurchaseAt = adminSupplierPaymentResponseExcelQDto.getCompletePurchaseAt();
        this.completePurchaseCheck = adminSupplierPaymentResponseExcelQDto.getCompletePurchaseCheck();
        this.onnuryCommission = adminSupplierPaymentResponseExcelQDto.getOnnuryCommission();
        this.creditCommission = adminSupplierPaymentResponseExcelQDto.getCreditCommission();
        this.onnuryCommissionPrice = adminSupplierPaymentResponseExcelQDto.getOnnuryCommissionPrice();
        this.creditCommissionPrice = adminSupplierPaymentResponseExcelQDto.getCreditCommissionPrice();
        this.eventCheck = adminSupplierPaymentResponseExcelQDto.getEventCheck();
        this.eventInfo = adminSupplierPaymentResponseExcelQDto.getEventInfo();
        this.deliveryAddPrice = adminSupplierPaymentResponseExcelQDto.getDeliveryAddPrice();
        this.transportCheck = !adminSupplierPaymentResponseExcelQDto.getTransportNumber().isEmpty() ? "Y" : "N";

        return this;
    }
}
