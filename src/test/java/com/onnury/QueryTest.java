//package com.onnury;
//
//
//import com.onnury.brand.domain.Brand;
//import com.onnury.category.domain.CategoryInBrand;
//import com.onnury.category.response.DownCategoryInfoResponseDto;
//import com.onnury.category.response.MiddleCategoryInfoResponseDto;
//import com.onnury.category.response.RelatedBrandResponseDto;
//import com.onnury.category.response.UpCategoryInfoResponseDto;
//import com.querydsl.core.Tuple;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import org.junit.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static com.onnury.brand.domain.QBrand.brand;
//import static com.onnury.category.domain.QCategory.category;
//import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;
//import static com.onnury.product.domain.QProduct.product;
//import static com.onnury.supplier.domain.QSupplier.supplier;
//
//@SpringBootTest
//@ExtendWith(MockitoExtension.class)
//public class QueryTest {
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Autowired
//    public JPAQueryFactory jpaQueryFactory;
//
//    @DisplayName("쿼리 테스트")
//    @Test
//    public void queryTest() {
//
//        List<CategoryInBrand> relateCategoryInBrandList = jpaQueryFactory
//                .select(product.categoryInBrandId)
//                .from(product)
//                .where(product.productId.goe(1L)
//                        .and(product.sellClassification.eq("C"))
//                )
//                .groupBy(product.categoryInBrandId)
//                .fetch()
//                .stream()
//                .map(eachCategoryInBrandId -> {
//                    CategoryInBrand existCategoryInBrandInfo = jpaQueryFactory
//                            .selectFrom(categoryInBrand)
//                            .where(categoryInBrand.categoryInBrandId.eq(eachCategoryInBrandId))
//                            .fetchOne();
//
//                    if (existCategoryInBrandInfo != null) {
//                        return existCategoryInBrandInfo;
//                    }
//
//                    return null;
//                })
//                .collect(Collectors.toList());
//
//        List<UpCategoryInfoResponseDto> allCategories = jpaQueryFactory
//                .selectFrom(category)
//                .where(category.categoryGroup.eq(0)
//                        .and(category.categoryId.eq(164L)))
//                .orderBy(category.categoryName.asc())
//                .fetch()
//                .stream()
//                .map(eachcategory -> {
//
//                    List<RelatedBrandResponseDto> relatedBrandsResult = new ArrayList<>();
//
//                    if (!relateCategoryInBrandList.isEmpty()) {
//                        List<RelatedBrandResponseDto> relatedBrands = new ArrayList<>();
//
//                        relateCategoryInBrandList.forEach(eachCategoryInBrand -> {
//                            if (eachCategoryInBrand.getCategory1Id() == eachcategory.getCategoryId()) {
//                                Brand brandInfo = jpaQueryFactory
//                                        .selectFrom(brand)
//                                        .where(brand.brandId.eq(eachCategoryInBrand.getBrandId()))
//                                        .fetchOne();
//
//                                assert brandInfo != null;
//
//                                if (relatedBrands.isEmpty() ||
//                                        relatedBrands.stream().noneMatch(eachRelateBrand -> eachRelateBrand.getBrandId().equals(brandInfo.getBrandId()))) {
//                                    relatedBrands.add(RelatedBrandResponseDto.builder()
//                                            .brandId(brandInfo.getBrandId())
//                                            .brandName(brandInfo.getBrandTitle())
//                                            .build());
//                                }
//                            }
//                        });
//
//                        if (!relatedBrands.isEmpty()) {
//                            relatedBrandsResult = relatedBrands;
//                        }
//                    }
//
//                    List<MiddleCategoryInfoResponseDto> subcategory = jpaQueryFactory
//                            .select(category.classficationCode, category.categoryId, category.categoryName)
//                            .from(category)
//                            .where(category.motherCode.eq(eachcategory.getClassficationCode()))
//                            .orderBy(category.categoryName.asc())
//                            .fetch()
//                            .stream()
//                            .map(eachcategory2 -> {
//                                List<DownCategoryInfoResponseDto> subcategory2 = jpaQueryFactory
//                                        .select(category.categoryId, category.categoryName)
//                                        .from(category)
//                                        .where(category.motherCode.eq(eachcategory2.get(category.classficationCode)))
//                                        .orderBy(category.categoryName.asc())
//                                        .fetch()
//                                        .stream()
//                                        .map(eachDownCategoryInfo ->
//                                                DownCategoryInfoResponseDto.builder()
//                                                        .downCategoryId(eachDownCategoryInfo.get(category.categoryId))
//                                                        .downCategoryName(eachDownCategoryInfo.get(category.categoryName))
//                                                        .build()
//                                        )
//                                        .collect(Collectors.toList());
//
//
//                                return MiddleCategoryInfoResponseDto.builder()
//                                        .middleCategoryId(eachcategory2.get(category.categoryId))
//                                        .middleCategoryName(eachcategory2.get(category.categoryName))
//                                        .relatedDownCategories(subcategory2)
//                                        .build();
//                            })
//                            .collect(Collectors.toList());
//
//                    return UpCategoryInfoResponseDto.builder()
//                            .upCategoryId(eachcategory.getCategoryId())
//                            .upCategoryName(eachcategory.getCategoryName())
//                            .relatedMiddleCategories(subcategory)
//                            .relatedBrands(relatedBrandsResult)
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        System.out.println("[ 가구/소품 대분류 카테고리에 속한 브랜드들 ]");
//        System.out.println(allCategories.get(0).getRelatedBrands());
//        System.out.println();
//        System.out.println("[ 가구/소품 대분류 카테고리에 속한 중분류 카테고리들 ]");
//        System.out.println(allCategories.get(0).getRelatedMiddleCategories());
//    }
//
//
//    @DisplayName("커미션 가격 테스트")
//    @Test
//    public void queryTest2() {
//        Tuple productEventInfo = jpaQueryFactory
//                .select(product.eventDescription, product.eventStartDate, product.eventEndDate, product.supplierId)
//                .from(product)
//                .where(product.productId.eq(7L))
//                .fetchOne();
//
//        Tuple supplierCommissionInfo = jpaQueryFactory
//                .select(supplier.onnuryCommission, supplier.creditCommission)
//                .from(supplier)
//                .where(supplier.supplierId.eq(productEventInfo.get(product.supplierId)))
//                .fetchOne();
//
//
//        int onnuryPay = 30;
//        int creditPay = 40 - onnuryPay;
//
//        int onnuryCommissionPrice = 0;
//
//        if (supplierCommissionInfo.get(supplier.onnuryCommission) != null) {
//            if (supplierCommissionInfo.get(supplier.onnuryCommission) > 0) {
//                onnuryCommissionPrice = (int) (onnuryPay * (supplierCommissionInfo.get(supplier.onnuryCommission) / 100));
//            }
//        }
//
//        int creditCommissionPrice = 0;
//
//        if (supplierCommissionInfo.get(supplier.creditCommission) != null) {
//            if (supplierCommissionInfo.get(supplier.creditCommission) > 0) {
//                creditCommissionPrice = (int) (creditPay * (supplierCommissionInfo.get(supplier.creditCommission) / 100));
//            }
//        }
//
//        System.out.println("온누리 가격 : " + onnuryPay);
//        System.out.println("신용 카드 가격 : " + creditPay);
//        System.out.println("공급사 온누리 수수료 % : " + supplierCommissionInfo.get(supplier.onnuryCommission));
//        System.out.println("공급사 신용카드 수수료 % : " + supplierCommissionInfo.get(supplier.creditCommission));
//        System.out.println("신용 카드 수수료 가격 : " + creditCommissionPrice);
//        System.out.println("온누리 수수료 가격 : " + onnuryCommissionPrice);
//    }
//}
