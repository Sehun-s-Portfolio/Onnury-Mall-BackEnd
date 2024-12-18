package com.onnury.query.admin;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.admin.response.*;
import com.onnury.payment.domain.CancleOrder;
import com.onnury.payment.domain.OrderInProduct;

import com.onnury.product.domain.Product;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.admin.domain.QAdminAccount.adminAccount;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.payment.domain.QCancleOrder.cancleOrder;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;
import static com.onnury.category.domain.QCategory.category;
import static com.onnury.supplier.domain.QSupplier.supplier;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminQueryData {

    private final JPAQueryFactory jpaQueryFactory;

    // 로그인 아이디 기준 관리자 계정 조회
    public AdminAccount getLoginAdminAccount(String accountType, String loginId) {
        AdminAccount loginAdmin = jpaQueryFactory
                .selectFrom(adminAccount)
                .where(adminAccount.loginId.eq(loginId)
                        .and(adminAccount.type.eq(accountType)))
                .fetchOne();

        if (loginAdmin != null) {
            return loginAdmin;
        } else {
            return null;
        }
    }

    public AdminAccount getAdminAccount(String accountType, String loginId) {
        AdminAccount loginAdmin = jpaQueryFactory
                .selectFrom(adminAccount)
                .where(adminAccount.loginId.eq(loginId)
                        .and(adminAccount.type.eq(accountType.equals("A") ? "admin" : "supplier")))
                .fetchOne();

        if (loginAdmin != null) {
            return loginAdmin;
        } else {
            return null;
        }
    }


    // 관리자 계정 회원가입 시 동일한 중복 계정 로그인 아이디 존재 유무 확인 쿼리 함수
    public AdminAccount checkDuplicateAdminLoginId(String loginId) {
        AdminAccount loginAdmin = jpaQueryFactory
                .selectFrom(adminAccount)
                .where(adminAccount.loginId.eq(loginId))
                .fetchOne();

        if (loginAdmin != null) {
            return loginAdmin;
        } else {
            return null;
        }
    }



    // 주문 제품 기간 조회 설정
    private BooleanExpression rangeOrderDate(String startDate, String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            String convertStartDateTime = startDate + " 00:00:00";
            String convertEndDateTime = endDate + " 23:59:59";
            LocalDateTime startDateTime = LocalDateTime.parse(convertStartDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endDateTime = LocalDateTime.parse(convertEndDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return orderInProduct.createdAt.after(startDateTime)
                    .and(orderInProduct.createdAt.before(endDateTime));
        } else if (!startDate.isEmpty() && endDate.isEmpty()) {
            String convertStartDateTime = startDate + " 00:00:00";
            LocalDateTime startDateTime = LocalDateTime.parse(convertStartDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return orderInProduct.createdAt.after(startDateTime);
        } else if (startDate.isEmpty() && !endDate.isEmpty()) {
            String convertEndDateTime = endDate + " 23:59:59";
            LocalDateTime endDateTime = LocalDateTime.parse(convertEndDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return orderInProduct.createdAt.before(endDateTime);
        }

        return null;
    }


    // 주문 제품 기간 조건
    private BooleanExpression rangeOrderInProductDate(String startDate, String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            String convertStartDateTime = startDate + " 00:00:00";
            String convertEndDateTime = endDate + " 23:59:59";
            LocalDateTime startDateTime = LocalDateTime.parse(convertStartDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endDateTime = LocalDateTime.parse(convertEndDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return orderInProduct.createdAt.after(startDateTime)
                    .and(orderInProduct.createdAt.before(endDateTime));
        } else if (!startDate.isEmpty() && endDate.isEmpty()) {
            String convertStartDateTime = startDate + " 00:00:00";
            LocalDateTime startDateTime = LocalDateTime.parse(convertStartDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return orderInProduct.createdAt.after(startDateTime);
        } else if (startDate.isEmpty() && !endDate.isEmpty()) {
            String convertEndDateTime = endDate + " 23:59:59";
            LocalDateTime endDateTime = LocalDateTime.parse(convertEndDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return orderInProduct.createdAt.before(endDateTime);
        }

        return null;
    }


    // 취소 주문 기간 조건
    private BooleanExpression rangeCancelOrderDate(String startDate, String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            String convertStartDateTime = startDate + " 00:00:00";
            String convertEndDateTime = endDate + " 23:59:59";
            LocalDateTime startDateTime = LocalDateTime.parse(convertStartDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endDateTime = LocalDateTime.parse(convertEndDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return cancleOrder.createdAt.after(startDateTime)
                    .and(cancleOrder.createdAt.before(endDateTime));
        } else if (!startDate.isEmpty() && endDate.isEmpty()) {
            String convertStartDateTime = startDate + " 00:00:00";
            LocalDateTime startDateTime = LocalDateTime.parse(convertStartDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return cancleOrder.createdAt.after(startDateTime);
        } else if (startDate.isEmpty() && !endDate.isEmpty()) {
            String convertEndDateTime = endDate + " 23:59:59";
            LocalDateTime endDateTime = LocalDateTime.parse(convertEndDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return cancleOrder.createdAt.before(endDateTime);
        }

        return null;
    }


    // 해당 브랜드 조건
    private BooleanExpression eqBrand(List<Long> brandIdList) {

        if (!brandIdList.isEmpty()) {
            return categoryInBrand.brandId.in(brandIdList);
        }

        return null;
    }


    // 해당 공급사 조건
    private BooleanExpression eqSupplier(List<Long> supplierIdList) {

        if (!supplierIdList.isEmpty()) {
            return product.supplierId.in(supplierIdList);
        }

        return null;
    }


    // [개선 1] 대시 보드
    public DashBoardResponseDto adminDashBoardCase2(AdminAccount account, String startDate, String endDate, List<Long> brandIdList, List<Long> supplierIdList) {
        log.info("대시 보드 데이터 추출 로직 수행");

        // 주문 이력 데이터 추출
        List<OrderInProduct> dashBoardOrderProducts = jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.productClassificationCode.in(
                                        jpaQueryFactory
                                                .select(product.classificationCode)
                                                .from(product)
                                                .where(product.productId.in(
                                                                jpaQueryFactory
                                                                        .select(product.productId)
                                                                        .from(product)
                                                                        .where(product.classificationCode.in(
                                                                                jpaQueryFactory
                                                                                        .select(orderInProduct.productClassificationCode)
                                                                                        .from(orderInProduct)
                                                                                        .where(orderInProduct.orderInProductId.goe(1L)
                                                                                                .and(rangeOrderDate(startDate, endDate)))
                                                                                        .fetch()))
                                                                        .fetch()
                                                        )
                                                        .and(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.categoryInBrandId.goe(1L)
                                                                                .and(eqBrand(brandIdList)))
                                                                        .fetch()))
                                                        .and(eqSupplier(supplierIdList)))
                                                .fetch()
                                )
                                .and(checkLoginAdminAccountType(account))
                                .and(rangeOrderInProductDate(startDate, endDate))
                )
                .fetch();


        // 총 주문 건 수 (배송비, 도서산간 비용, 환불 같은 내용도 전부 별도의 요청 건 수로 구분한 주문 건 수)
        int totalSalesOrderCount = dashBoardOrderProducts.stream()
                .mapToInt(OrderInProduct::getQuantity)
                .sum();

        // 총 매출 금액 (배송비, 도서산간 비용, 환불 같은 내용을 전부 합한 총 매출 금액)
        int totalSalesAmount = dashBoardOrderProducts.stream()
                .mapToInt(OrderInProduct::getProductTotalAmount)
                .sum();

        // 총 매입 금액
        int totalPurchasePrice = dashBoardOrderProducts.stream()
                .mapToInt(eachOrderProduct -> {
                    // 주문 이력을 가진 제품의 매입 가격 추출
                    Integer purchasePrice = jpaQueryFactory
                            .select(product.purchasePrice)
                            .from(product)
                            .where(product.classificationCode.eq(eachOrderProduct.getProductClassificationCode()))
                            .fetchOne();

                    // 매입 가격이 존재할 경우 주문 제품 이력의 수량을 곱한 매입 가격 계산
                    if (purchasePrice != null) {
                        return purchasePrice * eachOrderProduct.getQuantity();
                    } else {
                        return 0;
                    }
                })
                .sum();

        // 중분류 카테고리 기준 주문 건 수
        List<DashBoardMiddleCategoryOrderResponseDto> totalMiddleCategoryOrderCountInfo = dashBoardOrderProducts.stream()
                .map(eachOrderProduct ->
                        // 주문 제품의 구분 코드를 기준으로 중분류 카테고리 id 추출
                        jpaQueryFactory
                                .select(categoryInBrand.category2Id)
                                .from(categoryInBrand)
                                .where(categoryInBrand.categoryInBrandId.eq(
                                                jpaQueryFactory
                                                        .select(product.categoryInBrandId)
                                                        .from(product)
                                                        .where(product.classificationCode.eq(eachOrderProduct.getProductClassificationCode()))
                                                        .fetchOne()
                                        )
                                )
                                .groupBy(categoryInBrand.category2Id)
                                .fetchOne()
                )
                .distinct()
                .map(eachOrderProductMiddleCategoryId -> {
                    Tuple categoryInfo = jpaQueryFactory
                            .select(category.categoryId, category.categoryName)
                            .from(category)
                            .where(category.categoryId.eq(eachOrderProductMiddleCategoryId))
                            .fetchOne();

                    if (categoryInfo != null) {
                        Integer categoryOrderCount = jpaQueryFactory
                                .select(orderInProduct.quantity.sum())
                                .from(orderInProduct)
                                .where(orderInProduct.productClassificationCode.in(
                                                jpaQueryFactory
                                                        .select(product.classificationCode)
                                                        .from(product)
                                                        .where(product.categoryInBrandId.in(
                                                                        jpaQueryFactory
                                                                                .select(categoryInBrand.categoryInBrandId)
                                                                                .from(categoryInBrand)
                                                                                .where(categoryInBrand.category2Id.eq(eachOrderProductMiddleCategoryId))
                                                                                .fetch()
                                                                )
                                                        )
                                                        .groupBy(product.classificationCode)
                                                        .fetch()
                                        )
                                )
                                .fetchOne();

                        if (categoryOrderCount == null || categoryOrderCount == 0) {
                            return DashBoardMiddleCategoryOrderResponseDto.builder()
                                    .categoryId(categoryInfo.get(category.categoryId))
                                    .categoryName(categoryInfo.get(category.categoryName))
                                    .categoryOrderCount(0L)
                                    .build();
                        } else {
                            return DashBoardMiddleCategoryOrderResponseDto.builder()
                                    .categoryId(categoryInfo.get(category.categoryId))
                                    .categoryName(categoryInfo.get(category.categoryName))
                                    .categoryOrderCount(categoryOrderCount.longValue())
                                    .build();
                        }

                    } else {
                        return null;
                    }
                })
                .sorted(
                        Comparator.comparing(
                                (DashBoardMiddleCategoryOrderResponseDto dashBoardMiddleCategoryOrderResponseDto) -> dashBoardMiddleCategoryOrderResponseDto != null ? dashBoardMiddleCategoryOrderResponseDto.getCategoryOrderCount() : null
                        ).reversed()
                )
                .collect(Collectors.toList());

        // 대분류 카테고리 기준 주문 건 수
        List<DashBoardUpCategoryOrderResponseDto> totalUpCategoryOrderCountInfo = dashBoardOrderProducts.stream()
                .map(eachOrderProduct ->
                        jpaQueryFactory
                                .select(categoryInBrand.category1Id)
                                .from(categoryInBrand)
                                .where(categoryInBrand.categoryInBrandId.eq(
                                                jpaQueryFactory
                                                        .select(product.categoryInBrandId)
                                                        .from(product)
                                                        .where(product.classificationCode.eq(eachOrderProduct.getProductClassificationCode()))
                                                        .fetchOne()
                                        )
                                )
                                .groupBy(categoryInBrand.category1Id)
                                .fetchOne())
                .distinct()
                .map(eachOrderProductUpCategoryId -> {

                    Tuple categoryInfo = jpaQueryFactory
                            .select(category.categoryId, category.categoryName)
                            .from(category)
                            .where(category.categoryId.eq(eachOrderProductUpCategoryId))
                            .fetchOne();

                    if (categoryInfo != null) {
                        Integer categoryOrderCount = jpaQueryFactory
                                .select(orderInProduct.quantity.sum())
                                .from(orderInProduct)
                                .where(orderInProduct.productClassificationCode.in(
                                                jpaQueryFactory
                                                        .select(product.classificationCode)
                                                        .from(product)
                                                        .where(product.categoryInBrandId.in(
                                                                        jpaQueryFactory
                                                                                .select(categoryInBrand.categoryInBrandId)
                                                                                .from(categoryInBrand)
                                                                                .where(categoryInBrand.category1Id.eq(eachOrderProductUpCategoryId))
                                                                                .fetch()
                                                                )
                                                        )
                                                        .groupBy(product.classificationCode)
                                                        .fetch()
                                        )
                                )
                                .fetchOne();

                        if (categoryOrderCount == null || categoryOrderCount == 0) {
                            return DashBoardUpCategoryOrderResponseDto.builder()
                                    .categoryId(categoryInfo.get(category.categoryId))
                                    .categoryName(categoryInfo.get(category.categoryName))
                                    .categoryOrderCount(0L)
                                    .build();
                        } else {
                            return DashBoardUpCategoryOrderResponseDto.builder()
                                    .categoryId(categoryInfo.get(category.categoryId))
                                    .categoryName(categoryInfo.get(category.categoryName))
                                    .categoryOrderCount(categoryOrderCount.longValue())
                                    .build();
                        }

                    } else {
                        return null;
                    }
                })
                .sorted(
                        Comparator.comparing(
                                (DashBoardUpCategoryOrderResponseDto dashBoardUpCategoryOrderResponseDto) -> dashBoardUpCategoryOrderResponseDto != null ? dashBoardUpCategoryOrderResponseDto.getCategoryOrderCount() : null
                        ).reversed()
                )
                .collect(Collectors.toList());


        // 월별 총 매출 금액, 총 매출 이익 금액, 총 매출 이익률
        List<DashBoardMonthlySalesResponseDto> totalMonthlySalesSituation = getMonthlySalesInfoCase2(account, brandIdList, supplierIdList);

        // 판매 매출 상위 10 제품 정보
        List<DashBoardTop10ProductResponseDto> totalTop10ProductsInfo = dashBoardOrderProducts.stream()
                .map(eachOrderProduct ->
                        jpaQueryFactory
                                .select(product.classificationCode)
                                .from(product)
                                .where(product.classificationCode.eq(eachOrderProduct.getProductClassificationCode()))
                                .fetchOne()
                )
                .distinct()
                .map(eachTopProductClassificationCode -> {
                    Tuple topProductInfo = jpaQueryFactory
                            .select(orderInProduct.productClassificationCode,
                                    orderInProduct.productName,
                                    orderInProduct.quantity.sum(),
                                    orderInProduct.productTotalAmount.add(orderInProduct.deliveryPrice.add(orderInProduct.dangerPlacePrice)).sum()
                            )
                            .from(orderInProduct)
                            .where(orderInProduct.productClassificationCode.eq(eachTopProductClassificationCode)
                                    .and(rangeOrderInProductDate(startDate, endDate)))
                            .groupBy(orderInProduct.productClassificationCode, orderInProduct.productName)
                            .fetchOne();

                    return DashBoardTop10ProductResponseDto.builder()
                            .productClassficationCode(topProductInfo.get(orderInProduct.productClassificationCode))
                            .productName(topProductInfo.get(orderInProduct.productName))
                            .totalSalesQuantity(topProductInfo.get(orderInProduct.quantity.sum()).longValue())
                            .totalSalesAmount(topProductInfo.get(orderInProduct.productTotalAmount
                                    .add(orderInProduct.deliveryPrice.add(orderInProduct.dangerPlacePrice)).sum()).longValue())
                            .build();
                })
                .sorted(Comparator.comparing(DashBoardTop10ProductResponseDto::getTotalSalesAmount).reversed())
                .filter(eachTopProduct -> eachTopProduct.getTotalSalesQuantity() >= 1)
                .limit(10)
                .collect(Collectors.toList());


        // 취소 이력에 따른 내용 변화 반영
        List<CancleOrder> cancelOrderList = jpaQueryFactory
                .selectFrom(cancleOrder)
                .where(cancleOrder.cancelCheck.eq("Y")
                        .and(cancleOrder.productClassificationCode.in(
                                        jpaQueryFactory
                                                .select(product.classificationCode)
                                                .from(product)
                                                .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.categoryInBrandId.goe(1L)
                                                                                .and(eqBrand(brandIdList)))
                                                                        .fetch())
                                                        .and(eqSupplier(supplierIdList)))
                                                .fetch()
                                )
                        )
                        .and(checkLoginAdminAccountTypeForCancelData(account))
                        .and(rangeCancelOrderDate(startDate, endDate))
                )
                .fetch();

        int totalCancelOrderCount = 0;
        int totalCancelOrderAmount = 0;
        int cancelPurchasePrice = 0;
        double totalSalesProfitMargin = 0.0;

        if (!cancelOrderList.isEmpty()) {
            // 총 취소 수량
            totalCancelOrderCount = cancelOrderList.stream()
                    .mapToInt(CancleOrder::getCancelAmount)
                    .sum();

            // 총 취소 금액
            totalCancelOrderAmount = cancelOrderList.stream()
                    .mapToInt(eachCancelOrder -> eachCancelOrder.getCreditCanclePrice() + eachCancelOrder.getOnNuryCanclePrice())
                    .sum();

            // 총 취소 매입 금액
            cancelPurchasePrice = cancelOrderList.stream()
                    .mapToInt(eachCancelProduct -> {
                        Integer purchasePrice = jpaQueryFactory
                                .select(product.purchasePrice)
                                .from(product)
                                .where(product.classificationCode.eq(eachCancelProduct.getProductClassificationCode()))
                                .fetchOne();

                        if (purchasePrice != null) {
                            return purchasePrice * eachCancelProduct.getCancelAmount();
                        } else {
                            return 0;
                        }
                    })
                    .sum();

            // Top10 제품들 중 취소 이력이 존재 시 해당 이력 반영한 Top10 리스트
            totalTop10ProductsInfo = totalTop10ProductsInfo.stream()
                    .map(eachTop10Product -> {
                        int totalCancelCount = cancelOrderList.stream()
                                .filter(eachCancelOrder -> eachCancelOrder.getProductClassificationCode().equals(eachTop10Product.getProductClassficationCode()))
                                .mapToInt(CancleOrder::getCancelAmount)
                                .sum();

                        int totalCancelPrice = cancelOrderList.stream()
                                .filter(eachCancelOrder -> eachCancelOrder.getProductClassificationCode().equals(eachTop10Product.getProductClassficationCode()))
                                .mapToInt(eachCancelOrder -> eachCancelOrder.getCreditCanclePrice() + eachCancelOrder.getOnNuryCanclePrice())
                                .sum();

                        return DashBoardTop10ProductResponseDto.builder()
                                .productClassficationCode(eachTop10Product.getProductClassficationCode())
                                .productName(eachTop10Product.getProductName())
                                .totalSalesQuantity(eachTop10Product.getTotalSalesQuantity() - totalCancelCount)
                                .totalSalesAmount(eachTop10Product.getTotalSalesAmount() - totalCancelPrice)
                                .build();
                    })
                    .filter(eachTopProduct -> eachTopProduct.getTotalSalesQuantity() >= 1)
                    .sorted(
                            Comparator.comparing(
                                    (DashBoardTop10ProductResponseDto dashBoardTop10ProductResponseDto) -> dashBoardTop10ProductResponseDto != null ? dashBoardTop10ProductResponseDto.getTotalSalesAmount() : null
                            ).reversed()
                    )
                    .collect(Collectors.toList());


            // 중분류 카테고리 기준 취소 주문 반영 건 수
            totalMiddleCategoryOrderCountInfo = totalMiddleCategoryOrderCountInfo.stream()
                    .map(eachMiddleCategoryOrder -> {
                        Tuple categoryInfo = jpaQueryFactory
                                .select(category.categoryId, category.categoryName)
                                .from(category)
                                .where(category.categoryId.eq(eachMiddleCategoryOrder.getCategoryId()))
                                .fetchOne();

                        if (categoryInfo != null) {
                            Integer cancelOrderCount = jpaQueryFactory
                                    .select(cancleOrder.cancelAmount.sum())
                                    .from(cancleOrder)
                                    .where(cancleOrder.productClassificationCode.in(
                                                    jpaQueryFactory
                                                            .select(product.classificationCode)
                                                            .from(product)
                                                            .where(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.eq(eachMiddleCategoryOrder.getCategoryId()))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .groupBy(product.classificationCode)
                                                            .fetch()
                                            )
                                    )
                                    .fetchOne();

                            if (cancelOrderCount == null || cancelOrderCount == 0) {
                                return DashBoardMiddleCategoryOrderResponseDto.builder()
                                        .categoryId(categoryInfo.get(category.categoryId))
                                        .categoryName(categoryInfo.get(category.categoryName))
                                        .categoryOrderCount(eachMiddleCategoryOrder.getCategoryOrderCount())
                                        .build();
                            } else {
                                return DashBoardMiddleCategoryOrderResponseDto.builder()
                                        .categoryId(categoryInfo.get(category.categoryId))
                                        .categoryName(categoryInfo.get(category.categoryName))
                                        .categoryOrderCount(eachMiddleCategoryOrder.getCategoryOrderCount() - cancelOrderCount)
                                        .build();
                            }

                        } else {
                            return null;
                        }

                    })
                    .sorted(
                            Comparator.comparing(
                                    (DashBoardMiddleCategoryOrderResponseDto dashBoardMiddleCategoryOrderResponseDto) -> dashBoardMiddleCategoryOrderResponseDto != null ? dashBoardMiddleCategoryOrderResponseDto.getCategoryOrderCount() : null
                            ).reversed()
                    )
                    .collect(Collectors.toList());
        }

        // 총 매출 이익률 ((총 매출 금액 - 총 이익 금액(판매가 - 매입가)) / 총 매출 금액 = 총 매출 이익률)
        totalSalesProfitMargin = ((double) ((totalSalesAmount - totalCancelOrderAmount) - (totalPurchasePrice - cancelPurchasePrice)) / (double) (totalPurchasePrice - cancelPurchasePrice)) * 100;


        // 대분류 카테고리 기준 취소 주문 반영 건 수
        totalUpCategoryOrderCountInfo = totalUpCategoryOrderCountInfo.stream()
                .map(eachUpCategoryOrder -> {
                    Tuple categoryInfo = jpaQueryFactory
                            .select(category.categoryId, category.categoryName)
                            .from(category)
                            .where(category.categoryId.eq(eachUpCategoryOrder.getCategoryId()))
                            .fetchOne();

                    if (categoryInfo != null) {
                        Integer cancelOrderCount = jpaQueryFactory
                                .select(cancleOrder.cancelAmount.sum())
                                .from(cancleOrder)
                                .where(cancleOrder.productClassificationCode.in(
                                                jpaQueryFactory
                                                        .select(product.classificationCode)
                                                        .from(product)
                                                        .where(product.categoryInBrandId.in(
                                                                        jpaQueryFactory
                                                                                .select(categoryInBrand.categoryInBrandId)
                                                                                .from(categoryInBrand)
                                                                                .where(categoryInBrand.category1Id.eq(eachUpCategoryOrder.getCategoryId()))
                                                                                .fetch()
                                                                )
                                                        )
                                                        .groupBy(product.classificationCode)
                                                        .fetch()
                                        )
                                )
                                .fetchOne();

                        if (cancelOrderCount == null || cancelOrderCount == 0) {
                            return DashBoardUpCategoryOrderResponseDto.builder()
                                    .categoryId(categoryInfo.get(category.categoryId))
                                    .categoryName(categoryInfo.get(category.categoryName))
                                    .categoryOrderCount(eachUpCategoryOrder.getCategoryOrderCount())
                                    .build();
                        } else {
                            return DashBoardUpCategoryOrderResponseDto.builder()
                                    .categoryId(categoryInfo.get(category.categoryId))
                                    .categoryName(categoryInfo.get(category.categoryName))
                                    .categoryOrderCount(eachUpCategoryOrder.getCategoryOrderCount() - cancelOrderCount)
                                    .build();
                        }

                    } else {
                        return null;
                    }

                })
                .sorted(
                        Comparator.comparing(
                                (DashBoardUpCategoryOrderResponseDto dashBoardUpCategoryOrderResponseDto) -> dashBoardUpCategoryOrderResponseDto != null ? dashBoardUpCategoryOrderResponseDto.getCategoryOrderCount() : null
                        ).reversed()
                )
                .collect(Collectors.toList());

        return DashBoardResponseDto.builder()
                .totalSalesOrderCount((long) totalSalesOrderCount - totalCancelOrderCount)
                .totalSalesAmount((long) totalSalesAmount - totalCancelOrderAmount)
                .totalPurchasePrice((long) totalPurchasePrice - cancelPurchasePrice)
                .totalSalesProfitMargin((Math.round(totalSalesProfitMargin * 10) / 10.0) + "%")
                .totalMiddleCategoryOrderCountInfo(totalMiddleCategoryOrderCountInfo)
                .totalUpCategoryOrderCountInfo(totalUpCategoryOrderCountInfo)
                .totalMonthlySalesSituation(totalMonthlySalesSituation) //
                .totalTop10ProductsInfo(totalTop10ProductsInfo)
                .build();
    }


    // 대시보드 추출 시, 로그인한 공급사 혹은 관리자 유형에 따라 대시보드 데이터 차별화
    private BooleanExpression checkLoginAdminAccountType(AdminAccount account) {

        if (account.getType().equals("supplier")) {
            Long loginSupplierId = jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            assert loginSupplierId != null;
            return orderInProduct.supplierId.eq(loginSupplierId.toString());
        }

        return null;
    }

    // 대시보드 추출 시, 로그인한 공급사 혹은 관리자 유형에 따라 대시보드 데이터 차별화
    private BooleanExpression checkLoginAdminAccountTypeForCancelData(AdminAccount account) {

        if (account.getType().equals("supplier")) {
            Long loginSupplierId = jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            assert loginSupplierId != null;
            return cancleOrder.supplierId.eq(loginSupplierId.toString());
        }

        return null;
    }


    // 월별 총 매출 금액, 총 매출 이익 금액, 총 매출 이익률 추출
    private List<DashBoardMonthlySalesResponseDto> getMonthlySalesInfoCase2(AdminAccount account, List<Long> brandIdList, List<Long> supplierIdList) {

        List<DashBoardMonthlySalesResponseDto> totalMonthlySalesSituation = new ArrayList<>();

        LocalDateTime presentDateTime = LocalDateTime.now();

        for (int month = 11; month > -1; month--) {

            LocalDateTime prevDateTime = presentDateTime.minusMonths(month);

            List<OrderInProduct> dashBoardOrderProducts = jpaQueryFactory
                    .selectFrom(orderInProduct)
                    .where(orderInProduct.productClassificationCode.in(
                                    jpaQueryFactory.select(product.classificationCode).from(product).where(product.classificationCode.in(
                                                            jpaQueryFactory.select(orderInProduct.productClassificationCode).from(orderInProduct).where(orderInProduct.orderInProductId.goe(1L)).fetch()
                                                    )
                                                    .and(product.categoryInBrandId.in(
                                                            jpaQueryFactory
                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                    .from(categoryInBrand)
                                                                    .where(categoryInBrand.categoryInBrandId.goe(1L)
                                                                            .and(eqBrand(brandIdList)))
                                                                    .fetch()))
                                                    .and(eqSupplier(supplierIdList))
                                    ).fetch())
                            .and(checkLoginAdminAccountType(account))
                            .and(orderInProduct.createdAt.month().eq(prevDateTime.getMonthValue()).and(orderInProduct.createdAt.year().eq(prevDateTime.getYear())))
                    )
                    .fetch();

            int totalSalesOrderCount = 0; // 총 주문 건 수 (배송비, 도서산간 비용, 환불 같은 내용도 전부 별도의 요청 건 수로 구분한 주문 건 수)
            int totalSalesAmount = 0; // 총 매출 금액 (배송비, 도서산간 비용, 환불 같은 내용을 전부 합한 총 매출 금액)
            int totalPurchasePrice = 0;
            int totalProfitAmount = 0; // 총 이익 금액
            double totalSalesProfitMargin = 0; // 총 매출 이익률 ((총 매출 금액 - 총 이익 금액(판매가 - 매입가)) / 총 매출 금액 = 총 매출 이익률)

            // 제품 주문 건이 존재할 경우 진입
            if (!dashBoardOrderProducts.isEmpty()) {
                // 총 주문 건 수 (배송비, 도서산간 비용, 환불 같은 내용도 전부 별도의 요청 건 수로 구분한 주문 건 수)
                totalSalesOrderCount = dashBoardOrderProducts.stream()
                        .mapToInt(OrderInProduct::getQuantity)
                        .sum();

                // 총 매출 금액 (배송비, 도서산간 비용, 환불 같은 내용을 전부 합한 총 매출 금액)
                totalSalesAmount = dashBoardOrderProducts.stream()
                        .mapToInt(OrderInProduct::getProductTotalAmount)
                        .sum();

                // 총 매입 금액
                totalPurchasePrice = dashBoardOrderProducts.stream()
                        .mapToInt(eachOrderProduct1 -> {
                            Integer purchasePrice1 = jpaQueryFactory
                                    .select(product.purchasePrice)
                                    .from(product)
                                    .where(product.classificationCode.eq(eachOrderProduct1.getProductClassificationCode()))
                                    .fetchOne();

                            if (purchasePrice1 != null) {
                                return purchasePrice1 * eachOrderProduct1.getQuantity();
                            } else {
                                return 0;
                            }
                        })
                        .sum();

                // 총 이익 금액
                totalProfitAmount = dashBoardOrderProducts.stream()
                        .mapToInt(eachOrderProduct -> {

                            Product orderProduct = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.classificationCode.eq(eachOrderProduct.getProductClassificationCode()))
                                    .fetchOne();

                            int profitAmount = 0;

                            if (orderProduct != null) {
                                profitAmount = (eachOrderProduct.getProductTotalAmount() - (eachOrderProduct.getQuantity() * orderProduct.getPurchasePrice())) + eachOrderProduct.getDeliveryPrice() + eachOrderProduct.getDangerPlacePrice();

                            }

                            return profitAmount;
                        })
                        .sum();

                // 총 매출 이익률 ((총 매출 금액 - 총 이익 금액(판매가 - 매입가)) / 총 매출 금액 = 총 매출 이익률)
                totalSalesProfitMargin = ((double) (totalSalesAmount - totalPurchasePrice) / (double) totalPurchasePrice) * 100;

                // 취소 이력에 따른 내용 변화 반영
                List<CancleOrder> cancelOrderList = jpaQueryFactory
                        .selectFrom(cancleOrder)
                        .where(cancleOrder.productClassificationCode.in(
                                                jpaQueryFactory
                                                        .select(product.classificationCode)
                                                        .from(product)
                                                        .where(product.categoryInBrandId.in(
                                                                        jpaQueryFactory
                                                                                .select(categoryInBrand.categoryInBrandId)
                                                                                .from(categoryInBrand)
                                                                                .where(categoryInBrand.categoryInBrandId.goe(1L)
                                                                                        .and(eqBrand(brandIdList)))
                                                                                .fetch())
                                                                .and(eqSupplier(supplierIdList)))
                                                        .fetch()
                                        )
                                        .and(checkLoginAdminAccountTypeForCancelData(account))
                                        .and(cancleOrder.cancelCheck.eq("Y"))
                                        .and(cancleOrder.cancelAt.month().eq(month).and(cancleOrder.cancelAt.year().eq(LocalDateTime.now().getYear())))
                        )
                        .fetch();

                int totalCancelOrderCount = 0;
                int totalCancelOrderAmount = 0;
                int cancelPurchasePrice = 0;
                int cancelTotalProfitAmount = 0;

                // 취소 이력이 존재할 경우 반영
                if (!cancelOrderList.isEmpty()) {

                    // 총 취소 수량
                    totalCancelOrderCount = cancelOrderList.stream()
                            .mapToInt(CancleOrder::getCancelAmount)
                            .sum();

                    // 총 취소 금액
                    totalCancelOrderAmount = cancelOrderList.stream()
                            .mapToInt(eachCancelOrder -> eachCancelOrder.getCreditCanclePrice() + eachCancelOrder.getOnNuryCanclePrice())
                            .sum();

                    // 총 취소 매입 금액
                    cancelPurchasePrice = cancelOrderList.stream()
                            .mapToInt(eachCancelProduct -> {
                                Integer purchasePrice = jpaQueryFactory
                                        .select(product.purchasePrice)
                                        .from(product)
                                        .where(product.classificationCode.eq(eachCancelProduct.getProductClassificationCode()))
                                        .fetchOne();

                                if (purchasePrice != null) {
                                    return purchasePrice * eachCancelProduct.getCancelAmount();
                                } else {
                                    return 0;
                                }
                            })
                            .sum();

                    // 총 취소 매입 금액
                    cancelTotalProfitAmount = cancelOrderList.stream()
                            .mapToInt(eachCancelOrder -> {
                                Product cancelOrderProduct = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.classificationCode.eq(eachCancelOrder.getProductClassificationCode()))
                                        .fetchOne();

                                int cancelProfitAmount = 0;

                                if (cancelOrderProduct != null) {
                                    cancelProfitAmount = eachCancelOrder.getCancelAmount() * cancelOrderProduct.getPurchasePrice();
                                }

                                return cancelProfitAmount;
                            })
                            .sum();

                    // 총 취소 매출 이익 금액을 뺀 종합 매출 이익 금액
                    totalProfitAmount = totalProfitAmount - cancelTotalProfitAmount;
                    // 총 취소 수량을 뺀 종합 판매 수량
                    totalSalesOrderCount = totalSalesOrderCount - totalCancelOrderCount;
                    // 총 취소 금액을 뺀 종합 판매 금액
                    totalSalesAmount = totalSalesAmount - totalCancelOrderAmount;
                    // 총 취소 매입 금액을 뺀 종합 매입 금액
                    totalPurchasePrice = totalPurchasePrice - cancelPurchasePrice;


                    // 총 매출 이익률 ((총 매출 금액 - 총 이익 금액(판매가 - 매입가)) / 총 매출 금액 = 총 매출 이익률)
                    totalSalesProfitMargin = (((double) (totalSalesAmount - totalPurchasePrice) / (double) totalPurchasePrice)) * 100;

                }

            }

            totalMonthlySalesSituation.add(
                    DashBoardMonthlySalesResponseDto.builder()
                            .year(prevDateTime.getYear())
                            .month(prevDateTime.getMonthValue())
                            .totalSalesOrderCount((long) totalSalesOrderCount) // 총 주문 건 수
                            .totalSalesAmount((long) totalSalesAmount) // 총 매출 금액
                            .totalPurchaseAmount((long) totalPurchasePrice) // 총 매입 금액
                            .totalSalesAmountProfit((long) totalProfitAmount) // 총 이익 금액
                            .totalSalesProfitMargin((Math.round(totalSalesProfitMargin * 10) / 10.0) + "%") // 총 매출 이익률
                            .build()
            );
        }

        return totalMonthlySalesSituation;
    }
}
