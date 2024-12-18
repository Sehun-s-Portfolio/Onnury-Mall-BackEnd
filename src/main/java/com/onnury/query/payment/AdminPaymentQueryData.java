package com.onnury.query.payment;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.payment.domain.CancleOrder;
import com.onnury.payment.domain.OrderInDeliveryAddPrice;
import com.onnury.payment.domain.OrderInProduct;
import com.onnury.payment.request.TransportInfoRequestDto;
import com.onnury.payment.response.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.onnury.member.domain.QMember.member;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.supplier.domain.QSupplier.supplier;
import static com.onnury.payment.domain.QPayment.payment;
import static com.onnury.payment.domain.QCancleOrder.cancleOrder;
import static com.onnury.payment.domain.QOrderInDeliveryAddPrice.orderInDeliveryAddPrice;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminPaymentQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    // 결제 주문 리스트업 쿼리 함수
    public AdminPaymentListResponseDto listup(AdminAccount account, int page, String searchType, String searchKeyword, String startDate, String endDate) {

        List<AdminPaymentResponse2Dto> productOrderList = jpaQueryFactory
                .select(Projections.constructor(AdminPaymentResponse2Dto.class,
                        payment.orderNumber,
                        payment.linkCompany,
                        orderInProduct.supplierId,
                        supplier.supplierCompany.as("supplierName"),
                        orderInProduct.detailOptionTitle,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        orderInProduct.quantity,
                        orderInProduct.transportNumber,
                        orderInProduct.parcelName,
                        payment.receiver,
                        payment.address,
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.buyMemberLoginId,
                        member.userName != null ? member.userName.as("buyMemberName") : member.manager.as("buyMemberName"), // 결제 고객 명,
                        orderInProduct.cancelAmount,
                        orderInProduct.creditCommissionPrice,
                        orderInProduct.onnuryCommissionPrice,
                        orderInProduct.eventCheck,
                        orderInProduct.eventInfo,
                        orderInProduct.completePurchaseAt.stringValue().as("completePurchaseAt"),
                        orderInProduct.completePurchaseCheck)
                )
                .from(orderInProduct, payment, supplier, member)
                .where(orderInProduct.orderNumber.eq(payment.orderNumber)
                        .and(payment.buyMemberLoginId.eq(member.loginId))
                        .and(orderSearchOfSupplierId(account))
                        .and(orderSearchOfSearchType(searchType, searchKeyword))
                        .and(orderSearchOfRangeDate(startDate, endDate)))
                .orderBy(payment.orderedAt.desc())
                .fetch();

        Long totalOrderCount = (long) productOrderList.size();

        if (productOrderList.size() >= 10) {
            if ((page * 10) <= productOrderList.size()) {
                productOrderList = productOrderList.subList((page * 10) - 10, page * 10);
            } else {
                productOrderList = productOrderList.subList((page * 10) - 10, productOrderList.size());
            }
        } else {
            productOrderList = productOrderList.subList((page * 10) - 10, productOrderList.size());
        }

        return AdminPaymentListResponseDto.builder()
                .total(totalOrderCount)
                .paymentList(productOrderList)
                .build();
    }


    // 로그인한 관리자 혹은 공급사에 따른 결제 주문 이력 검색 기준
    private BooleanExpression orderSearchOfSupplierId(AdminAccount account) {
        if (account.getType().equals("admin")) {
            return orderInProduct.supplierId.eq(supplier.supplierId.stringValue());
        } else {

            String supplierId = jpaQueryFactory
                    .select(supplier.supplierId.stringValue())
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            assert supplierId != null;
            return orderInProduct.supplierId.eq(supplierId)
                    .and(supplier.supplierId.eq(Long.parseLong(supplierId)))
                    .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()));
        }
    }


    public AdminPaymentListResponseDto paymentMemberListUp(int page, String searchType, String searchKeyword, String startDate, String endDate, String memberId) {

        List<AdminPaymentResponse2Dto> OrdersproductList = jpaQueryFactory
                .select(Projections.constructor(AdminPaymentResponse2Dto.class,
                        payment.orderNumber,
                        payment.linkCompany,
                        orderInProduct.supplierId,
                        supplier.supplierCompany.as("supplierName"),
                        orderInProduct.detailOptionTitle,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        orderInProduct.quantity,
                        orderInProduct.transportNumber,
                        orderInProduct.parcelName,
                        payment.receiver,
                        payment.address,
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.buyMemberLoginId,
                        member.userName != null ? member.userName.as("buyMemberName") : member.manager.as("buyMemberName"), // 결제 고객 명,
                        orderInProduct.cancelAmount,
                        orderInProduct.creditCommissionPrice,
                        orderInProduct.onnuryCommissionPrice,
                        orderInProduct.eventCheck,
                        orderInProduct.eventInfo,
                        orderInProduct.completePurchaseAt.stringValue().as("completePurchaseAt"),
                        orderInProduct.completePurchaseCheck)
                )
                .from(orderInProduct, payment, supplier, member)
                .where(orderInProduct.orderNumber.eq(payment.orderNumber)
                        .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()))
                        .and(payment.buyMemberLoginId.eq(member.loginId))
                        .and(payment.buyMemberLoginId.eq(memberId))
                        .and(orderSearchOfSearchType(searchType, searchKeyword))
                        .and(orderSearchOfRangeDate(startDate, endDate)))
                .orderBy(payment.orderedAt.desc())
                .offset((page * 10L) - 10L)
                .limit(10)
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(orderInProduct.count())
                .from(orderInProduct, payment)
                .where(payment.orderNumber.eq(orderInProduct.orderNumber).and(payment.buyMemberLoginId.eq(memberId)))
                .fetchOne();

        return AdminPaymentListResponseDto.builder()
                .total(totalCount)
                .paymentList(OrdersproductList)
                .build();
    }

    public AdminPaymentDetailResponseDto detail(AdminAccount loginAdminAccount, String orderNumber) {

        AdminPaymentMemberPosterResponseDto OrdersproductList2 = jpaQueryFactory
                .select(Projections.constructor(AdminPaymentMemberPosterResponseDto.class,
                        payment.orderNumber,
                        payment.orderedAt.stringValue(),
                        payment.buyMemberLoginId,
                        member.userName,
                        member.birth,
                        member.postNumber,
                        member.address,
                        member.detailAddress,
                        member.email,
                        member.phone,
                        member.type,
                        member.businessNumber,
                        member.manager,
                        payment.receiver,
                        payment.receiverPhone,
                        payment.address.as("posterAddress"),
                        payment.message))
                .from(payment, member)
                .where(payment.orderNumber.eq(orderNumber)
                        .and(payment.buyMemberLoginId.eq(member.loginId)))
                .fetchOne();


        List<AdminPaymentProductResponseDto> OrdersProductList = jpaQueryFactory
                .select(Projections.constructor(AdminPaymentProductResponseDto.class,
                        orderInProduct.seq,
                        supplier.supplierCompany.as("supplierName"),
                        orderInProduct.detailOptionTitle,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        orderInProduct.quantity,
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.buyMemberLoginId,
                        payment.creditStatementNumber,
                        orderInProduct.productTotalAmount.subtract(orderInProduct.onnurypay).as("creditApprovalPrice"),
                        payment.onNuryStatementNumber,
                        orderInProduct.onnurypay.as("onNuryApprovalPrice"),
                        orderInProduct.deliveryPrice,
                        orderInProduct.dangerPlacePrice,
                        orderInProduct.productTotalAmount.as("totalApprovalPrice"),
                        orderInProduct.parcelName,
                        orderInProduct.transportNumber,
                        orderInProduct.cancelAmount,
                        orderInProduct.creditCommissionPrice,
                        orderInProduct.onnuryCommissionPrice,
                        orderInProduct.eventCheck,
                        orderInProduct.eventInfo))
                .from(orderInProduct, supplier, payment)
                .where(payment.orderNumber.eq(orderNumber)
                        .and(payment.orderNumber.eq(orderNumber))
                        .and(payment.orderNumber.eq(orderInProduct.orderNumber))
                        .and(supplierDetailPaymentInfoCheck(loginAdminAccount))
                )
                .orderBy(orderInProduct.seq.desc())
                .fetch();

        List<AdminPaymentDeriveryResponseDto> OrdersDeriveryList = jpaQueryFactory
                .select(Projections.constructor(AdminPaymentDeriveryResponseDto.class,
                        orderInDeliveryAddPrice.orderNumber,
                        orderInDeliveryAddPrice.seq,
                        orderInDeliveryAddPrice.productName,
                        orderInDeliveryAddPrice.supplierId,
                        supplier.supplierCompany.as("supplierName"),
                        orderInDeliveryAddPrice.onnuryPay,
                        orderInDeliveryAddPrice.creditPay,
                        orderInDeliveryAddPrice.amount,
                        orderInDeliveryAddPrice.cancleStatus,
                        orderInDeliveryAddPrice.frcNumber,
                        orderInDeliveryAddPrice.businessNumber))
                .from(orderInDeliveryAddPrice, supplier)
                .where(orderInDeliveryAddPrice.orderNumber.eq(orderNumber)
                        .and(supplierDeliveryPaymentInfoCheck(loginAdminAccount)))
                .orderBy(orderInDeliveryAddPrice.seq.desc())
                .fetch();

        return AdminPaymentDetailResponseDto.builder()
                .paymentDetailList(OrdersproductList2)
                .paymentProductList(OrdersProductList)
                .paymentDeriveryList(OrdersDeriveryList)
                .build();
    }


    // 로그인한 관리자 / 공급사 계정 유형에 따라 상세 결제 내용 차별화
    private BooleanExpression supplierDetailPaymentInfoCheck(AdminAccount loginAdminAccount) {

        if (loginAdminAccount.getType().equals("supplier")) {
            Long supplierId = jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(loginAdminAccount.getAdminAccountId()))
                    .fetchOne();

            assert supplierId != null;

            return supplier.supplierId.eq(supplierId)
                    .and(orderInProduct.supplierId.eq(supplierId.toString())
                    .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue())));
        } else {
            return orderInProduct.supplierId.eq(supplier.supplierId.stringValue());
        }
    }

    private BooleanExpression supplierDeliveryPaymentInfoCheck(AdminAccount loginAdminAccount) {

        if (loginAdminAccount.getType().equals("supplier")) {
            Long supplierId = jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(loginAdminAccount.getAdminAccountId()))
                    .fetchOne();

            assert supplierId != null;

            return orderInDeliveryAddPrice.supplierId.eq(supplierId.toString())
                    .and(supplier.supplierId.eq(supplierId))
                    .and(orderInDeliveryAddPrice.supplierId.eq(supplier.supplierId.stringValue()));
        } else {
            return orderInDeliveryAddPrice.supplierId.eq(supplier.supplierId.stringValue());
        }
    }

    // 로그인한 관리자 / 공급사 계정 유형에 따라 상세 결제 취소 내용 차별화
    private BooleanExpression supplierDetailCancelPaymentInfoCheck(AdminAccount loginAdminAccount) {

        if (loginAdminAccount.getType().equals("supplier")) {
            Long supplierId = jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(loginAdminAccount.getAdminAccountId()))
                    .fetchOne();

            assert supplierId != null;

            return cancleOrder.supplierId.eq(supplierId.toString());
        } else {
            return cancleOrder.supplierId.eq(supplier.supplierId.stringValue());
        }
    }


    // 결제 주문 취소 이력 조회
    public AdminPaymentList3ResponseDto cancelListUp(AdminAccount account, int page) {

        List<AdminPaymentResponse3Dto> productCancelOrdersList = jpaQueryFactory
                .select(Projections.constructor(AdminPaymentResponse3Dto.class,
                        cancleOrder.orderNumber,
                        cancleOrder.supplierId,
                        supplier.supplierCompany.as("supplierName"),
                        cancleOrder.detailOptionTitle,
                        cancleOrder.productClassificationCode,
                        cancleOrder.productName,
                        cancleOrder.cancelAmount.as("quantity"),
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.buyMemberLoginId,
                        cancleOrder.cancelCheck,
                        cancleOrder.cancelAt.stringValue().as("cancelAt"),
                        cancleOrder.seq
                ))
                .from(payment, cancleOrder, supplier)
                .where(payment.orderNumber.eq(cancleOrder.orderNumber)
                        .and(cancelProductOrderOfSupplierId(account))
                        .and(cancleOrder.cancelCheck.eq("N")))
                .orderBy(cancleOrder.cancelAt.desc())
                .fetch()
                .stream()
                .filter(eachCancelData ->
                        jpaQueryFactory
                                .selectFrom(cancleOrder)
                                .where(cancleOrder.orderNumber.eq(eachCancelData.getOrderNumber())
                                        .and(cancleOrder.seq.eq(eachCancelData.getSeq()))
                                        .and(cancleOrder.productClassificationCode.eq(eachCancelData.getProductClassificationCode()))
                                        .and(cancleOrder.cancelCheck.eq("Y")))
                                .fetchOne() == null
                )
                .collect(Collectors.toList());

        Long totalCancelRequestOrderCount = (long) productCancelOrdersList.size();

        if (productCancelOrdersList.size() >= 10) {
            if ((page * 10) <= productCancelOrdersList.size()) {
                productCancelOrdersList = productCancelOrdersList.subList((page * 10) - 10, page * 10);
            } else {
                productCancelOrdersList = productCancelOrdersList.subList((page * 10) - 10, productCancelOrdersList.size());
            }
        } else {
            productCancelOrdersList = productCancelOrdersList.subList((page * 10) - 10, productCancelOrdersList.size());
        }

        return AdminPaymentList3ResponseDto.builder()
                .total(totalCancelRequestOrderCount)
                .paymentList(productCancelOrdersList)
                .build();
    }


    // 결제 주문 취소 이력 조회 시 공급사 id 검색 기준
    private BooleanExpression cancelProductOrderOfSupplierId(AdminAccount account) {
        if (account.getType().equals("admin")) {
            return cancleOrder.supplierId.eq(supplier.supplierId.stringValue());
        } else {
            String supplierId = jpaQueryFactory
                    .select(supplier.supplierId.stringValue())
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            assert supplierId != null;
            return cancleOrder.supplierId.eq(supplierId)
                    .and(supplier.supplierId.eq(Long.parseLong(supplierId)))
                    .and(cancleOrder.supplierId.eq(supplier.supplierId.stringValue()));
        }
    }


    // 공급사 기준 결제 주문 리스트업 쿼리 함수 (정산 관리)
    public AdminSupllierPaymentListResponseDto supplierListUp(AdminAccount account, int page, String supplierId, String searchType, String searchKeyword, String startDate, String endDate) {

        AtomicReference<String> checkOrderNumber = new AtomicReference<>("");
        AtomicReference<Long> checkOrderSupplierId = new AtomicReference<>();
        List<AdminOrderInProductResponseQDto> finalProductOrderList = new ArrayList<>();
        List<AdminOrderInProductResponseQDto> compareProductOrderList = new ArrayList<>();

        jpaQueryFactory
                .select(Projections.constructor(AdminOrderInProductResponseQDto.class,
                        payment.orderNumber,
                        payment.linkCompany,
                        supplier.supplierCompany.as("supplierName"),
                        supplier.supplierId,
                        orderInProduct.detailOptionTitle,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        orderInProduct.quantity,
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.creditStatementNumber,
                        orderInProduct.productTotalAmount.subtract(orderInProduct.onnurypay).as("creditApprovalPrice"),
                        payment.onNuryStatementNumber,
                        orderInProduct.onnurypay.as("onNuryApprovalPrice"),
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.creditCanclePrice.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "creditCanclePrice"),
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.onNuryCanclePrice.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "onNuryCanclePrice"),
                        supplier.onnuryCommission,
                        supplier.creditCommission,
                        orderInProduct.onnuryCommissionPrice,
                        orderInProduct.creditCommissionPrice,
                        orderInProduct.eventCheck,
                        orderInProduct.eventInfo,
                        orderInProduct.onnurypay.subtract(orderInProduct.onnurypay).as("deliveryAddPrice"),
                        orderInProduct.completePurchaseAt.stringValue().as("completePurchaseAt"),
                        orderInProduct.completePurchaseCheck,
                        orderInProduct.parcelName,
                        orderInProduct.transportNumber,
                        orderInProduct.transportNumber.as("transportCheck"))
                )
                .from(orderInProduct, payment, supplier)
                .where(orderInProduct.orderNumber.eq(payment.orderNumber)
                        .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()))
                        .and(supplierOrderSearchOfSupplierId(account, supplierId))
                        .and(supplierOrderSearchOfSearchType(searchType, searchKeyword))
                        .and(supplierOrderSearchOfRangeDate(startDate, endDate))
                )
                .orderBy(payment.orderedAt.desc())
                .fetch()
                .forEach(eachAdminOrderHistory -> {

                    if (checkOrderNumber.get() != null && checkOrderSupplierId.get() != null) {
                        if (!checkOrderNumber.get().equals(eachAdminOrderHistory.getOrderNumber())
                                || checkOrderSupplierId.get() != eachAdminOrderHistory.getSupplierId()) {

                            OrderInDeliveryAddPrice relateDeliveryAddPriceInfo = jpaQueryFactory
                                    .selectFrom(orderInDeliveryAddPrice)
                                    .where(orderInDeliveryAddPrice.orderNumber.eq(checkOrderNumber.get())
                                            .and(orderInDeliveryAddPrice.supplierId.eq(String.valueOf(checkOrderSupplierId.get()))))
                                    .fetchOne();

                            if (relateDeliveryAddPriceInfo != null) {
                                String supplierName = jpaQueryFactory
                                        .select(supplier.supplierCompany)
                                        .from(supplier)
                                        .where(supplier.supplierId.eq(Long.valueOf(relateDeliveryAddPriceInfo.getSupplierId())))
                                        .fetchOne();

                                Tuple transportInfo = jpaQueryFactory
                                        .select(orderInProduct.parcelName, orderInProduct.transportNumber)
                                        .from(orderInProduct)
                                        .where(orderInProduct.orderNumber.eq(checkOrderNumber.get())
                                                .and(orderInProduct.supplierId.eq(checkOrderSupplierId.get().toString())))
                                        .groupBy(orderInProduct.parcelName, orderInProduct.transportNumber)
                                        .fetchOne();

                                if(!checkOrderNumber.get().equals(eachAdminOrderHistory.getOrderNumber())){
                                    compareProductOrderList.add(
                                            AdminOrderInProductResponseQDto.builder()
                                                    .orderNumber(checkOrderNumber.get())
                                                    .orderedAt(relateDeliveryAddPriceInfo.getCreatedAt().toString().replace("T", " "))
                                                    .supplierName(supplierName != null ? supplierName : null)
                                                    .productName(relateDeliveryAddPriceInfo.getProductName())
                                                    .creditApprovalPrice(relateDeliveryAddPriceInfo.getCreditPay() != 0 ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryApprovalPrice(relateDeliveryAddPriceInfo.getOnnuryPay() != 0 ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .creditCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .deliveryAddPrice(relateDeliveryAddPriceInfo.getAmount())
                                                    .completePurchaseAt(relateDeliveryAddPriceInfo.getCreatedAt().toString())
                                                    .completePurchaseCheck("Y")
                                                    .parcelName(transportInfo.get(orderInProduct.parcelName))
                                                    .transportNumber(transportInfo.get(orderInProduct.transportNumber))
                                                    .transportCheck(!transportInfo.get(orderInProduct.transportNumber).isEmpty() ? "Y" : "N")
                                                    .build()
                                    );

                                    finalProductOrderList.addAll(compareProductOrderList);
                                    compareProductOrderList.clear();

                                }else{
                                    compareProductOrderList.add(
                                            AdminOrderInProductResponseQDto.builder()
                                                    .orderNumber(checkOrderNumber.get())
                                                    .orderedAt(relateDeliveryAddPriceInfo.getCreatedAt().toString().replace("T", " "))
                                                    .supplierName(supplierName != null ? supplierName : null)
                                                    .productName(relateDeliveryAddPriceInfo.getProductName())
                                                    .creditApprovalPrice(relateDeliveryAddPriceInfo.getCreditPay() != 0 ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryApprovalPrice(relateDeliveryAddPriceInfo.getOnnuryPay() != 0 ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .creditCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .deliveryAddPrice(relateDeliveryAddPriceInfo.getAmount())
                                                    .completePurchaseAt(relateDeliveryAddPriceInfo.getCreatedAt().toString())
                                                    .completePurchaseCheck("Y")
                                                    .parcelName(transportInfo.get(orderInProduct.parcelName))
                                                    .transportNumber(transportInfo.get(orderInProduct.transportNumber))
                                                    .transportCheck(!transportInfo.get(orderInProduct.transportNumber).isEmpty() ? "Y" : "N")
                                                    .build()
                                    );
                                }

                                finalProductOrderList.add(new AdminOrderInProductResponseQDto().setTransportCheck(eachAdminOrderHistory));
                            } else {
                                finalProductOrderList.add(new AdminOrderInProductResponseQDto().setTransportCheck(eachAdminOrderHistory));
                            }

                            checkOrderNumber.set(eachAdminOrderHistory.getOrderNumber());
                            checkOrderSupplierId.set(eachAdminOrderHistory.getSupplierId());
                        } else {
                            finalProductOrderList.add(new AdminOrderInProductResponseQDto().setTransportCheck(eachAdminOrderHistory));
                        }
                    } else {
                        checkOrderNumber.set(eachAdminOrderHistory.getOrderNumber());
                        checkOrderSupplierId.set(eachAdminOrderHistory.getSupplierId());
                        finalProductOrderList.add(new AdminOrderInProductResponseQDto().setTransportCheck(eachAdminOrderHistory));
                    }
                });

        List<AdminOrderInProductResponseQDto> productOrderList = finalProductOrderList;

        Long totalSupplierOrderCount = (long) productOrderList.size();

        if (productOrderList.size() >= 10) {
            if ((page * 10) <= productOrderList.size()) {
                productOrderList = productOrderList.subList((page * 10) - 10, page * 10);
            } else {
                productOrderList = productOrderList.subList((page * 10) - 10, productOrderList.size());
            }
        } else {
            productOrderList = productOrderList.subList((page * 10) - 10, productOrderList.size());
        }

        return AdminSupllierPaymentListResponseDto.builder()
                .total(totalSupplierOrderCount)
                .paymentList(productOrderList)
                .build();
    }


    // 결제 주문 이력 조회 시 검색 유형 및 검색 키워드 검색 기준
    private BooleanExpression orderSearchOfSearchType(String searchtype, String search) {

        if (!searchtype.isEmpty()) {
            if (!search.isEmpty()) {
                if (searchtype.equals("주문번호")) {
                    return orderInProduct.orderNumber.like("%" + search.replace(" ", "%") + "%");
                } else if (searchtype.equals("제품명")) {
                    return orderInProduct.productName.like("%" + search.replace(" ", "%") + "%");
                } else if (searchtype.equals("제품코드")) {
                    return orderInProduct.productClassificationCode.like("%" + search.replace(" ", "%") + "%");
                } else if (searchtype.equals("공급사")) {
                    return supplier.supplierCompany.like("%" + search.replace(" ", "%") + "%");
                } else if (searchtype.equals("회원ID")) {
                    return payment.buyMemberLoginId.like("%" + search.replace(" ", "%") + "%");
                } else if (searchtype.equals("주문/수령인")) {
                    return member.userName.like("%" + search.replace(" ", "%") + "%").or(payment.receiver.like("%" + search.replace(" ", "%") + "%"));
                }
            } else {
                return null;
            }
        } else {
            if (!search.isEmpty()) {
                return orderInProduct.orderNumber.like("%" + search.replace(" ", "%") + "%")
                        .or(orderInProduct.productName.like("%" + search.replace(" ", "%") + "%"))
                        .or(orderInProduct.productClassificationCode.like("%" + search.replace(" ", "%") + "%"))
                        .or(supplier.supplierCompany.like("%" + search.replace(" ", "%") + "%"))
                        .or(payment.buyMemberLoginId.like("%" + search.replace(" ", "%") + "%"))
                        .or(member.userName.like("%" + search.replace(" ", "%") + "%").or(payment.receiver.like("%" + search.replace(" ", "%") + "%")));
            } else {
                return null;
            }
        }

        return null;
    }


    // 결제 주문 이력 조회 시 범위 일자 검색 기준
    private BooleanExpression orderSearchOfRangeDate(String startDate, String endDate) {

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
            LocalDateTime startOrderDate = LocalDateTime.parse(startDate + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endOrderDate = LocalDateTime.parse(endDate + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            return payment.orderedAt.between(startOrderDate, endOrderDate);
        } else {
            if (!startDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
                LocalDateTime startOrderDate = LocalDateTime.parse(startDate + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
                return payment.orderedAt.after(startOrderDate);
            }

            if (!endDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
                LocalDateTime endOrderDate = LocalDateTime.parse(endDate + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
                return payment.orderedAt.before(endOrderDate);
            }
        }

        return null;
    }


    // 공급사 정산 관리 주문 이력 조회 시 공급사 id 검색 기준
    private BooleanExpression supplierOrderSearchOfSupplierId(AdminAccount account, String supplierId) {

        if (account.getType().equals("admin")) {
            log.info("관리자 접속");

            if (!supplierId.isEmpty()) {
                log.info("공급사 id 존재 : {}", supplierId);
                return orderInProduct.supplierId.eq(supplierId)
                        .and(supplier.supplierId.eq(Long.parseLong(supplierId)))
                        .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()));
            } else {
                log.info("공급사 id 존재하지 않음 : {}", supplierId);
                return null;
            }
        } else {
            log.info("공급사 접속");
            String supplierIdString = jpaQueryFactory
                    .select(supplier.supplierId.stringValue())
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            assert supplierIdString != null;
            return orderInProduct.supplierId.eq(supplierIdString)
                    .and(supplier.supplierId.eq(Long.parseLong(supplierIdString)))
                    .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()));
        }
    }


    // 공급사 정산 관리 주문 이력 조회 시 검색 유형 및 키워드 검색 기준
    private BooleanExpression supplierOrderSearchOfSearchType(String searchType, String search) {
        if (!searchType.isEmpty()) {
            if (!search.isEmpty()) {
                if (searchType.equals("주문번호")) {
                    return payment.orderNumber.like("%" + search.replace(" ", "%") + "%");
                } else if (searchType.equals("제품명")) {
                    return orderInProduct.productName.like("%" + search.replace(" ", "%") + "%");
                } else if (searchType.equals("제품코드")) {
                    return orderInProduct.productClassificationCode.like("%" + search.replace(" ", "%") + "%");
                }
            } else {
                return null;
            }
        } else {
            if (!search.isEmpty()) {
                return payment.orderNumber.like("%" + search.replace(" ", "%") + "%")
                        .or(orderInProduct.productName.like("%" + search.replace(" ", "%") + "%"))
                        .or(orderInProduct.productClassificationCode.like("%" + search.replace(" ", "%") + "%"));
            } else {
                return null;
            }
        }

        return null;
    }


    // 공급사 정산 관리 주문 이력 조회 시 범위 일자 검색 기준
    private BooleanExpression supplierOrderSearchOfRangeDate(String startDate, String endDate) {
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
            LocalDateTime startOrderDate = LocalDateTime.parse(startDate + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endOrderDate = LocalDateTime.parse(endDate + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            return payment.orderedAt.between(startOrderDate, endOrderDate);
        } else {
            if (!startDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
                LocalDateTime startOrderDate = LocalDateTime.parse(startDate + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
                return payment.orderedAt.after(startOrderDate);
            }

            if (!endDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
                LocalDateTime endOrderDate = LocalDateTime.parse(endDate + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
                return payment.orderedAt.before(endOrderDate);
            }
        }

        return null;
    }


    // 운송장 번호 등록 쿼리 함수
    @Transactional
    public String confirmTransportNumber(List<TransportInfoRequestDto> transportInfoRequestDto) {
        log.info("운송장 번호 등록 쿼리 함수 진입");
        log.info("테스트 확인용 DTO : {}", transportInfoRequestDto);
        log.info("테스트 확인용 운송장 번호 : {}", transportInfoRequestDto.get(0).getTransportNumber());
        log.info("테스트 확인용 택배사 : {}", transportInfoRequestDto.get(0).getParcelName());
        log.info("테스트 확인용 시퀀스 : {}", transportInfoRequestDto.get(0).getSeq());
        log.info("테스트 확인용 주문 번호 : {}", transportInfoRequestDto.get(0).getOrderNumber());

        // 운송장 업데이트가 가능한지 확인 변수
        boolean checkSuccessTransportUpdate = false;

        // 운송장 업데이트가 불가한 데이터를 저장할 HashMap 리스트
        List<HashMap<String, String>> cantUpdateTransportInfoOrder = new ArrayList<>();

        // 운송장 업데이트가 불가한 데이터가 존재할 경우 view에 노출시키기 위한 String Reference
        AtomicReference<String> cantUpdateResult = new AtomicReference<>("");

        // 전달 받은 운송장 정보로 혹여나 운송장 업데이트가 불가한 데이터가 존재하는 지 확인
        for (TransportInfoRequestDto eachTransportConfirmData : transportInfoRequestDto) {
            // 취소 이력이 존재하는지 데이터 호출
            CancleOrder existCancelInfo = jpaQueryFactory
                    .selectFrom(cancleOrder)
                    .where(cancleOrder.orderNumber.eq(eachTransportConfirmData.getOrderNumber())
                            .and(cancleOrder.seq.eq(eachTransportConfirmData.getSeq())))
                    .fetchOne();

            // 정상적인 주문 이력 정보가 존재하는지 데잍어 호출
            OrderInProduct existOrderInProduct = jpaQueryFactory
                    .selectFrom(orderInProduct)
                    .where(orderInProduct.orderNumber.eq(eachTransportConfirmData.getOrderNumber())
                            .and(orderInProduct.seq.eq(eachTransportConfirmData.getSeq())))
                    .fetchOne();

            // 취소 이력이 존재하거나 정상적인 주문 이력 정보가 존재하지 않을  경우 진입하여 정보 저장
            if (existCancelInfo != null || existOrderInProduct == null) {
                log.info("취소 이력이 존재하여 운송장 번호 업데이트 불가 데이터 존재함");

                // 운송장 업데이트 불가 true 처리
                checkSuccessTransportUpdate = true;

                // 취소 이력이 존재할 경우 진입
                if(existCancelInfo != null){

                    // 취소 이력 데이터 HashMap 저장 후 리스트 저장
                    HashMap<String, String> existCancelOrder = new HashMap<>();
                    existCancelOrder.put("orderNumber", existCancelInfo.getOrderNumber());
                    existCancelOrder.put("seq", existCancelInfo.getSeq());
                    existCancelOrder.put("productName", existCancelInfo.getProductName());

                    cantUpdateTransportInfoOrder.add(existCancelOrder);
                }

                // 정상적인 주문 이력 정보가 존재하지 않을 경우 진입
                if (existOrderInProduct == null) {
                    // 주문 이력 정보가 존재하지 않을 경우 String Reference 에 노출 문구 저장
                    cantUpdateResult.set(cantUpdateResult + "[" + eachTransportConfirmData.getOrderNumber() + ", " + eachTransportConfirmData.getSeq() + "]" + " 존재하지 않는 주문 번호 및 상품 번호입니다. \n");
                }
            }

        }

        // 전부 운송장 업데이트가 가능할 경우 진입
        if (!checkSuccessTransportUpdate) {
            log.info("전부 운송장 번호 업데이트가 가능할 경우 진입");
            log.info("테스트 확인용 운송장 번호 : {}", transportInfoRequestDto.get(0).getTransportNumber());
            log.info("테스트 확인용 택배사 : {}", transportInfoRequestDto.get(0).getParcelName());
            log.info("테스트 확인용 시퀀스 : {}", transportInfoRequestDto.get(0).getSeq());
            log.info("테스트 확인용 주문 번호 : {}", transportInfoRequestDto.get(0).getOrderNumber());

            // 전달 받은 운송장 데이터들을 기준으로 운송장 업데이트
            transportInfoRequestDto.forEach(eachTransportConfirmData -> {

                log.info("");
                log.info("=================================================");
                log.info("업데이트 운송장 번호 : {}", eachTransportConfirmData.getTransportNumber());
                log.info("업데이트 택배사 이름 : {}", eachTransportConfirmData.getParcelName());
                log.info("업데이트 주문번호 : {}", eachTransportConfirmData.getOrderNumber());
                log.info("=================================================");
                log.info("");

                // 택배사, 운송장 번호 업데이트 처리
                jpaQueryFactory
                        .update(orderInProduct)
                        .set(orderInProduct.parcelName, eachTransportConfirmData.getParcelName())
                        .set(orderInProduct.transportNumber, eachTransportConfirmData.getTransportNumber())
                        .where(orderInProduct.orderNumber.eq(eachTransportConfirmData.getOrderNumber())
                                .and(orderInProduct.seq.eq(eachTransportConfirmData.getSeq())))
                        .execute();
            });

            entityManager.flush();
            entityManager.clear();

            return "운송장 정보가 정상적으로 등록되었습니다.";

        } else { // 하나라도 운송장 업데이트가 불가할 경우 진입
            log.info("하나라도 업데이트가 불가한 데이터가 존재하여 운송장 번호가 반영되지 않을 경우 진입");

            // 취소 요청 진행 중인 주문 제품이 존재하여 운송장 번호를 발급할 수 없는 데이터가 존재할 경우
            if (!cantUpdateTransportInfoOrder.isEmpty()) {
                // 운송장 업데이트가 불가한 데이터들을 기준으로 주문 번호 추출
                List<String> cantUpdateTransportInfoOrderNumberList = cantUpdateTransportInfoOrder.stream()
                        .map(eachCantUpdateOrder -> eachCantUpdateOrder.get("orderNumber"))
                        .distinct()
                        .collect(Collectors.toList());

                // 추출한 주문 번호들을 기준으로 String Reference에 저장
                cantUpdateTransportInfoOrderNumberList.forEach(eachCantData -> {
                    AtomicReference<String> cantUpdateDataResult = new AtomicReference<>("[" + eachCantData + "] ");

                    cantUpdateTransportInfoOrder.stream()
                            .filter(eachCantUpdateOrder -> eachCantUpdateOrder.get("orderNumber").equals(eachCantData))
                            .forEach(eachCantUpdateOrderData -> {
                                cantUpdateDataResult.set(cantUpdateDataResult + "(" + eachCantUpdateOrderData.get("seq") + ") " + eachCantUpdateOrderData.get("productName") + ", ");
                            });

                    cantUpdateResult.set(cantUpdateResult + cantUpdateDataResult.get().substring(0, cantUpdateDataResult.get().length() - 2) + " 주문 제품(들)이 현재 취소 요청되어 운송장 정보를 등록할 수 없습니다.");
                });
            }

            /**
            AtomicReference<String> cantUpdateResult2 = new AtomicReference<>("");

            // 이미 운송장 정보가 발급되어 운송장 번호를 재발급할 수 없는 데이터가 존재할 경우
            if (!cantUpdateAlreadyTransportInfoOrder.isEmpty()) {
                List<String> cantUpdateAlreadyTransportInfoOrderNumberList = cantUpdateAlreadyTransportInfoOrder.stream()
                        .map(eachCantUpdateOrder -> eachCantUpdateOrder.get("orderNumber"))
                        .distinct()
                        .collect(Collectors.toList());

                cantUpdateAlreadyTransportInfoOrderNumberList.forEach(eachCantData -> {
                    AtomicReference<String> cantUpdateDataResult2 = new AtomicReference<>("[" + eachCantData + "] ");

                    cantUpdateAlreadyTransportInfoOrder.stream()
                            .filter(eachCantUpdateOrder -> eachCantUpdateOrder.get("orderNumber").equals(eachCantData))
                            .forEach(eachCantUpdateOrderData -> {
                                cantUpdateDataResult2.set(cantUpdateDataResult2 + "(" + eachCantUpdateOrderData.get("seq") + ") " + eachCantUpdateOrderData.get("productName") + ", ");
                            });

                    cantUpdateResult2.set(cantUpdateResult2 + cantUpdateDataResult2.get().substring(0, cantUpdateDataResult2.get().length() - 2) + " 주문 제품(들)은 이미 운송장 번호가 발급되어 주문이 완료된 제품(들)이므로 운송장 정보를 등록할 수 없습니다.");
                });
            }
             **/

            // 운송장 업데이트가 불가한 데이터들을 주문 번호와 seq 기준으로 String 리스트화하여 변환
            List<String> cantUpdateTransportDataList = cantUpdateTransportInfoOrder.stream()
                    .map(eachCantUpdateOrder ->
                            eachCantUpdateOrder.get("orderNumber") + ":" + eachCantUpdateOrder.get("seq")
                    )
                    .collect(Collectors.toList());

            /**
            cantUpdateTransportDataList.addAll(
                    cantUpdateAlreadyTransportInfoOrder.stream()
                            .map(eachCantUpdateOrder ->
                                    eachCantUpdateOrder.get("orderNumber") + ":" + eachCantUpdateOrder.get("seq")
                            )
                            .collect(Collectors.toList())
            );
             **/

            log.info("운송장 업데이트 불가한 데이터 제외한 나머지 데이터 업데이트 처리");

            // 변환한 운송장 업데이트 불가 리스트를 기준으로 이 리스트에 해당되지 않는 정보들은 운송장 업데이트가 가능한 것으로 판별하여 업데이트 처리
            transportInfoRequestDto.stream()
                    .filter(eachOrder ->
                            !cantUpdateTransportDataList.stream()
                                    .distinct()
                                    .collect(Collectors.toList())
                                    .contains(eachOrder.getOrderNumber() + ":" + eachOrder.getSeq())
                    )
                    .collect(Collectors.toList())
                    .forEach(eachTransportConfirmData -> {

                        // 나머지 운송장 업데이트 처리
                        jpaQueryFactory
                                .update(orderInProduct)
                                .set(orderInProduct.parcelName, eachTransportConfirmData.getParcelName())
                                .set(orderInProduct.transportNumber, eachTransportConfirmData.getTransportNumber())
                                .where(orderInProduct.orderNumber.eq(eachTransportConfirmData.getOrderNumber())
                                        .and(orderInProduct.seq.eq(eachTransportConfirmData.getSeq())))
                                .execute();

                    });

            entityManager.flush();
            entityManager.clear();

            return cantUpdateResult.get();
        }
    }
}
