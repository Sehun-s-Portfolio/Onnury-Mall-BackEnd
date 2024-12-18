package com.onnury.query.payment;

import com.onnury.member.domain.Member;
import com.onnury.payment.domain.*;
import com.onnury.payment.repository.*;
import com.onnury.payment.response.CancelPaymentResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.onnury.payment.domain.QPaymentApproval.paymentApproval;
import static com.onnury.payment.domain.QProductOrder.productOrder;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.payment.domain.QProductOrderOfOrderInProduct.productOrderOfOrderInProduct;
import static com.onnury.payment.domain.QPayment.payment;
import static com.onnury.payment.domain.QOrderInDeliveryAddPrice.orderInDeliveryAddPrice;
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final ProductOrderRepository productOrderRepository;
    private final OrderInProductRepository orderInProductRepository;
    private final ProductOrderOfOrderInProductRepository productOrderOfOrderInProductRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentApprovalRepository paymentApprovalRepository;
    private final PaymentZppReqRepository paymentZppReqRepository;


    // 온누리 결제 승인 정보 조회
    public PaymentApproval getOnnuryPaymentApprovalInfo(String merchantOrderID) {
        return jpaQueryFactory
                .selectFrom(paymentApproval)
                .where(paymentApproval.merchantOrderID.eq(merchantOrderID))
                .fetchOne();
    }


    // 온누리 결제된 상품 정보들 조회
//    public boolean getOnnuryOrderInProducts(String merchantOrderID, List<JSONObject> cancelProducts) {
//        // 주문 번호와 일치한 제품 주문 이력 조회
//        Long productOrderId = jpaQueryFactory
//                .select(productOrder.proudctOrderId)
//                .from(productOrder)
//                .where(productOrder.orderNumber.eq(merchantOrderID))
//                .fetchOne();
//
//        // 결제 취소 요청받은 제품들의 seq 일련번호 추출
//        List<String> seqList = cancelProducts.stream()
//                .map(eachRequestCancelProduct -> (String) eachRequestCancelProduct.get("seq"))
//                .collect(Collectors.toList());
//
//        // 결제 취소 요청받은 제품들의 seq들을 기준으로 이전 주문 제품들 정보들을 담을 리스트 생성
//        List<OrderInProduct> cancelOrderInProducts = new ArrayList<>();
//
//        // 결제 취소 요청받은 제품들의 seq들을 기준으로 이전 주문 제품들 정보들 호출
//        jpaQueryFactory
//                .select(productOrderOfOrderInProduct.orderInProductId)
//                .from(productOrderOfOrderInProduct)
//                .where(productOrderOfOrderInProduct.productOrderId.eq(productOrderId))
//                .fetch()
//                .forEach(eachOrderInProductId -> {
//                    // 기존에 결제한 주문 제품 정보 호출
//                    OrderInProduct eachOrderProduct = jpaQueryFactory
//                            .selectFrom(orderInProduct)
//                            .where(orderInProduct.orderInProductId.eq(eachOrderInProductId))
//                            .fetchOne();
//
//                    // 기존에 결제한 주문 제품이 계속 존재할 경우 진입
//                    if (eachOrderProduct != null) {
//                        // 결제 취소 요청받은 제품들의 seq 일련번호들 중 기존에 결제한 주문 제품의 seq와 일치하는 제품들만 리스트에 저장
//                        for (String requestSeq : seqList) {
//                            if (eachOrderProduct.getSeq().equals(requestSeq)) {
//                                cancelOrderInProducts.add(eachOrderProduct);
//                            }
//                        }
//                    }
//                });
//
//        // seq가 일치하는 제품 리스트가 하나 이상의 제품을 가지고 있을 경우 진입
//        if (!cancelOrderInProducts.isEmpty()) {
//            // 리스트에 속한 제품들의 결제 취소 요청 금액과 이전 결제 완료 금액을 비교
//            for (int i = 0; i < cancelOrderInProducts.size(); i++) {
//                int index = i;
//
//                // 리스트에 속한 제품들의 결제 취소 요청 금액과 이전 결제 완료 금액을 비교 하여 틀릴 경우 true 반환
//                if (cancelProducts.stream()
//                        .filter(matchProduct -> matchProduct.get("seq").equals(cancelOrderInProducts.get(index).getSeq()))
//                        .noneMatch(matchProduct -> ((Integer) matchProduct.get("cancelAmount")) == cancelOrderInProducts.get(index).getTotalPrice())) {
//                    return true;
//                }
//            }
//
//            return false;
//
//        } else { // seq가 일치하는 제품이 존재하지 않으므로 결제 취소 불가
//            return true;
//        }
//    }
//
//
//    @Transactional
//    public void updatePaymentInfo(JSONObject easyPayJsonObj) {
//        log.info("Payment 정보 업데이트");
//
//        jpaQueryFactory
//                .update(payment)
//                .set(payment.creditStatementNumber, (String) easyPayJsonObj.get("pgCno"))
//                .set(payment.creditApprovalPrice, ((Long) easyPayJsonObj.get("amount")).intValue())
//                .set(payment.totalApprovalPrice, ((Long) easyPayJsonObj.get("amount")).intValue())
//                .where(payment.orderNumber.eq((String) easyPayJsonObj.get("shopOrderNo")))
//                .execute();
//
//        entityManager.flush();
//        entityManager.clear();
//    }
//
//
//    // 온누리 결제 취소 시 취소 데이터 저장
//    public void cancelPaymentUpdate(Member authMember, Map<String, Object> bizresultEVJsonData, List<HashMap<String, Object>> cancelProducts) {
//
//        // 취소 시점 날짜 데이터 컨버팅
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
//        String convertCancelDateTime = LocalDateTime.now().toString().substring(0, 19).replace("T", " ");
//        LocalDateTime cancelDateTime = LocalDateTime.parse(convertCancelDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
//
//        // 이전 제품 주문 이력 정보 조회
//        ProductOrder beforeProductOrder = jpaQueryFactory
//                .selectFrom(productOrder)
//                .where(productOrder.orderNumber.eq((String) bizresultEVJsonData.get("merchantOrderID")))
//                .fetchOne();
//
//        List<OrderInProduct> cancelOrderInProduct = new ArrayList<>();
//
//        // 취소 요청 제품들 조회하여 취소 이력 저장 처리
//        cancelProducts.forEach(eachCancelProduct -> {
//            // 취소하고자 하는 제품의 취소 금액이 정상적으로 요청되어 들어왔을 경우,
//            if ((Integer) eachCancelProduct.get("cancelAmount") != 0) {
//
//                // 이전 주문 결제 정보에 해당되는 주문 제품 정보 조회 후 취소 이력 저장 처리
//                jpaQueryFactory
//                        .select(productOrderOfOrderInProduct.orderInProductId)
//                        .from(productOrderOfOrderInProduct)
//                        .where(productOrderOfOrderInProduct.productOrderId.eq(beforeProductOrder.getProudctOrderId()))
//                        .fetch()
//                        .forEach(eachOrderInProductId -> {
//
//                            // 이전 주문 결제에 해당되는 제품 정보 호출
//                            OrderInProduct cancelProduct = jpaQueryFactory
//                                    .selectFrom(orderInProduct)
//                                    .where(orderInProduct.orderInProductId.eq(eachOrderInProductId)
//                                            .and(orderInProduct.seq.eq((String) eachCancelProduct.get("seq"))))
//                                    .fetchOne();
//
//                            // 제품 정보가 존재할 경우 진입
//                            if (cancelProduct != null) {
//                                // 취소 저장 리스트에 해당 제품의 취소 정보를 반영한 데이터를 저장
//                                cancelOrderInProduct.add(
//                                        OrderInProduct.builder()
//                                                .seq(cancelProduct.getSeq())
//                                                .brandId(cancelProduct.getBrandId())
//                                                .upCategoryId(cancelProduct.getUpCategoryId())
//                                                .middleCategoryId(cancelProduct.getMiddleCategoryId())
//                                                .downCategoryId(cancelProduct.getDownCategoryId())
//                                                .supplierId(cancelProduct.getSupplierId())
//                                                .supplierCode(cancelProduct.getSupplierCode())
//                                                .detailOptionTitle(cancelProduct.getDetailOptionTitle())
//                                                .productId(cancelProduct.getProductId())
//                                                .productClassificationCode(cancelProduct.getProductClassificationCode())
//                                                .productName(cancelProduct.getProductName())
//                                                .quantity(cancelProduct.getQuantity())
//                                                .useOnnuryPayAmount(-cancelProduct.getUseOnnuryPayAmount())
//                                                .completePurchaseCheck(cancelProduct.getCompletePurchaseCheck())
//                                                .completePurchaseAt(cancelProduct.getCompletePurchaseAt())
//                                                .transportNumber(cancelProduct.getTransportNumber())
//                                                .parcelName(cancelProduct.getParcelName())
//                                                .cancelCheck("Y")
//                                                .cancelRequestAt(cancelDateTime)
//                                                .cancelAt(null)
//                                                .deliveryPrice(-cancelProduct.getDeliveryPrice())
//                                                .dangerPlacePrice(-cancelProduct.getDangerPlacePrice())
//                                                .totalPrice(-cancelProduct.getTotalPrice())
//                                                .purchasePrice(cancelProduct.getPurchasePrice())
//                                                .build()
//                                );
//                            }
//                        });
//            }
//        });
//
//        // 취소 이력이 저장된 이후 가지고 있을 리스트 생성
//        List<OrderInProduct> cancelOrderInProducts = new ArrayList<>();
//
//        // 만약 취소 요청 제품 리스트가 한 제품이라도 존재할 경우,
//        if (!cancelOrderInProduct.isEmpty()) {
//            // 취소 이력 일괄 저장 후 임시 저장 리스트에 고정
//            cancelOrderInProducts = orderInProductRepository.saveAll(cancelOrderInProduct);
//        }
//
//        // 결제 주문 정보와 해당 주문 제품 매핑 정보를 담을 리스트 생성
//        List<ProductOrderOfOrderInProduct> relatedCancelProductOrderOfOrderInProductList = new ArrayList<>();
//
//        // 주문 취소 제품들을 기준으로 취소 주문 제품과 주문 정보를 매핑하여 리스트에 저장
//        cancelOrderInProducts.forEach(eachCancelProductOrderOfOrderInProduct -> {
//            relatedCancelProductOrderOfOrderInProductList.add(
//                    ProductOrderOfOrderInProduct.builder()
//                            .orderInProductId(eachCancelProductOrderOfOrderInProduct.getOrderInProductId())
//                            .productOrderId(beforeProductOrder.getProudctOrderId())
//                            .memberId(authMember.getMemberId())
//                            .build());
//        });
//
//        // 결제 주문 정보와 해당 주문 제품 매핑 정보 일괄 저장
//        productOrderOfOrderInProductRepository.saveAll(relatedCancelProductOrderOfOrderInProductList);
//
//        // 이전 결제 주문 정보 호출
//        Payment beforePayment = jpaQueryFactory
//                .selectFrom(payment)
//                .where(payment.orderNumber.eq((String) bizresultEVJsonData.get("merchantOrderID")))
//                .fetchOne();
//
//        // 이전 결제 주문 정보를 기준으로 취소 정보를 반영하여 저장
//        Payment cancelPayment = Payment.builder()
//                .orderNumber(beforePayment.getOrderNumber())
//                .orderCancelNumber((String) bizresultEVJsonData.get("merchantCancelID"))
//                .cancelAt(cancelDateTime)
//                .onNuryStatementNumber(beforePayment.getOnNuryStatementNumber())
//                .creditStatementNumber(beforePayment.getCreditStatementNumber())
//                .onNuryApprovalPrice(-beforePayment.getOnNuryApprovalPrice())
//                .creditApprovalPrice(-beforePayment.getCreditApprovalPrice())
//                .totalApprovalPrice(-beforePayment.getTotalApprovalPrice())
//                .status("N")
//                .build();
//
//        // 주문 취소 정보 저장
//        paymentRepository.save(cancelPayment);
//    }
//

    /**
    // EasyPay 결제 취소 시 취소 데이터 저장
    public void cancelEasyPaymentUpdate(Member authMember, JSONObject cancelData) {

        // 취소 시점 날짜 데이터 컨버팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        String convertCancelDateTime = LocalDateTime.now().toString().substring(0, 19).replace("T", " ");
        LocalDateTime cancelDateTime = LocalDateTime.parse(convertCancelDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 이전 제품 주문 이력 정보 조회
        ProductOrder beforeProductOrder = jpaQueryFactory
                .selectFrom(productOrder)
                .where(productOrder.orderNumber.eq((String) cancelData.get("shopOrderNo")))
                .fetchOne();

        List<OrderInProduct> cancelOrderInProduct = new ArrayList<>();

        // 이전 주문 결제 정보에 해당되는 주문 제품 정보 조회 후 취소 이력 저장 처리
        jpaQueryFactory
                .select(productOrderOfOrderInProduct.orderInProductId)
                .from(productOrderOfOrderInProduct)
                .where(productOrderOfOrderInProduct.productOrderId.eq(beforeProductOrder.getProudctOrderId()))
                .fetch()
                .forEach(eachOrderInProductId -> {

                    // 이전 주문 결제에 해당되는 제품 정보 호출
                    OrderInProduct cancelProduct = jpaQueryFactory
                            .selectFrom(orderInProduct)
                            .where(orderInProduct.orderInProductId.eq(eachOrderInProductId))
                            .fetchOne();

                    // 제품 정보가 존재할 경우 진입
                    if (cancelProduct != null) {
                        // 취소 저장 리스트에 해당 제품의 취소 정보를 반영한 데이터를 저장
                        cancelOrderInProduct.add(
                                OrderInProduct.builder()
                                        .seq(cancelProduct.getSeq())
                                        .brandId(cancelProduct.getBrandId())
                                        .upCategoryId(cancelProduct.getUpCategoryId())
                                        .middleCategoryId(cancelProduct.getMiddleCategoryId())
                                        .downCategoryId(cancelProduct.getDownCategoryId())
                                        .supplierId(cancelProduct.getSupplierId())
                                        .supplierCode(cancelProduct.getSupplierCode())
                                        .detailOptionTitle(cancelProduct.getDetailOptionTitle())
                                        .productId(cancelProduct.getProductId())
                                        .productClassificationCode(cancelProduct.getProductClassificationCode())
                                        .productName(cancelProduct.getProductName())
                                        .quantity(cancelProduct.getQuantity())
                                        .useOnnuryPayAmount(-cancelProduct.getUseOnnuryPayAmount())
                                        .completePurchaseCheck(cancelProduct.getCompletePurchaseCheck())
                                        .completePurchaseAt(cancelProduct.getCompletePurchaseAt())
                                        .transportNumber(cancelProduct.getTransportNumber())
                                        .parcelName(cancelProduct.getParcelName())
                                        .cancelCheck("Y")
                                        .cancelRequestAt(cancelDateTime)
                                        .cancelAt(null)
                                        .deliveryPrice(-cancelProduct.getDeliveryPrice())
                                        .dangerPlacePrice(-cancelProduct.getDangerPlacePrice())
                                        .totalPrice(-cancelProduct.getTotalPrice())
                                        .purchasePrice(cancelProduct.getPurchasePrice())
                                        .build()
                        );
                    }
                });

        // 취소 이력이 저장된 이후 가지고 있을 리스트 생성
        List<OrderInProduct> cancelOrderInProducts = new ArrayList<>();

        // 만약 취소 요청 제품 리스트가 한 제품이라도 존재할 경우,
        if (!cancelOrderInProduct.isEmpty()) {
            // 취소 이력 일괄 저장 후 임시 저장 리스트에 고정
            cancelOrderInProducts = orderInProductRepository.saveAll(cancelOrderInProduct);
        }

        // 결제 주문 정보와 해당 주문 제품 매핑 정보를 담을 리스트 생성
        List<ProductOrderOfOrderInProduct> relatedCancelProductOrderOfOrderInProductList = new ArrayList<>();

        // 주문 취소 제품들을 기준으로 취소 주문 제품과 주문 정보를 매핑하여 리스트에 저장
        cancelOrderInProducts.forEach(eachCancelProductOrderOfOrderInProduct -> {
            relatedCancelProductOrderOfOrderInProductList.add(
                    ProductOrderOfOrderInProduct.builder()
                            .orderInProductId(eachCancelProductOrderOfOrderInProduct.getOrderInProductId())
                            .productOrderId(beforeProductOrder.getProudctOrderId())
                            .memberId(authMember.getMemberId())
                            .build());
        });

        // 결제 주문 정보와 해당 주문 제품 매핑 정보 일괄 저장
        productOrderOfOrderInProductRepository.saveAll(relatedCancelProductOrderOfOrderInProductList);

        // 이전 결제 주문 정보 호출
        Payment beforePayment = jpaQueryFactory
                .selectFrom(payment)
                .where(payment.orderNumber.eq((String) cancelData.get("merchantOrderID")))
                .fetchOne();

        // 이전 결제 주문 정보를 기준으로 취소 정보를 반영하여 저장
        Payment cancelPayment = Payment.builder()
                .orderNumber(beforePayment.getOrderNumber())
                .orderCancelNumber((String) cancelData.get("cancelPgCno"))
                .cancelAt(cancelDateTime)
                .onNuryStatementNumber(beforePayment.getOnNuryStatementNumber())
                .creditStatementNumber(beforePayment.getCreditStatementNumber())
                .onNuryApprovalPrice(-beforePayment.getOnNuryApprovalPrice())
                .creditApprovalPrice(-beforePayment.getCreditApprovalPrice())
                .totalApprovalPrice(-beforePayment.getTotalApprovalPrice())
                .status("N")
                .build();

        // 주문 취소 정보 저장
        paymentRepository.save(cancelPayment);
    }
     **/


    public List<CancelPaymentResponseDto> listUpPaymentInfo(String orderNumber) {

        List<CancelPaymentResponseDto> OrdersproductList = jpaQueryFactory
                .select(Projections.constructor(CancelPaymentResponseDto.class,
                        payment.orderNumber,
                        orderInProduct.detailOptionTitle,
                        orderInProduct.seq,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        orderInProduct.quantity,
                        payment.orderedAt,
                        payment.buyMemberLoginId,
                        payment.message,
                        orderInProduct.deliveryPrice,
                        orderInProduct.dangerPlacePrice,
                        payment.receiverPhone,
                        payment.creditStatementNumber,
                        payment.creditApprovalPrice,
                        payment.onNuryStatementNumber,
                        payment.onNuryApprovalPrice,
                        payment.totalApprovalPrice,
                        orderInProduct.transportNumber,
                        orderInProduct.parcelName,
                        payment.receiver,
                        payment.address.as("address")))
                .from(orderInProduct, payment)
                .where(productOrderOfOrderInProduct.orderInProductId.eq(orderInProduct.orderInProductId)
                        .and(productOrderOfOrderInProduct.productOrderId.eq(productOrder.proudctOrderId))
                        .and(productOrder.orderNumber.eq(payment.orderNumber)))
                .fetch();

        return OrdersproductList;
    }

    public Payment PaymentInfo(String orderNumber) {

        return jpaQueryFactory.selectFrom(payment).where(payment.orderNumber.eq(orderNumber)).fetchOne();
    }

    public List<OrderInProduct> ProductList(String orderNumber) {

        return jpaQueryFactory.selectFrom(orderInProduct).where(orderInProduct.orderNumber.eq(orderNumber)).fetch();
    }

    public OrderInProduct ProductPart(String orderNumber, String seq) {

        return jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.orderNumber.eq(orderNumber)
                        .and(orderInProduct.seq.eq(seq)))
                .fetchOne();
    }

    public List<OrderInDeliveryAddPrice> DeliveryAddPriceList(String orderNumber) {

        return jpaQueryFactory.selectFrom(orderInDeliveryAddPrice).where(orderInDeliveryAddPrice.orderNumber.eq(orderNumber)).fetch();
    }

    public OrderInDeliveryAddPrice DeliveryAddPricePart(String orderNumber, String seq) {

        return jpaQueryFactory.selectFrom(orderInDeliveryAddPrice).where(orderInDeliveryAddPrice.orderNumber.eq(orderNumber).and(orderInDeliveryAddPrice.seq.eq(seq))).fetchOne();
    }
    @Transactional
    public void productCancleUpdate(String orderNumber) {

        List<OrderInProduct> op = jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.orderNumber.eq(orderNumber))
                .fetch();

        for (int i = 0; i < op.size(); i++) {
            jpaQueryFactory.update(orderInProduct)
                    .set(orderInProduct.cancelAmount, orderInProduct.quantity)
                    .where(orderInProduct.orderNumber.eq(orderNumber).and(orderInProduct.seq.eq(op.get(i).getSeq())))
                    .execute();
        }
        entityManager.flush();
        entityManager.clear();

    }

    @Transactional
    public void productCanclePartUpdate(String orderNumber, String seq, int quantity) {


        jpaQueryFactory.update(orderInProduct)
                .set(orderInProduct.cancelAmount, quantity)
                .where(orderInProduct.orderNumber.eq(orderNumber).and(orderInProduct.seq.eq(seq)))
                .execute();

        entityManager.flush();
        entityManager.clear();

    }
}
