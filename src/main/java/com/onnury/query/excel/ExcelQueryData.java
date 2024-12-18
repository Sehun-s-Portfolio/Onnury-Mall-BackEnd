package com.onnury.query.excel;

import com.onnury.banner.domain.Banner;
import com.onnury.brand.domain.Brand;
import com.onnury.category.domain.Category;
import com.onnury.category.domain.CategoryInBrand;
import com.onnury.category.response.*;
import com.onnury.excel.response.BannerExcelResponseDto;
import com.onnury.excel.response.FaqExcelResponseDto;
import com.onnury.excel.response.InquiryExcelResponseDto;
import com.onnury.excel.response.LabelExcelResponseDto;
import com.onnury.inquiry.domain.Faq;
import com.onnury.inquiry.response.InquiryDataResponseDto;
import com.onnury.label.domain.Label;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.member.domain.Member;
import com.onnury.member.domain.QMember;
import com.onnury.payment.domain.OrderInDeliveryAddPrice;
import com.onnury.payment.response.AdminOrderInProductResponseQDto;
import com.onnury.payment.response.AdminSupplierPaymentResponseExcelQDto;
import com.onnury.product.domain.Product;
import com.onnury.product.domain.ProductDetailOption;
import com.onnury.product.domain.ProductOfOption;
import com.onnury.product.domain.ProductOption;
import com.onnury.product.request.ProductSearchRequestDto;
import com.onnury.product.response.*;
import com.onnury.supplier.domain.QSupplier;
import com.onnury.supplier.domain.Supplier;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.onnury.banner.domain.QBanner.banner;
import static com.onnury.brand.domain.QBrand.brand;
import static com.onnury.category.domain.QCategory.category;
import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;
import static com.onnury.inquiry.domain.QFaq.faq;
import static com.onnury.inquiry.domain.QInquiry.inquiry;
import static com.onnury.label.domain.QLabel.label;
import static com.onnury.label.domain.QLabelOfProduct.labelOfProduct;
import static com.onnury.media.domain.QMedia.media;
import static com.onnury.member.domain.QMember.member;
import static com.onnury.payment.domain.QCancleOrder.cancleOrder;
import static com.onnury.payment.domain.QOrderInDeliveryAddPrice.orderInDeliveryAddPrice;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.payment.domain.QPayment.payment;
import static com.onnury.payment.domain.QProductOrder.productOrder;
import static com.onnury.payment.domain.QProductOrderOfOrderInProduct.productOrderOfOrderInProduct;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.product.domain.QProductDetailInfo.productDetailInfo;
import static com.onnury.product.domain.QProductDetailOption.productDetailOption;
import static com.onnury.product.domain.QProductOfOption.productOfOption;
import static com.onnury.product.domain.QProductOption.productOption;
import static com.onnury.query.product.ProductQueryData.convertStringToList;
import static com.onnury.supplier.domain.QSupplier.supplier;


@Slf4j
@RequiredArgsConstructor
@Component
public class ExcelQueryData {

    private final JPAQueryFactory jpaQueryFactory;

    // 배너 리스트 excel
    public List<BannerExcelResponseDto> listUpBanner(HttpServletRequest request) {

        return jpaQueryFactory
                .selectFrom(banner)
                .orderBy(banner.expressionOrder.asc(), banner.createdAt.desc())
                .fetch()
                .stream()
                .map(eachBanner ->
                        BannerExcelResponseDto.builder()
                                .bannerId(eachBanner.getBannerId())
                                .title(eachBanner.getTitle())
                                .linkUrl(eachBanner.getLinkUrl())
                                .expressionOrder(eachBanner.getExpressionOrder())
                                .expressionCheck(eachBanner.getExpressionCheck())
                                .startPostDate(eachBanner.getStartPostDate())
                                .endPostDate(eachBanner.getEndPostDate())
                                .createdAt(eachBanner.getCreatedAt().toString())
                                .modifiedAt(eachBanner.getModifiedAt().toString())
                                .build()
                )
                .collect(Collectors.toList());
    }

    // 공급사 리스트 excel
    public List<Supplier> listUpSupplier(HttpServletRequest request) {

        List<Supplier> supplier = jpaQueryFactory
                .selectFrom(QSupplier.supplier)
                .fetch();

        return supplier;
    }

    // 라벨 리스트 excel
    public List<LabelExcelResponseDto> listUpLabel(HttpServletRequest request) {

        return jpaQueryFactory
                .selectFrom(label)
                .orderBy(label.createdAt.desc())
                .fetch()
                .stream()
                .map(eachLabel ->
                        LabelExcelResponseDto.builder()
                                .labelId(eachLabel.getLabelId())
                                .labelTitle(eachLabel.getLabelTitle())
                                .colorCode(eachLabel.getColorCode())
                                .startPostDate(eachLabel.getStartPostDate().toString())
                                .endPostDate(eachLabel.getEndPostDate().toString())
                                .imgUrl(eachLabel.getImgUrl())
                                .topExpression(eachLabel.getTopExpression())
                                .createdAt(eachLabel.getCreatedAt().toString())
                                .modifiedAt(eachLabel.getModifiedAt().toString())
                                .build()
                )
                .collect(Collectors.toList());

    }

    // 카테고리 리스트 excel
    public List<CategoryDataExcelResponseDto> listUpCategory(HttpServletRequest request) {

        List<CategoryDataExcelResponseDto> categoryList = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(2))
                .fetch()
                .stream()
                .map(eachThreeCategory -> {

                    String medmoder = jpaQueryFactory
                            .select(category.motherCode)
                            .from(category)
                            .where(category.classficationCode.eq(eachThreeCategory.getMotherCode()))
                            .fetchOne();

                    String medName = jpaQueryFactory
                            .select(category.categoryName)
                            .from(category)
                            .where(category.classficationCode.eq(eachThreeCategory.getMotherCode()))
                            .fetchOne();

                    String upName = jpaQueryFactory
                            .select(category.categoryName)
                            .from(category)
                            .where(category.classficationCode.eq(medmoder))
                            .fetchOne();

                    List<Long> categoryInBrandIds = jpaQueryFactory
                            .select(categoryInBrand.categoryInBrandId)
                            .from(categoryInBrand)
                            .where(categoryInBrand.category3Id.eq(eachThreeCategory.getCategoryId()))
                            .fetch();

                    Long productCount = jpaQueryFactory
                            .select(product.count())
                            .from(product)
                            .where(product.categoryInBrandId.in(categoryInBrandIds))
                            .fetchOne();

                    assert productCount != null;

                    return CategoryDataExcelResponseDto.builder()
                            .ucategoryName(upName)
                            .mcategoryName(medName)
                            .dcategoryName(eachThreeCategory.getCategoryName())
                            .productCount(productCount)
                            .build();

                })
                .collect(Collectors.toList());

        return categoryList;
    }

    // 제품 리스트 excel
    public List<ProductExcelResponseDto> listUpProduct(ProductSearchRequestDto productSearchRequestDto) {

        // 검색 시 카테고리와 브랜드 조건에 해당되는 CategoryInBrand 들을 우선 호출 (없으면 전체 CategoryInBrand 호출)
        List<Long> searchCategoryInBrandList = jpaQueryFactory
                .select(categoryInBrand.categoryInBrandId)
                .from(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.gt(0L)
                        .and(eqUpCategory(productSearchRequestDto.getUpCategoryId()))
                        .and(eqMiddleCategory(productSearchRequestDto.getMiddleCategoryId()))
                        .and(eqDownCategory(productSearchRequestDto.getDownCategoryId()))
                        .and(eqBrand(productSearchRequestDto.getBrandId())))
                .fetch();

        // 검색된 제품들을 담기 위한 첫 번째 리스트
        List<Product> searchProducts = new ArrayList<>();

        // 호출된 CategoryInBrand가 존재할 시 진입
        if (!searchCategoryInBrandList.isEmpty()) {

            // 리스트에 공급사 id와 검색 키워드, 호출된 CategoryInBrand와 일치한 제품들을 담기
            searchProducts = jpaQueryFactory
                    .selectFrom(product)
                    .where(product.categoryInBrandId.in(searchCategoryInBrandList)
                            .and(eqSupplier(productSearchRequestDto.getSupplierId()))
                            .and(containProductNameSearchKeyword(productSearchRequestDto.getSearchKeyword())))
                    .orderBy(product.productId.desc())
                    .fetch();
        }

        // 최종적으로 확인하기 위한 반환 리스트 선언
        List<ProductExcelResponseDto> getSearchProductList = new ArrayList<>();

        // 검색 제품들이 존재할 경우 진입
        if (!searchProducts.isEmpty()) {

            // 검색 제품들을 돌려 반환 객체에 맞게끔 매핑 및 Convert
            searchProducts.forEach(eachSearchProduct -> {
                // 검색 제품을 Dto 객체로 매핑하여 변환
                ProductCreateExcelResponseDto convertProductInfo = getProduct(eachSearchProduct, "N");


                // 검색된 제품들 저장 리스트에 Dto 객체 정보들로 매핑하여 저장
                getSearchProductList.add(
                        ProductExcelResponseDto.builder()
                                .supplierId(convertProductInfo.getSupplierId())
                                .supplierName(convertProductInfo.getSupplierName())
                                .brandId(convertProductInfo.getBrandId())
                                .brand(convertProductInfo.getBrand())
                                .upCategoryId(convertProductInfo.getUpCategoryId())
                                .upCategory(convertProductInfo.getUpCategory())
                                .middleCategoryId(convertProductInfo.getMiddleCategoryId())
                                .middleCategory(convertProductInfo.getMiddleCategory())
                                .downCategoryId(convertProductInfo.getDownCategoryId())
                                .downCategory(convertProductInfo.getDownCategory())
                                .productId(convertProductInfo.getProductId())
                                .productName(convertProductInfo.getProductName())
                                .classificationCode(convertProductInfo.getClassificationCode())
                                .labelList(convertProductInfo.getLabelList())
                                .modelNumber(convertProductInfo.getModelNumber())
                                .sellClassification(convertProductInfo.getSellClassification())
                                .expressionCheck(convertProductInfo.getExpressionCheck())
                                .normalPrice(convertProductInfo.getNormalPrice())
                                .sellPrice(convertProductInfo.getSellPrice())
                                .deliveryType(convertProductInfo.getDeliveryType())
                                .deliveryPrice(convertProductInfo.getDeliveryPrice())
                                .purchasePrice(convertProductInfo.getPurchasePrice())
                                .eventPrice(convertProductInfo.getEventPrice())
                                .eventStartDate(convertProductInfo.getEventStartDate())
                                .eventEndDate(convertProductInfo.getEventEndDate())
                                .eventDescription(convertProductInfo.getEventDescription())
                                .optionCheck(convertProductInfo.getOptionCheck())
                                .productOptionList(convertProductInfo.getProductOptionList())
                                .productDetailInfo(convertProductInfo.getProductDetailInfo())
                                .manufacturer(convertProductInfo.getManufacturer())
                                .madeInOrigin(convertProductInfo.getMadeInOrigin())
                                .consignmentStore(convertProductInfo.getConsignmentStore())
                                .memo(convertProductInfo.getMemo())
                                .status(convertProductInfo.getStatus())
                                .build()
                );
            });

        }

        return getSearchProductList;
    }

    // 회원 리스트 excel
    public List<Member> listUpMember(HttpServletRequest request, String searchtype, String search) {

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.memberId.goe(1L)
                        .and(membersearch(searchtype, search)))
                .fetch();

        return result;
    }

    // 문의 리스트 excel
    public List<InquiryExcelResponseDto> listUpInquriy(HttpServletRequest request, String searchType, String searchType2, String searchKeyword) {

        List<InquiryExcelResponseDto> inquiryList = jpaQueryFactory
                .selectFrom(inquiry)
                .where(
                        inquiry.inquiryId.gt(0L)
                                .and(inquirysearch(searchType))
                                .and(inquirysearch2(searchType2, searchKeyword))
                )
                .fetch()
                .stream()
                .map(eachInquiry -> {

                    Tuple memberInfo = jpaQueryFactory
                            .select(QMember.member.loginId, QMember.member.userName)
                            .from(QMember.member)
                            .where(QMember.member.memberId.eq(eachInquiry.getMemberId()))
                            .fetchOne();

                    assert memberInfo != null;

                    Tuple inquiryDateTime = jpaQueryFactory
                            .select(inquiry.createdAt.stringValue(), inquiry.answerAt.stringValue())
                            .from(inquiry)
                            .where(inquiry.inquiryId.eq(eachInquiry.getInquiryId()))
                            .fetchOne();

                    return InquiryExcelResponseDto.builder()
                            .inquiryId(eachInquiry.getInquiryId())
                            .inquiryTitle(eachInquiry.getInquiryTitle())
                            .inquiryContent(eachInquiry.getInquiryContent())
                            .type(eachInquiry.getType())
                            .answer(eachInquiry.getAnswer() != null ? eachInquiry.getAnswer() : "")
                            .answerAt(inquiryDateTime.get(inquiry.answerAt.stringValue()) != null ? inquiryDateTime.get(inquiry.answerAt.stringValue()) : "")
                            .createdAt(inquiryDateTime.get(inquiry.createdAt.stringValue()))
                            .customerLoginId(memberInfo.get(QMember.member.loginId))
                            .customerName(memberInfo.get(QMember.member.userName))
                            .build();
                })
                .collect(Collectors.toList());

        return inquiryList;
    }

    // FAQ 리스트 excel
    public List<FaqExcelResponseDto> listUpFaq(HttpServletRequest request, String type) {
        return jpaQueryFactory
                .selectFrom(faq)
                .where(faq.faqId.goe(1L)
                        .and(eqFaqType(type)))
                .orderBy(faq.createdAt.desc())
                .fetch()
                .stream()
                .map(eachFaq ->
                        FaqExcelResponseDto.builder()
                                .faqId(eachFaq.getFaqId())
                                .type(eachFaq.getType())
                                .question(eachFaq.getQuestion())
                                .answer(eachFaq.getAnswer())
                                .expressCheck(eachFaq.getExpressCheck())
                                .createdAt(eachFaq.getCreatedAt().toString())
                                .modifiedAt(eachFaq.getModifiedAt().toString())
                                .build()
                )
                .collect(Collectors.toList());
    }


    // FAQ 액셀 추출을 위한 동적 조건
    private BooleanExpression eqFaqType(String type) {

        if (!type.isEmpty() && !type.equals("전체")) {
            return faq.type.eq(type);
        }

        return null;
    }


    // 주문/결제 리스트 데이터 추출 (취소 이력 없는 결제 이력 리스트)
    public List<AdminSupplierPaymentResponseExcelQDto> listUpPayment(
            Long supplierId,
            String startDate,
            String endDate,
            String searchType,
            String searchKeyword) {

        AtomicReference<String> checkOrderNumber = new AtomicReference<>("");
        AtomicReference<Long> checkOrderSupplierId = new AtomicReference<>();
        List<AdminSupplierPaymentResponseExcelQDto> finalProductOrderList = new ArrayList<>();
        List<AdminSupplierPaymentResponseExcelQDto> compareProductOrderList = new ArrayList<>();

        jpaQueryFactory
                .select(Projections.constructor(AdminSupplierPaymentResponseExcelQDto.class,
                        payment.orderNumber,
                        payment.linkCompany,
                        supplier.supplierCompany.as("supplierName"),
                        supplier.supplierId,
                        orderInProduct.detailOptionTitle,
                        orderInProduct.seq,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        product.consignmentStore,
                        orderInProduct.quantity,
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.buyMemberLoginId,
                        payment.message,
                        orderInProduct.deliveryPrice,
                        orderInProduct.dangerPlacePrice,
                        member.phone,
                        payment.receiverPhone,
                        payment.creditStatementNumber,
                        payment.onNuryStatementNumber,
                        orderInProduct.onnurypay,
                        orderInProduct.productTotalAmount.subtract(orderInProduct.onnurypay).as("creditpay"),
                        orderInProduct.productTotalAmount,
                        orderInProduct.transportNumber,
                        orderInProduct.parcelName,
                        payment.receiver,
                        payment.address,
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.cancelAmount.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "cancelAmount"),
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.creditCanclePrice.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "creditCanclePrice"),
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.onNuryCanclePrice.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "onNuryCanclePrice"),
                        orderInProduct.completePurchaseAt.stringValue().as("completePurchaseAt"),
                        orderInProduct.completePurchaseCheck,
                        supplier.onnuryCommission,
                        supplier.creditCommission,
                        orderInProduct.onnuryCommissionPrice,
                        orderInProduct.creditCommissionPrice,
                        orderInProduct.eventCheck,
                        orderInProduct.eventInfo,
                        orderInProduct.onnurypay.subtract(orderInProduct.onnurypay).as("deliveryAddPrice"),
                        orderInProduct.transportNumber.as("transportCheck"))
                )
                .from(orderInProduct, supplier, payment, member, product)
                .where(orderInProduct.orderNumber.eq(payment.orderNumber)
                        .and(orderInProduct.cancelAmount.eq(0)) // 취소 되지 않은 정상적인 결제 진행한 주문 제품들에 한하여 액셀 추출
                        .and(orderInProduct.productClassificationCode.eq(product.classificationCode))
                        .and(payment.buyMemberLoginId.eq(member.loginId))
                        .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()))
                        .and(checkSupplier(supplierId))
                        .and(checkPaymentExcelRangeDate(startDate, endDate))
                        .and(checkPaymentExcelSearchType(searchType, searchKeyword)))
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

                                String deliveryAddPriceDateTime = jpaQueryFactory
                                        .select(orderInDeliveryAddPrice.createdAt.stringValue())
                                        .from(orderInDeliveryAddPrice)
                                        .where(orderInDeliveryAddPrice.orderInDeliveryAddPriceId.eq(relateDeliveryAddPriceInfo.getOrderInDeliveryAddPriceId()))
                                        .fetchOne();

                                Tuple transportInfo = jpaQueryFactory
                                        .select(orderInProduct.parcelName, orderInProduct.transportNumber)
                                        .from(orderInProduct)
                                        .where(orderInProduct.orderNumber.eq(checkOrderNumber.get())
                                                .and(orderInProduct.supplierId.eq(checkOrderSupplierId.get().toString())))
                                        .groupBy(orderInProduct.parcelName, orderInProduct.transportNumber)
                                        .fetchOne();

                                if (!checkOrderNumber.get().equals(eachAdminOrderHistory.getOrderNumber())) {
                                    compareProductOrderList.add(
                                            AdminSupplierPaymentResponseExcelQDto.builder()
                                                    .orderNumber(checkOrderNumber.get())
                                                    .orderedAt(deliveryAddPriceDateTime)
                                                    .supplierName(supplierName != null ? supplierName : null)
                                                    .productName(relateDeliveryAddPriceInfo.getProductName())
                                                    .onnurypay(relateDeliveryAddPriceInfo.getOnnuryPay() != 0 ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .creditpay(relateDeliveryAddPriceInfo.getCreditPay() != 0 ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .creditCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .productTotalAmount(relateDeliveryAddPriceInfo.getAmount())
                                                    .completePurchaseAt(deliveryAddPriceDateTime)
                                                    .completePurchaseCheck("Y")
                                                    .parcelName(transportInfo.get(orderInProduct.parcelName))
                                                    .transportNumber(transportInfo.get(orderInProduct.transportNumber))
                                                    .transportCheck(!transportInfo.get(orderInProduct.transportNumber).isEmpty() ? "Y" : "N")
                                                    .build()
                                    );

                                    finalProductOrderList.addAll(compareProductOrderList);
                                    compareProductOrderList.clear();

                                } else {
                                    compareProductOrderList.add(
                                            AdminSupplierPaymentResponseExcelQDto.builder()
                                                    .orderNumber(checkOrderNumber.get())
                                                    .orderedAt(deliveryAddPriceDateTime)
                                                    .supplierName(supplierName != null ? supplierName : null)
                                                    .productName(relateDeliveryAddPriceInfo.getProductName())
                                                    .onnurypay(relateDeliveryAddPriceInfo.getOnnuryPay() != 0 ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .creditpay(relateDeliveryAddPriceInfo.getCreditPay() != 0 ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .creditCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .productTotalAmount(relateDeliveryAddPriceInfo.getAmount())
                                                    .completePurchaseAt(deliveryAddPriceDateTime)
                                                    .completePurchaseCheck("Y")
                                                    .parcelName(transportInfo.get(orderInProduct.parcelName))
                                                    .transportNumber(transportInfo.get(orderInProduct.transportNumber))
                                                    .transportCheck(!transportInfo.get(orderInProduct.transportNumber).isEmpty() ? "Y" : "N")
                                                    .build()
                                    );
                                }

                                finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                            } else {
                                finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                            }

                            checkOrderNumber.set(eachAdminOrderHistory.getOrderNumber());
                            checkOrderSupplierId.set(eachAdminOrderHistory.getSupplierId());
                        } else {
                            finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                        }
                    } else {
                        checkOrderNumber.set(eachAdminOrderHistory.getOrderNumber());
                        checkOrderSupplierId.set(eachAdminOrderHistory.getSupplierId());
                        finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                    }

                });


        // 안녕하세요
        return finalProductOrderList;
    }

    private BooleanExpression checkSupplier(Long supplierId) {
        if (supplierId != 0L) {
            return supplier.supplierId.eq(supplierId);
        }

        return null;
    }


    private BooleanExpression checkPaymentExcelRangeDate(String startDate, String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDateTime startPostDate = LocalDateTime.parse(startDate + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endPostDate = LocalDateTime.parse(endDate + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            return payment.orderedAt.between(startPostDate, endPostDate);
        } else {
            if (!startDate.isEmpty()) {
                LocalDateTime startPostDate = LocalDateTime.parse(startDate + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
                return payment.orderedAt.after(startPostDate);
            }

            if (!endDate.isEmpty()) {
                LocalDateTime endPostDate = LocalDateTime.parse(endDate + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
                return payment.orderedAt.before(endPostDate);
            }
        }

        return null;
    }


    private BooleanExpression checkPaymentExcelSearchType(String searchType, String searchKeyword) {

        if (!searchType.isEmpty() && !searchKeyword.isEmpty()) {

            if (searchType.equals("주문번호")) {
                return orderInProduct.orderNumber.like("%" + searchKeyword.replace(" ", "%") + "%");
            } else if (searchType.equals("제품명")) {
                return orderInProduct.productName.like("%" + searchKeyword.replace(" ", "%") + "%");
            } else if (searchType.equals("제품코드")) {
                return orderInProduct.productClassificationCode.like("%" + searchKeyword.replace(" ", "%") + "%");
            } else if (searchType.equals("공급사")) {
                return supplier.supplierCompany.like("%" + searchKeyword.replace(" ", "%") + "%");
            } else if (searchType.equals("회원ID")) {
                return payment.buyMemberLoginId.like("%" + searchKeyword.replace(" ", "%") + "%");
            } else if (searchType.equals("주문/수령인")) {
                return member.userName.like("%" + searchKeyword.replace(" ", "%") + "%").or(payment.receiver.like("%" + searchKeyword.replace(" ", "%") + "%"));
            }

        } else {

            if (!searchType.isEmpty()) {
                return null;
            }

            if (!searchKeyword.isEmpty()) {
                return orderInProduct.orderNumber.like("%" + searchKeyword.replace(" ", "%") + "%")
                        .or(orderInProduct.productName.like("%" + searchKeyword.replace(" ", "%") + "%"))
                        .or(orderInProduct.productClassificationCode.like("%" + searchKeyword.replace(" ", "%") + "%"))
                        .or(supplier.supplierCompany.like("%" + searchKeyword.replace(" ", "%") + "%"))
                        .or(payment.buyMemberLoginId.like("%" + searchKeyword.replace(" ", "%") + "%"))
                        .or(member.userName.like("%" + searchKeyword.replace(" ", "%") + "%"))
                        .or(payment.receiver.like("%" + searchKeyword.replace(" ", "%") + "%"));
            }
        }

        return null;
    }

    private BooleanExpression membersearch(String searchtype, String search) {

        if (!searchtype.isEmpty() && !search.isEmpty()) {
            //'회원ID', '이름', '제목'
            if (searchtype.equals("회원ID")) {
                return member.loginId.like("%" + search.replace(" ", "%") + "%");
            } else if (searchtype.equals("이름")) {
                return member.userName.like("%" + search.replace(" ", "%") + "%");
            } else if (searchtype.equals("사업자번호")) {
                return member.businessNumber.like("%" + search.replace(" ", "%") + "%");
            } else if (searchtype.equals("생년월일")) {
                return member.birth.like("%" + search.replace(" ", "%") + "%");
            }
        }
        return null;
    }

    private BooleanExpression inquirysearch(String searchType) {

        if (!searchType.equals("")) {
            // 검색 요청 키워드에서 텍스트 서칭이 가능하도록 키워드에 % 기호 적용
            return inquiry.type.eq(searchType);
        }
        return null;
    }


    private BooleanExpression inquirysearch2(String searchType2, String searchKeyword) {

        if (!searchType2.equals("") && !searchKeyword.equals("")) {
            //'회원ID', '이름', '제목'
            if (searchType2.equals("회원ID")) {
                if (!searchKeyword.isEmpty()) {
                    List<Long> searchInquiryIds = jpaQueryFactory
                            .select(member.memberId)
                            .from(member)
                            .where(member.loginId.like("%" + searchKeyword.replace(" ", "%") + "%"))
                            .fetch();
                    return inquiry.memberId.in(searchInquiryIds);
                }
            } else if (searchType2.equals("이름")) {
                if (!searchKeyword.isEmpty()) {
                    List<Long> searchInquiryIds = jpaQueryFactory
                            .select(member.memberId)
                            .from(member)
                            .where(member.userName.like("%" + searchKeyword.replace(" ", "%") + "%"))
                            .fetch();
                    return inquiry.memberId.in(searchInquiryIds);
                }
            } else if (searchType2.equals("제목")) {
                if (!searchKeyword.isEmpty()) {
                    return inquiry.inquiryTitle.like("%" + searchKeyword.replace(" ", "%") + "%");
                }
            }

        }
        return null;
    }

    // 대분류 카테고리 검색 조건
    private BooleanExpression eqUpCategory(Long upCategoryId) {
        // 대분류 카테고리 id가 존재하거나 0이 아닐 경우 진입
        if (upCategoryId != null && upCategoryId != 0L) {
            // 해당되는 대분류 카테고리 id를 가진 매핑 정보 조건 적용
            return categoryInBrand.category1Id.eq(upCategoryId);
        }

        return null;
    }

    // 중분류 카테고리 검색 조건
    private BooleanExpression eqMiddleCategory(Long middleCategoryId) {
        // 중분류 카테고리 id가 존재하거나 0이 아닐 경우 진입
        if (middleCategoryId != null && middleCategoryId != 0L) {
            // 해당되는 중분류 카테고리 id를 가진 매핑 정보 조건 적용
            return categoryInBrand.category2Id.eq(middleCategoryId);
        }

        return null;
    }

    // 소분류 카테고리 검색 조건
    private BooleanExpression eqDownCategory(Long downCategoryId) {
        // 소분류 카테고리 id가 존재하거나 0이 아닐 경우 진입
        if (downCategoryId != null && downCategoryId != 0L) {
            // 해당되는 소분류 카테고리 id를 가진 매핑 정보 조건 적용
            return categoryInBrand.category3Id.eq(downCategoryId);
        }

        return null;
    }

    // 브랜드 검색 조건
    private BooleanExpression eqBrand(Long brandId) {
        // 브랜드 id가 존재하고 0이 아닐 경우 진입
        if (brandId != null && brandId != 0L) {
            // 해당되는 브랜드 id를 가진 매핑 정보 조건 적용
            return categoryInBrand.brandId.eq(brandId);
        }

        return null;
    }

    // 공급사 검색 조건
    private BooleanExpression eqSupplier(Long supplierId) {
        // 공급사 id가 존재하고 0이 아닐 경우 진입
        if (supplierId != null && supplierId != 0L) {
            // 제품의 공급사 id 조건 적용
            return product.supplierId.eq(supplierId);
        }

        return null;
    }

    // 제품 명 검색 키워드 조건
    private BooleanExpression containProductNameSearchKeyword(String searchKeyword) {
        // 검색 키워드가 존재하고 한 글자라도 입력했을 경우 진입
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            // 제품 명 기준으로 검색 키워드 조건을 적용
            return product.productName.like("%" + searchKeyword.replace(" ", "%") + "%");
        }

        return null;
    }

    // 생성된 제품 조회
    public ProductCreateExcelResponseDto getProduct(Product getProduct, String checkNeedProductDetailInfo) {
        // 생성된 제품 호출
        Product createProduct = jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(getProduct.getProductId()))
                .fetchOne();

        assert createProduct != null;

        Supplier getSupplierName = jpaQueryFactory
                .selectFrom(supplier)
                .where(supplier.supplierId.eq(createProduct.getSupplierId()))
                .fetchOne();

        // 제품에 연관된 카테고리 및 브랜드 매핑 정보 호출
        CategoryInBrand getCategoryInBrand = jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.eq(createProduct.getCategoryInBrandId()))
                .fetchOne();

        assert getCategoryInBrand != null;

        // 제품과 매핑된 브랜드 정보 호출
        Brand getBrand = jpaQueryFactory
                .selectFrom(brand)
                .where(brand.brandId.eq(getCategoryInBrand.getBrandId()))
                .fetchOne();

        // 브랜드 id 초기 값 설정 (브랜드가 삭제 및 존재하지 않게 되었을 경우 0으로 초기값 부여)
        Long brandId = 0L;
        // 브랜드 명 초기 값 설정 (브랜드가 삭제 및 존재하지 않게 되었을 경우 공백으로 초기값 부여)
        String brandTitle = "";

        // 연관된 브랜드 정보가 존재할 경우 진입
        if (getBrand != null) {
            // 브랜드 id 및 브랜드 명 설정
            brandId = getBrand.getBrandId();
            brandTitle = getBrand.getBrandTitle();
        }

        // 제품과 연관된 대분류 카테고리 호출
        Category upCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory1Id()))
                .fetchOne();

        // 제품과 연관된 중분류 카테고리 호출
        Category middleCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory2Id()))
                .fetchOne();

        // 제품과 연관된 소분류 카테고리 호출
        Category downCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory3Id()))
                .fetchOne();

        // 제품에 연관된 라벨 id 리스트 추출
        List<Long> getLabelOfProduct = jpaQueryFactory
                .select(labelOfProduct.labelId)
                .from(labelOfProduct)
                .where(labelOfProduct.productId.eq(createProduct.getProductId()))
                .fetch();

        // 제품과 연관된 라벨 정보들을 저장할 리스트 생성
        List<LabelDataResponseDto> labelList = new ArrayList<>();

        // 제품과 연관된 라벨 정보들이 존재할 경우 진입
        if (!getLabelOfProduct.isEmpty()) {
            // 라벨 정보들을 하나씩 조회하며 라벨 정보들 추출
            getLabelOfProduct.forEach(eachLabelOfProduct -> {
                // 라벨 정보 추출
                Label getLabel = jpaQueryFactory
                        .selectFrom(label)
                        .where(label.labelId.eq(eachLabelOfProduct)
                                .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                        .fetchOne();

                if (getLabel != null) {
                    // 반환 리스트에 라벨 정보 저장
                    labelList.add(
                            LabelDataResponseDto.builder()
                                    .labelId(getLabel.getLabelId())
                                    .labelTitle(getLabel.getLabelTitle())
                                    .colorCode(getLabel.getColorCode())
                                    .startPostDate(getLabel.getStartPostDate())
                                    .endPostDate(getLabel.getEndPostDate())
                                    .imgUrl(getLabel.getImgUrl())
                                    .topExpression(getLabel.getTopExpression())
                                    .build()
                    );
                }

            });
        }

        // 제품에 해당되는 옵션 매핑 정보 추출
        List<ProductOfOption> getProductOfOptions = jpaQueryFactory
                .selectFrom(productOfOption)
                .where(productOfOption.productId.eq(createProduct.getProductId()))
                .fetch();

        // 제품 옵션을 저장할 리스트 생성
        List<ProductOptionCreateResponseDto> productOptionList = new ArrayList<>();

        // 만약 옵션 매핑 정보가 존재할 경우 진입
        if (!getProductOfOptions.isEmpty()) {
            // 옵션 매핑 정보들을 기준으로 옵션 데이터 처리
            getProductOfOptions.forEach(eachProductOfOption -> {
                // 매핑 정보 하나씩 조회하여 실제 제품 옵션 데이터 호출
                ProductOption getProductOption = jpaQueryFactory
                        .selectFrom(productOption)
                        .where(productOption.productOptionId.eq(eachProductOfOption.getProductOptionId()))
                        .fetchOne();

                // 제품 옵션에 속한 상세 옵션 데이터 추출
                List<ProductDetailOption> getProductDetailOptions = jpaQueryFactory
                        .selectFrom(productDetailOption)
                        .where(productDetailOption.productOption.eq(getProductOption))
                        .fetch();

                // 제품 상세 정보를 저장할 반환 리스트 생성
                List<ProductDetailOptionCreateResponseDto> productDetailOptionList = new ArrayList<>();

                // 상세 정보들이 존재할 경우 진입
                if (!getProductDetailOptions.isEmpty()) {
                    // 상세 정보들을 하나씩 조회하여 처리
                    getProductDetailOptions.forEach(eachProductDetailOption -> {
                        // 반환 리스트에 상세 옵션 정보 저장
                        productDetailOptionList.add(
                                ProductDetailOptionCreateResponseDto.builder()
                                        .detailOptionId(eachProductDetailOption.getProductDetailOptionId())
                                        .detailOptionName(eachProductDetailOption.getDetailOptionName())
                                        .optionPrice(eachProductDetailOption.getOptionPrice())
                                        .build()
                        );
                    });
                }

                assert getProductOption != null;

                // 제품 옵션 저장 리스트에 상위 옵션 정보들 저장
                productOptionList.add(
                        ProductOptionCreateResponseDto.builder()
                                .productOptionId(getProductOption.getProductOptionId())
                                .productOptionTitle(getProductOption.getOptionTitle())
                                .necessaryCheck(getProductOption.getNecessaryCheck())
                                .productDetailOptionList(productDetailOptionList)
                                .build()
                );
            });
        }

        String productDetailInfoContent = "";

        return ProductCreateExcelResponseDto.builder()
                .supplierId(createProduct.getSupplierId())
                .supplierName(getSupplierName.getSupplierCompany())
                .brandId(brandId)
                .brand(brandTitle)
                .upCategoryId(upCategory.getCategoryId())
                .upCategory(upCategory.getCategoryName())
                .middleCategoryId(middleCategory.getCategoryId())
                .middleCategory(middleCategory.getCategoryName())
                .downCategoryId(downCategory.getCategoryId())
                .downCategory(downCategory.getCategoryName())
                .productId(createProduct.getProductId())
                .productName(createProduct.getProductName())
                .classificationCode(createProduct.getClassificationCode())
                .labelList(labelList)
                .modelNumber(createProduct.getModelNumber())
                .deliveryType(createProduct.getDeliveryType())
                .sellClassification(createProduct.getSellClassification())
                .expressionCheck(createProduct.getExpressionCheck())
                .normalPrice(createProduct.getNormalPrice())
                .sellPrice(createProduct.getSellPrice())
                .deliveryPrice(createProduct.getDeliveryPrice())
                .purchasePrice(createProduct.getPurchasePrice())
                .eventPrice(createProduct.getEventPrice())
                .eventStartDate(createProduct.getEventStartDate().toString())
                .eventEndDate(createProduct.getEventEndDate().toString())
                .eventDescription(createProduct.getEventDescription())
                .optionCheck(createProduct.getOptionCheck())
                .productOptionList(productOptionList)
                .productDetailInfo(productDetailInfoContent)
                .relateImgIds(createProduct.getRelateImgIds())
                .manufacturer(createProduct.getManufacturer())
                .madeInOrigin(createProduct.getMadeInOrigin())
                .consignmentStore(createProduct.getConsignmentStore())
                .memo(createProduct.getMemo())
                .status(createProduct.getStatus())
                .build();
    }


    // 총 결제 주문 정산 리스트 데이터 추출 (취소 이력 포함 모든 결제 이력 리스트)
    public List<AdminSupplierPaymentResponseExcelQDto> listUpTotalOrder(
            Long supplierId,
            String startDate,
            String endDate,
            String searchType,
            String searchKeyword) {

        AtomicReference<String> checkOrderNumber = new AtomicReference<>("");
        AtomicReference<Long> checkOrderSupplierId = new AtomicReference<>();
        List<AdminSupplierPaymentResponseExcelQDto> finalProductOrderList = new ArrayList<>();
        List<AdminSupplierPaymentResponseExcelQDto> compareProductOrderList = new ArrayList<>();

        jpaQueryFactory
                .select(Projections.constructor(AdminSupplierPaymentResponseExcelQDto.class,
                        payment.orderNumber,
                        payment.linkCompany,
                        supplier.supplierCompany.as("supplierName"),
                        supplier.supplierId,
                        orderInProduct.detailOptionTitle,
                        orderInProduct.seq,
                        orderInProduct.productClassificationCode,
                        orderInProduct.productName,
                        product.consignmentStore,
                        orderInProduct.quantity,
                        payment.orderedAt.stringValue().as("orderedAt"),
                        payment.buyMemberLoginId,
                        payment.message,
                        orderInProduct.deliveryPrice,
                        orderInProduct.dangerPlacePrice,
                        member.phone,
                        payment.receiverPhone,
                        payment.creditStatementNumber,
                        payment.onNuryStatementNumber,
                        orderInProduct.onnurypay,
                        orderInProduct.productTotalAmount.subtract(orderInProduct.onnurypay).as("creditpay"),
                        orderInProduct.productTotalAmount,
                        orderInProduct.transportNumber,
                        orderInProduct.parcelName,
                        payment.receiver,
                        payment.address,
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.cancelAmount.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "cancelAmount"),
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.creditCanclePrice.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "creditCanclePrice"),
                        ExpressionUtils.as(JPAExpressions.select(cancleOrder.onNuryCanclePrice.sum()).from(cancleOrder).where(orderInProduct.orderNumber.eq(cancleOrder.orderNumber).and(orderInProduct.seq.eq(cancleOrder.seq)).and(cancleOrder.cancelCheck.eq("Y"))), "onNuryCanclePrice"),
                        orderInProduct.completePurchaseAt.stringValue().as("completePurchaseAt"),
                        orderInProduct.completePurchaseCheck,
                        supplier.onnuryCommission,
                        supplier.creditCommission,
                        orderInProduct.onnuryCommissionPrice,
                        orderInProduct.creditCommissionPrice,
                        orderInProduct.eventCheck,
                        orderInProduct.eventInfo,
                        orderInProduct.onnurypay.subtract(orderInProduct.onnurypay).as("deliveryAddPrice"),
                        orderInProduct.transportNumber.as("transportCheck"))
                )
                .from(orderInProduct, supplier, payment, member, product)
                .where(orderInProduct.orderNumber.eq(payment.orderNumber)
                        .and(orderInProduct.productClassificationCode.eq(product.classificationCode))
                        .and(payment.buyMemberLoginId.eq(member.loginId))
                        .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue()))
                        .and(checkSupplier(supplierId))
                        .and(checkPaymentExcelRangeDate(startDate, endDate))
                        .and(checkPaymentExcelSearchType(searchType, searchKeyword)))
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

                                String deliveryAddPriceDateTime = jpaQueryFactory
                                        .select(orderInDeliveryAddPrice.createdAt.stringValue())
                                        .from(orderInDeliveryAddPrice)
                                        .where(orderInDeliveryAddPrice.orderInDeliveryAddPriceId.eq(relateDeliveryAddPriceInfo.getOrderInDeliveryAddPriceId()))
                                        .fetchOne();

                                Tuple transportInfo = jpaQueryFactory
                                        .select(orderInProduct.parcelName, orderInProduct.transportNumber)
                                        .from(orderInProduct)
                                        .where(orderInProduct.orderNumber.eq(checkOrderNumber.get())
                                                .and(orderInProduct.supplierId.eq(checkOrderSupplierId.get().toString())))
                                        .groupBy(orderInProduct.parcelName, orderInProduct.transportNumber)
                                        .fetchOne();

                                if (!checkOrderNumber.get().equals(eachAdminOrderHistory.getOrderNumber())) {

                                    compareProductOrderList.add(
                                            AdminSupplierPaymentResponseExcelQDto.builder()
                                                    .orderNumber(checkOrderNumber.get())
                                                    .orderedAt(deliveryAddPriceDateTime)
                                                    .supplierName(supplierName != null ? supplierName : null)
                                                    .productName(relateDeliveryAddPriceInfo.getProductName())
                                                    .onnurypay(relateDeliveryAddPriceInfo.getOnnuryPay() != 0 ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .creditpay(relateDeliveryAddPriceInfo.getCreditPay() != 0 ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .creditCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .productTotalAmount(relateDeliveryAddPriceInfo.getAmount())
                                                    .completePurchaseAt(deliveryAddPriceDateTime)
                                                    .completePurchaseCheck("Y")
                                                    .parcelName(transportInfo.get(orderInProduct.parcelName))
                                                    .transportNumber(transportInfo.get(orderInProduct.transportNumber))
                                                    .transportCheck(!transportInfo.get(orderInProduct.transportNumber).isEmpty() ? "Y" : "N")
                                                    .build()
                                    );

                                    finalProductOrderList.addAll(compareProductOrderList);
                                    compareProductOrderList.clear();

                                } else {
                                    compareProductOrderList.add(
                                            AdminSupplierPaymentResponseExcelQDto.builder()
                                                    .orderNumber(checkOrderNumber.get())
                                                    .orderedAt(relateDeliveryAddPriceInfo.getCreatedAt().toString())
                                                    .supplierName(supplierName != null ? supplierName : null)
                                                    .productName(relateDeliveryAddPriceInfo.getProductName())
                                                    .onnurypay(relateDeliveryAddPriceInfo.getOnnuryPay() != 0 ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .creditpay(relateDeliveryAddPriceInfo.getCreditPay() != 0 ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .creditCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getCreditPay() : 0)
                                                    .onNuryCanclePrice(relateDeliveryAddPriceInfo.getCancleStatus().equals("Y") ? relateDeliveryAddPriceInfo.getOnnuryPay() : 0)
                                                    .productTotalAmount(relateDeliveryAddPriceInfo.getAmount())
                                                    .completePurchaseAt(relateDeliveryAddPriceInfo.getCreatedAt().toString())
                                                    .completePurchaseCheck("Y")
                                                    .parcelName(transportInfo.get(orderInProduct.parcelName))
                                                    .transportNumber(transportInfo.get(orderInProduct.transportNumber))
                                                    .transportCheck(!transportInfo.get(orderInProduct.transportNumber).isEmpty() ? "Y" : "N")
                                                    .build()
                                    );
                                }

                                finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                            } else {
                                finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                            }

                            checkOrderNumber.set(eachAdminOrderHistory.getOrderNumber());
                            checkOrderSupplierId.set(eachAdminOrderHistory.getSupplierId());
                        } else {
                            finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                        }
                    } else {
                        checkOrderNumber.set(eachAdminOrderHistory.getOrderNumber());
                        checkOrderSupplierId.set(eachAdminOrderHistory.getSupplierId());
                        finalProductOrderList.add(new AdminSupplierPaymentResponseExcelQDto().setTransportCheck(eachAdminOrderHistory));
                    }
                });

        return finalProductOrderList;
    }

}
