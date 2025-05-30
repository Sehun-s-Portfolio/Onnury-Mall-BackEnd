package com.onnury.query.product;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.brand.domain.Brand;
import com.onnury.brand.response.BrandDataResponseDto;
import com.onnury.brand.response.BrandResponseDto;
import com.onnury.category.domain.Category;
import com.onnury.category.domain.CategoryInBrand;
import com.onnury.category.repository.CategoryInBrandRepository;
import com.onnury.category.response.DownCategoryResponseDto;
import com.onnury.category.response.RelatedCategoryDataResponseDto;
import com.onnury.category.response.MiddleCategoryResponseDto;
import com.onnury.category.response.UpCategoryResponseDto;
import com.onnury.common.util.LogUtil;
import com.onnury.label.domain.Label;
import com.onnury.label.domain.LabelOfProduct;
import com.onnury.label.repository.LabelOfProductRepository;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.label.response.LabelResponseDto;
import com.onnury.label.response.NewReleaseProductLabelResponseDto;
import com.onnury.mapper.LabelMapper;
import com.onnury.mapper.MediaMapper;
import com.onnury.mapper.ProductMapper;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.response.MediaResponseDto;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.product.domain.*;
import com.onnury.product.repository.ProductDetailOptionRepository;
import com.onnury.product.repository.ProductOfMediaRepository;
import com.onnury.product.repository.ProductOfOptionRepository;
import com.onnury.product.repository.ProductOptionRepository;
import com.onnury.product.request.ProductDetailOptionUpdateRequestDto;
import com.onnury.product.request.ProductOptionUpdateRequestDto;
import com.onnury.product.request.ProductSearchRequestDto;
import com.onnury.product.request.ProductUpdateRequestDto;
import com.onnury.product.response.*;
import com.onnury.query.category.CategoryQueryData;
import com.onnury.supplier.domain.Supplier;
import com.onnury.supplier.response.SupplierResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.onnury.payment.domain.QPayment.payment;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;
import static com.onnury.brand.domain.QBrand.brand;
import static com.onnury.category.domain.QCategory.category;
import static com.onnury.label.domain.QLabelOfProduct.labelOfProduct;
import static com.onnury.label.domain.QLabel.label;
import static com.onnury.product.domain.QProductOption.productOption;
import static com.onnury.product.domain.QProductOfOption.productOfOption;
import static com.onnury.product.domain.QProductDetailOption.productDetailOption;
import static com.onnury.product.domain.QProductDetailInfo.productDetailInfo;
import static com.onnury.product.domain.QProductOfMedia.productOfMedia;
import static com.onnury.media.domain.QMedia.media;
import static com.onnury.supplier.domain.QSupplier.supplier;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final LabelOfProductRepository labelOfProductRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductDetailOptionRepository productDetailOptionRepository;
    private final ProductOfOptionRepository productOfOptionRepository;
    private final ProductOfMediaRepository productOfMediaRepository;
    private final CategoryInBrandRepository categoryInBrandRepository;
    private final MediaRepository mediaRepository;
    private final MediaUploadInterface mediaUploadInterface;
    private final CategoryQueryData categoryQueryData;
    private final EntityManager entityManager;

    @Resource(name = "productMapper")
    private ProductMapper productMapper;

    @Resource(name = "labelMapper")
    private LabelMapper labelMapper;

    @Resource(name = "mediaMapper")
    private MediaMapper mediaMapper;

    // 새롭게 생성된 제품의 Classification Code 생성
    public String getProductClassificationCode() {
        String year = String.valueOf(LocalDateTime.now().getYear());

        // 제품이 생성될 때 자동으로 생성될 제품 구분 코드
        StringBuilder classificationCode = new StringBuilder();

        // 제품 코드 앞 부분에 등록될 년도 정보 추출
        classificationCode.append(year.substring(2));

        // 만약 현재 월이 10월 이하일 경우
        if (LocalDateTime.now().getMonthValue() < 10) {
            String addMonthClassificationCode = "0" + LocalDateTime.now().getMonthValue();
            classificationCode.append(addMonthClassificationCode);
        } else {
            classificationCode.append(LocalDateTime.now().getMonthValue());
        }

        if (LocalDateTime.now().getDayOfMonth() < 10) {
            String addDatClassificationCode = "0" + LocalDateTime.now().getDayOfMonth();
            classificationCode.append(addDatClassificationCode);
        } else {
            classificationCode.append(LocalDateTime.now().getDayOfMonth());
        }

        // 총 제품들의 갯수 추출
        Long productCount = jpaQueryFactory
                .select(product.count())
                .from(product)
                .limit(1)
                .fetchOne();

        // 제품이 하나라도 존재할 경우 진입
        if (productCount != 0L) {

            String lastClassificationCode = jpaQueryFactory
                    .select(product.classificationCode)
                    .from(product)
                    .orderBy(product.classificationCode.desc())
                    .limit(1)
                    .fetchOne();

            assert lastClassificationCode != null;


            // 제품 갯수에 1을 더하고 자릿수에 따른 0 을 추가하기 위한 remainCode 생성
            String finalClassificationCode = String.valueOf(Integer.parseInt(lastClassificationCode.substring(6)) + 1);
            int remainCode = 5 - finalClassificationCode.length();

            // remainCode 만큼 돌려서 0 을 classificationCode에 붙이기
            for (int i = 0; i < remainCode; i++) {
                String zeroCode = "0";
                classificationCode.append(zeroCode);
            }

            // 마지막으로 제품 수량을 classificationCode에 붙이기
            classificationCode.append(finalClassificationCode);
        } else { // 제품이 아예 한 개도 존재하지 않을 경우 진입
            // 초기 제품 구분 코드 설정
            classificationCode.append("00001");
        }

        return classificationCode.toString();
    }


    // 제품 수정 처리
    @Transactional(transactionManager = "MasterTransactionManager")
    public Product updateProduct(AdminAccount loginAccount, List<MultipartFile> updateProductImgs, ProductUpdateRequestDto productUpdateRequestDto) throws IOException {

        // 수정하고자 하는 제품 호출
        Product updateProduct = jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(productUpdateRequestDto.getProductId()))
                .fetchOne();

        assert updateProduct != null;

        // 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause productClause = jpaQueryFactory
                .update(product)
                .where(product.productId.eq(productUpdateRequestDto.getProductId()));

        // 제품과 연관된 상세 정보의 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause productDetailInfoClause = jpaQueryFactory
                .update(productDetailInfo)
                .where(productDetailInfo.productId.eq(updateProduct.getProductId()));

        // 수정할 내용이 있는지 확인하기 위한 boolean 변수
        boolean existProductUpdateContent = false;
        boolean existProductDetailInfoUpdateContent = false;

        // [ Product ] 제품 고유 정보 수정
        // 제품과 매핑된 카테고리 + 브랜드 매핑 정보 수정
        // 기존에 매핑된 CategoryInBrand 호출
        CategoryInBrand updateCategoryInBrand = jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.eq(updateProduct.getCategoryInBrandId()))
                .fetchOne();

        // 카테고리나 브랜드를 수정할 때 새롭게 들어온 CategoryInBrand를 호출
        CategoryInBrand newCategoryInBrand = categoryQueryData.getCategoryInBrand(
                productUpdateRequestDto.getBrandId(), productUpdateRequestDto.getUpCategoryId(), productUpdateRequestDto.getMiddleCategoryId(), productUpdateRequestDto.getDownCategoryId());

        assert updateCategoryInBrand != null;

        // 제품에 연관된 카테고리 및 브랜드 매핑 정보가 존재하지 않을 경우 진입
        if (newCategoryInBrand == null) {
            // 제품이 등록될 카테고리에 속한 브랜드를 우선 생성
            newCategoryInBrand = categoryInBrandRepository.save(
                    CategoryInBrand.builder()
                            .brandId(productUpdateRequestDto.getBrandId())
                            .category1Id(productUpdateRequestDto.getUpCategoryId())
                            .category2Id(productUpdateRequestDto.getMiddleCategoryId())
                            .category3Id(productUpdateRequestDto.getDownCategoryId())
                            .build()
            );

            // 막 생성한 카테고리 / 브랜드 매핑 정보를 updateCategoryInBrand에 지정
            updateCategoryInBrand = newCategoryInBrand;

            // 제품 수정 여부 존재 체크
            existProductUpdateContent = true;
            // 제품과 연관된 카테고리 및 브랜드 id 정보 수정 세팅
            productClause.set(product.categoryInBrandId, updateCategoryInBrand.getCategoryInBrandId());

        } else { // 제품에 연관된 카테고리 및 브랜드 매핑 정보가 존재할 경우 진입

            // 연관된 카테고리 및 브랜드 매핑 정보들 중 하나라도 일치하지 않으면 진입
            if (updateCategoryInBrand.getBrandId() != newCategoryInBrand.getBrandId() ||
                    updateCategoryInBrand.getCategory1Id() != newCategoryInBrand.getCategory1Id() ||
                    updateCategoryInBrand.getCategory2Id() != newCategoryInBrand.getCategory2Id() ||
                    updateCategoryInBrand.getCategory3Id() != newCategoryInBrand.getCategory3Id()) {

                // 카테고리 / 브랜드 매핑 정보를 updateCategoryInBrand에 지정
                updateCategoryInBrand = newCategoryInBrand;

                // 제품 수정 여부 존재 체크
                existProductUpdateContent = true;
                // 제품과 연관된 카테고리 및 브랜드 id 정보 수정 세팅
                productClause.set(product.categoryInBrandId, updateCategoryInBrand.getCategoryInBrandId());
            }
        }

        // 제품 공급사 id 수정 세팅
        if (loginAccount.getType().equals("admin")) {
            if (productUpdateRequestDto.getSupplierId() != 0L && !Objects.equals(updateProduct.getSupplierId(), productUpdateRequestDto.getSupplierId())) {
                existProductUpdateContent = true;
                productClause.set(product.supplierId, productUpdateRequestDto.getSupplierId());
            }
        } else {
            existProductUpdateContent = true;

            productClause.set(product.supplierId, jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(loginAccount.getAdminAccountId()))
                    .fetchOne());
        }

        // 제품 명 수정 세팅
        if (!productUpdateRequestDto.getProductName().isEmpty() && !updateProduct.getProductName().equals(productUpdateRequestDto.getProductName())) {
            existProductUpdateContent = true;
            productClause.set(product.productName, productUpdateRequestDto.getProductName());
        }

        // 제품 유형 수정 세팅
        if (!productUpdateRequestDto.getDeliveryType().isEmpty() && !updateProduct.getDeliveryType().equals(productUpdateRequestDto.getDeliveryType())) {
            existProductUpdateContent = true;
            productClause.set(product.deliveryType, productUpdateRequestDto.getDeliveryType());
        }

        // 제품 판매 구분 수정 세팅
        if (!productUpdateRequestDto.getSellClassification().isEmpty() && !updateProduct.getSellClassification().equals(productUpdateRequestDto.getSellClassification())) {
            existProductUpdateContent = true;
            productClause.set(product.sellClassification, productUpdateRequestDto.getSellClassification());
        }

        // 제품 모델 명 수정 세팅
        if (!productUpdateRequestDto.getModelNumber().isEmpty() && !updateProduct.getModelNumber().equals(productUpdateRequestDto.getModelNumber())) {
            existProductUpdateContent = true;
            productClause.set(product.modelNumber, productUpdateRequestDto.getModelNumber());
        }

        // 제품 노출 여부 수정 세팅
        if (!productUpdateRequestDto.getExpressionCheck().isEmpty() && !updateProduct.getExpressionCheck().equals(productUpdateRequestDto.getExpressionCheck())) {
            existProductUpdateContent = true;
            productClause.set(product.expressionCheck, productUpdateRequestDto.getExpressionCheck());
        }

        // 제품 정상 가격 수정 세팅
        if (productUpdateRequestDto.getNormalPrice() != 0 && updateProduct.getNormalPrice() != productUpdateRequestDto.getNormalPrice()) {
            existProductUpdateContent = true;
            productClause.set(product.normalPrice, productUpdateRequestDto.getNormalPrice());
        }

        // 제품 판매 가격 수정 세팅
        if (productUpdateRequestDto.getSellPrice() != 0 && updateProduct.getSellPrice() != productUpdateRequestDto.getSellPrice()) {
            existProductUpdateContent = true;
            productClause.set(product.sellPrice, productUpdateRequestDto.getSellPrice());
        }

        // 제품 배달비 수정 세팅
        if (updateProduct.getDeliveryPrice() != productUpdateRequestDto.getDeliveryPrice()) {
            existProductUpdateContent = true;
            productClause.set(product.deliveryPrice, productUpdateRequestDto.getDeliveryPrice());
        }

        // 제품 구입 가격 수정 세팅
        if (productUpdateRequestDto.getPurchasePrice() != 0 && updateProduct.getPurchasePrice() != productUpdateRequestDto.getPurchasePrice()) {
            existProductUpdateContent = true;
            productClause.set(product.purchasePrice, productUpdateRequestDto.getPurchasePrice());
        }

        // 제품 이벤트 가격 수정 세팅
        if (updateProduct.getEventPrice() != productUpdateRequestDto.getEventPrice()) {
            existProductUpdateContent = true;
            productClause.set(product.eventPrice, productUpdateRequestDto.getEventPrice());
        }

        // 제품 제조사 수정 세팅
        if (!productUpdateRequestDto.getManufacturer().isEmpty()) {
            existProductUpdateContent = true;
            productClause.set(product.manufacturer, productUpdateRequestDto.getManufacturer());
        }

        // 제품 원산지 수정 세팅
        if (!productUpdateRequestDto.getMadeInOrigin().isEmpty()) {
            existProductUpdateContent = true;
            productClause.set(product.madeInOrigin, productUpdateRequestDto.getMadeInOrigin());
        }

        // 제품 위탁점 수정 세팅
        if (!productUpdateRequestDto.getConsignmentStore().isEmpty()) {
            existProductUpdateContent = true;
            productClause.set(product.consignmentStore, productUpdateRequestDto.getConsignmentStore());
        }

        // 제품 메모 수정 세팅
        if (!productUpdateRequestDto.getMemo().isEmpty()) {
            existProductUpdateContent = true;
            productClause.set(product.memo, productUpdateRequestDto.getMemo());
        }

        // 제품 상태 수정 세팅
        if (!productUpdateRequestDto.getStatus().isEmpty()) {
            existProductUpdateContent = true;
            productClause.set(product.status, productUpdateRequestDto.getStatus());
        }

        // 제품 이벤트 날짜 수정 세팅
        if (!productUpdateRequestDto.getEventStartDate().isEmpty() && !productUpdateRequestDto.getEventEndDate().isEmpty()) {
            // 수정 요청 받은 이벤트 시작 일자, 마무리 일자를 - 기호 기준으로 잘라 리스트 화
            String[] eventStartDateSplit = productUpdateRequestDto.getEventStartDate().split("-");
            String[] eventEntDateSplit = productUpdateRequestDto.getEventEndDate().split("-");

            // 요청 받은 이벤트 시작, 마무리 일자를 DateTime으로 포맷시킬 formatter 생성
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
            // 시작 일자, 마무리 일자를 formatter에 적용하여 LocalDateTime으로 변경
            LocalDateTime updateEventStartDate = LocalDateTime.parse(productUpdateRequestDto.getEventStartDate() + " 00:00:00", formatter);
            LocalDateTime updateEventEndDate = LocalDateTime.parse(productUpdateRequestDto.getEventEndDate() + " 23:59:59", formatter);

            // 만약 제품에 기존 이벤트 시작, 마무리 일자가 존재할 경우 진입
            if (updateProduct.getEventStartDate() != null && updateProduct.getEventEndDate() != null) {

                // 기존 이벤트 시작 일자의 각 날짜 데이터와 요청 받아 변환시킨 이벤트 수정 데이터의 각 날짜 데이터가 하나라도 일치하지 않으면 진입
                if (updateProduct.getEventStartDate().getYear() != Integer.parseInt(eventStartDateSplit[0]) ||
                        updateProduct.getEventStartDate().getMonthValue() != Integer.parseInt(eventStartDateSplit[1]) ||
                        updateProduct.getEventStartDate().getDayOfMonth() != Integer.parseInt(eventStartDateSplit[2])) {
                    // 제품 수정 여부 체크
                    existProductUpdateContent = true;
                    // 이벤트 시작 일자 수정 세팅
                    productClause.set(product.eventStartDate, updateEventStartDate);
                }

                // 기존 이벤트 마무리 일자의 각 날짜 데이터와 요청 받아 변환시킨 이벤트 수정 데이터의 각 날짜 데이터가 하나라도 일치하지 않으면 진입
                if (updateProduct.getEventEndDate().getYear() != Integer.parseInt(eventEntDateSplit[0]) ||
                        updateProduct.getEventEndDate().getMonthValue() != Integer.parseInt(eventEntDateSplit[1]) ||
                        updateProduct.getEventEndDate().getDayOfMonth() != Integer.parseInt(eventEntDateSplit[2])) {
                    // 제품 수정 여부 체크
                    existProductUpdateContent = true;
                    // 이벤트 마무리 일자 수정 세팅
                    productClause.set(product.eventEndDate, updateEventEndDate);
                }

            } else { // 만약 제품에 기존 이벤트 시작, 마무리 일자가 존재하지 않을 경우 진
                // 제품 수정 여부 체크
                existProductUpdateContent = true;

                // 이벤트 시작, 마무리 일자 수정 세팅
                productClause.set(product.eventStartDate, updateEventStartDate);
                productClause.set(product.eventEndDate, updateEventEndDate);
            }

            // 제품 이벤트 비고 내용 수정 세팅
            productClause.set(product.eventDescription, productUpdateRequestDto.getEventDescription());
        }

        // 제품 옵션 사용 여부 수정 세팅
        if (!productUpdateRequestDto.getOptionCheck().isEmpty()) {

            // 수정 요청 받은 옵션 사용 여부가 Y일 경우
            if (productUpdateRequestDto.getOptionCheck().equals("Y")) {
                // 기존 N 상태에서 새롭게 Y 로 옵션 사용 여부를 지정했으면 옵션들이 새로 등록
                if (!updateProduct.getOptionCheck().equals(productUpdateRequestDto.getOptionCheck())) {
                    // 제품 수정 여부 체크
                    existProductUpdateContent = true;
                    // 제품 옵션 사용 여부 수정 세팅
                    productClause.set(product.optionCheck, productUpdateRequestDto.getOptionCheck());

                    // 최종적으로 제품과 옵션 연관 정보를 담아 한 번에 저장하기 위한 리스트 생성
                    List<ProductOfOption> saveProductOfOptionList = new ArrayList<>();

                    // 요청받은 제품의 옵션 리스트를 조회하여 옵션 정보 처리
                    productUpdateRequestDto.getProductOptionList().forEach(eachOption -> {

                        // 우선 최상위 옵션 저장
                        ProductOption saveProductOption = productOptionRepository.save(
                                ProductOption.builder()
                                        .optionTitle(eachOption.getProductOptionTitle())
                                        .necessaryCheck(eachOption.getNecessaryCheck())
                                        .build()
                        );

                        // 연관된 상세 옵션 정보들을 한 번에 저장하기 위한 리스트
                        List<ProductDetailOption> saveProductDetailOptionList = new ArrayList<>();

                        // 최상위 옵션에 해당되는 상세 옵션 내용이 존재할 경우 진입
                        if (!eachOption.getProductDetailOptionList().isEmpty()) {
                            // 각 상세 옵션 내용에 따라 리스트에 담기
                            eachOption.getProductDetailOptionList().forEach(eachDetailOption -> {
                                saveProductDetailOptionList.add(
                                        ProductDetailOption.builder()
                                                .detailOptionName(eachDetailOption.getDetailOptionName())
                                                .optionPrice(eachDetailOption.getOptionPrice())
                                                .productOption(saveProductOption)
                                                .build()
                                );
                            });

                            // ProductDetailOption 한 번에 일괄 저장
                            productDetailOptionRepository.saveAll(saveProductDetailOptionList);
                        }

                        // 제품과 옵션 연관 정보를 담은 리스트에 ProductOfOption 담기
                        saveProductOfOptionList.add(
                                ProductOfOption.builder()
                                        .productId(updateProduct.getProductId())
                                        .productOptionId(saveProductOption.getProductOptionId())
                                        .build()
                        );

                    });

                    // ProductOfOption 일괄 저장
                    productOfOptionRepository.saveAll(saveProductOfOptionList);

                } else { // 요청받은 옵션 사용 여부가 기존과 동일하게 Y인 상태에서 등록된 옵션에 대해서 수정 사항이 발생하거나 삭제한 옵션이 있을 경우 수정 처리

                    // 요청받은 옵션 리스트
                    List<Long> requestCompareProductOptionIds = productUpdateRequestDto.getProductOptionList()
                            .stream()
                            .map(ProductOptionUpdateRequestDto::getProductOptionId)
                            .collect(Collectors.toList());

                    // 요청받은 옵션 리스트 중 기존에 존재했던 옵션들 id 리스트
                    List<Long> compareProductOptionIds = requestCompareProductOptionIds.stream()
                            .filter(productOptionId -> productOptionId != 0L)
                            .collect(Collectors.toList());

                    // 이전에 존재했었던 연관된 옵션 id 리스트들
                    List<Long> updateProductOfOptions = jpaQueryFactory
                            .select(productOfOption.productOptionId)
                            .from(productOfOption)
                            .where(productOfOption.productId.eq(updateProduct.getProductId()))
                            .fetch();

                    // 새롭게 요청된 제품 옵션들 id 리스트 중 기존에 존재했지만 새롭게 수정된 요청 사항에서는 제외된 삭제될 옵션 리스트 id 추출
                    List<Long> deleteProductOptionIds = updateProductOfOptions.stream()
                            .filter(deleteProductOptionId -> !compareProductOptionIds.contains(deleteProductOptionId))
                            .collect(Collectors.toList());

                    // 만약 제품 옵션을 수정할 때 기존 옵션들 중 삭제된 옵션이 있다면 삭제 처리
                    if (!deleteProductOptionIds.isEmpty()) {

                        // 삭제할 제품 옵션들을 조회하며 관련 데이터들 삭제 처리 진행
                        deleteProductOptionIds.forEach(deleteProductOption -> {

                            // 삭제할 제품 옵션 호출
                            ProductOption deletePrevProductOption = jpaQueryFactory
                                    .selectFrom(productOption)
                                    .where(productOption.productOptionId.eq(deleteProductOption))
                                    .fetchOne();

                            assert deletePrevProductOption != null;

                            // 연관된 상세 옵션 삭제
                            jpaQueryFactory
                                    .delete(productDetailOption)
                                    .where(productDetailOption.productOption.eq(deletePrevProductOption))
                                    .execute();

                            // 연관된 제품 매핑 옵션 정보 삭제
                            jpaQueryFactory
                                    .delete(productOfOption)
                                    .where(productOfOption.productOptionId.eq(deletePrevProductOption.getProductOptionId()))
                                    .execute();

                            // 제품 옵션 삭제
                            jpaQueryFactory
                                    .delete(productOption)
                                    .where(productOption.productOptionId.eq(deletePrevProductOption.getProductOptionId()))
                                    .execute();
                        });
                    }

                    // 수정 요청 받은 제품 옵션 리스트를 조회하며 옵션 관련 데이터들 수정 처리
                    productUpdateRequestDto.getProductOptionList().forEach(eachUpdateProductOption -> {

                        // 제품 옵션 수정 여부 Atomic 변수 생성
                        AtomicBoolean existProductOptionUpdateContent = new AtomicBoolean(false);

                        // 만약 기존에 존재하던 상세 옵션을 수정할 경우
                        if (eachUpdateProductOption.getProductOptionId() != 0L) {
                            // 수정할 제품 옵션 호출
                            ProductOption updateProductOption = jpaQueryFactory
                                    .selectFrom(productOption)
                                    .where(productOption.productOptionId.eq(eachUpdateProductOption.getProductOptionId()))
                                    .fetchOne();

                            // 제품 옵션을 동적으로 수정하기 위한 JPAUpdateClause 생성
                            JPAUpdateClause productOptionClause = jpaQueryFactory
                                    .update(productOption)
                                    .where(productOption.productOptionId.eq(eachUpdateProductOption.getProductOptionId()));

                            assert updateProductOption != null;

                            // 최상위 옵션 타이틀 수정 세팅
                            if (!eachUpdateProductOption.getProductOptionTitle().isEmpty() && !updateProductOption.getOptionTitle().equals(eachUpdateProductOption.getProductOptionTitle())) {
                                existProductOptionUpdateContent.set(true);
                                productOptionClause.set(productOption.optionTitle, eachUpdateProductOption.getProductOptionTitle());
                            }

                            // 최상위 옵션 선택 필수 유무 수정 세팅
                            if (!eachUpdateProductOption.getNecessaryCheck().isEmpty() && !updateProductOption.getNecessaryCheck().equals(eachUpdateProductOption.getNecessaryCheck())) {
                                existProductOptionUpdateContent.set(true);
                                productOptionClause.set(productOption.necessaryCheck, eachUpdateProductOption.getNecessaryCheck());
                            }

                            // 수정할 제품 옵션의 상세 옵션 리스트가 존재할 경우 진입
                            if (!eachUpdateProductOption.getProductDetailOptionList().isEmpty()) {

                                // 요청받은 옵션 리스트 중 기존에 존재했던 옵션들 id 리스트
                                List<Long> compareProductDetailOptionIds = eachUpdateProductOption.getProductDetailOptionList()
                                        .stream()
                                        .map(ProductDetailOptionUpdateRequestDto::getDetailOptionId)
                                        .filter(productDetailOptionId -> productDetailOptionId != 0L)
                                        .collect(Collectors.toList());

                                // 기존 옵션들이 존재할 경우 진입
                                if (!compareProductDetailOptionIds.isEmpty()) {
                                    // 이전에 존재했었던 연관된 옵션 id 리스트들
                                    List<Long> prevRemainProductDetailOptionIds = jpaQueryFactory
                                            .select(productDetailOption.productDetailOptionId)
                                            .from(productDetailOption)
                                            .where(productDetailOption.productOption.eq(updateProductOption))
                                            .fetch();

                                    // 새롭게 요청된 제품 옵션들 id 리스트 중 기존에 존재했지만 새롭게 수정된 요청 사항에서는 제외된 삭제될 옵션 리스트 id 추출
                                    List<Long> deleteProductDetailOptionIds = prevRemainProductDetailOptionIds.stream()
                                            .filter(deleteProductDetailOptionId -> !compareProductDetailOptionIds.contains(deleteProductDetailOptionId))
                                            .collect(Collectors.toList());

                                    // 만약 제품 옵션을 수정할 때 기존 상세 옵션들 중 삭제된 상세 옵션이 있다면 삭제 처리
                                    if (!deleteProductDetailOptionIds.isEmpty()) {
                                        deleteProductDetailOptionIds.forEach(deleteProductDetailOption -> {

                                            // 상세 옵션 삭제
                                            jpaQueryFactory
                                                    .delete(productDetailOption)
                                                    .where(productDetailOption.productOption.eq(updateProductOption)
                                                            .and(productDetailOption.productDetailOptionId.eq(deleteProductDetailOption)))
                                                    .execute();

                                        });
                                    }
                                }

                                // 수정 요청 받은 제품 옵션의 상세 옵션 리스트들을 조회하며 상세 옵션 정보들을 수정 처리
                                eachUpdateProductOption.getProductDetailOptionList().forEach(eachUpdateProductDetailOption -> {

                                    // 제품 상세 옵션 수정 여부 Atomic 변수 생성
                                    AtomicBoolean existProductDetailOptionUpdateContent = new AtomicBoolean(false);

                                    // 수정 요청 받은 제품 옵션의 상세 옵션이 기존에 존재하던 상세 옵션일 경우 진입
                                    if (eachUpdateProductDetailOption.getDetailOptionId() != 0L) {
                                        // 기존 상세 옵션 데이터 호출
                                        ProductDetailOption updateProductDetailOption = jpaQueryFactory
                                                .selectFrom(productDetailOption)
                                                .where(productDetailOption.productDetailOptionId.eq(eachUpdateProductDetailOption.getDetailOptionId()))
                                                .fetchOne();

                                        // 상세 옵션 정보 동적 수정을 위한 JPAUpdateClause 생성
                                        JPAUpdateClause productDetailOptionClause = jpaQueryFactory
                                                .update(productDetailOption)
                                                .where(productDetailOption.productDetailOptionId.eq(eachUpdateProductDetailOption.getDetailOptionId()));

                                        assert updateProductDetailOption != null;

                                        // 제품 상세 옵션 명 수정 세팅
                                        if (!eachUpdateProductDetailOption.getDetailOptionName().isEmpty() && !updateProductDetailOption.getDetailOptionName().equals(eachUpdateProductDetailOption.getDetailOptionName())) {
                                            existProductDetailOptionUpdateContent.set(true);
                                            productDetailOptionClause.set(productDetailOption.detailOptionName, eachUpdateProductDetailOption.getDetailOptionName());
                                        }

                                        // 제품 상세 옵션 가격 수정 세팅
                                        if (eachUpdateProductDetailOption.getOptionPrice() != 0 && updateProductDetailOption.getOptionPrice() != eachUpdateProductDetailOption.getOptionPrice()) {
                                            existProductDetailOptionUpdateContent.set(true);
                                            productDetailOptionClause.set(productDetailOption.optionPrice, eachUpdateProductDetailOption.getOptionPrice());
                                        }

                                        // 수정할 제품 옵션 컨텐츠가 존재할 경우 업데이트 실행
                                        if (existProductDetailOptionUpdateContent.get()) {
                                            log.info("상세 옵션 수정 성공");
                                            productDetailOptionClause.execute();
                                        } else {
                                            log.info("수정할 상세 옵션 내용 없음");
                                        }

                                    } else { // 수정 요청 받은 제품 옵션의 상세 옵션이 새로운 상세 옵션일 경우 진입
                                        // 각 상세 옵션 내용에 따라 리스트에 담기
                                        ProductDetailOption newProductDetailOption = ProductDetailOption.builder()
                                                .detailOptionName(eachUpdateProductDetailOption.getDetailOptionName())
                                                .optionPrice(eachUpdateProductDetailOption.getOptionPrice())
                                                .productOption(updateProductOption)
                                                .build();

                                        // 새로운 ProductDetailOption 저장
                                        productDetailOptionRepository.save(newProductDetailOption);
                                    }
                                });
                            }


                            // 수정할 제품 옵션 컨텐츠가 존재할 경우 업데이트 실행
                            if (existProductOptionUpdateContent.get()) {
                                log.info("수정 성공");
                                productOptionClause.execute();
                            } else {
                                log.info("수정할 옵션 내용 없음");
                            }

                        } else { // 새롭게 추가되는 상세 옵션의 경우 저장 처리를 진행

                            // 최종적으로 제품과 옵션 연관 정보를 담아 한 번에 저장하기 위한 리스트 생성
                            List<ProductOfOption> saveProductOfOptionList = new ArrayList<>();

                            // 우선 최상위 옵션 저장
                            ProductOption saveProductOption = productOptionRepository.save(
                                    ProductOption.builder()
                                            .optionTitle(eachUpdateProductOption.getProductOptionTitle())
                                            .necessaryCheck(eachUpdateProductOption.getNecessaryCheck())
                                            .build()
                            );

                            // 연관된 상세 옵션 정보들을 한 번에 저장하기 위한 리스트
                            List<ProductDetailOption> saveProductDetailOptionList = new ArrayList<>();

                            // 최상위 옵션에 해당되는 상세 옵션 내용이 존재할 경우 진입
                            if (!eachUpdateProductOption.getProductDetailOptionList().isEmpty()) {
                                // 각 상세 옵션 내용에 따라 리스트에 담기
                                eachUpdateProductOption.getProductDetailOptionList().forEach(eachDetailOption -> {
                                    saveProductDetailOptionList.add(
                                            ProductDetailOption.builder()
                                                    .detailOptionName(eachDetailOption.getDetailOptionName())
                                                    .optionPrice(eachDetailOption.getOptionPrice())
                                                    .productOption(saveProductOption)
                                                    .build()
                                    );
                                });

                                // ProductDetailOption 한 번에 일괄 저장
                                productDetailOptionRepository.saveAll(saveProductDetailOptionList);
                            }

                            // 제품과 옵션 연관 정보를 담은 리스트에 ProductOfOption 담기
                            saveProductOfOptionList.add(
                                    ProductOfOption.builder()
                                            .productId(updateProduct.getProductId())
                                            .productOptionId(saveProductOption.getProductOptionId())
                                            .build()
                            );

                            // ProductOfOption 일괄 저장
                            productOfOptionRepository.saveAll(saveProductOfOptionList);
                        }
                    });
                }

            } else if (productUpdateRequestDto.getOptionCheck().equals("N")) { // 수정 요청 받은 옵션 사용 유무가 N 일 경우 진입
                // 기존 옵션 활성화된 Y인 상태에서 N으로 비활성화 시 수정 진입
                if (!updateProduct.getOptionCheck().equals(productUpdateRequestDto.getOptionCheck())) {

                    // 기존에 등록된 제품 및 옵션 매핑 정보 리스트 호출
                    List<ProductOfOption> deleteProductOfOptions = jpaQueryFactory
                            .selectFrom(productOfOption)
                            .where(productOfOption.productId.eq(updateProduct.getProductId()))
                            .fetch();

                    // 만약 기존에 등록된 제품 및 옵션 매핑 정보 리스트가 존재할 경우 진입
                    if (!deleteProductOfOptions.isEmpty()) {
                        // 기존 등록 매핑 정보 리스트를 조회하며 옵션 사용 유무가 N으로 변경 되었으므로 삭제 처리
                        deleteProductOfOptions.forEach(eachDeleteRelatedProductOption -> {
                            // 삭제할 제품 옵션 호출
                            ProductOption deleteProductOption = jpaQueryFactory
                                    .selectFrom(productOption)
                                    .where(productOption.productOptionId.eq(eachDeleteRelatedProductOption.getProductOptionId()))
                                    .fetchOne();

                            assert deleteProductOption != null;

                            // 연관된 상세 옵션 삭제
                            jpaQueryFactory
                                    .delete(productDetailOption)
                                    .where(productDetailOption.productOption.eq(deleteProductOption))
                                    .execute();

                            // 연관된 제품 매핑 옵션 정보 삭제
                            jpaQueryFactory
                                    .delete(productOfOption)
                                    .where(productOfOption.productOptionId.eq(deleteProductOption.getProductOptionId()))
                                    .execute();

                            // 제품 옵션 삭제
                            jpaQueryFactory
                                    .delete(productOption)
                                    .where(productOption.productOptionId.eq(deleteProductOption.getProductOptionId()))
                                    .execute();
                        });
                    }
                }
            }

        }

        // [ ProductDetailInfo ] 제품 상세 정보 수정
        // 제품 상세 정보 내용 수정
        existProductDetailInfoUpdateContent = true;
        productDetailInfoClause.set(productDetailInfo.content, productUpdateRequestDto.getProductDetailInfo());

        // [ LabelOfProduct ] 제품과 매핑된 라벨 정보 수정
        // 제품과 매핑될 라벨 리스트 수정 세팅
        if (!productUpdateRequestDto.getLabelList().isEmpty()) {
            // 수정할 제품에 해당되는 기존 라벨 및 제품 매핑 정보 리스트 호출
            List<LabelOfProduct> labelOfProducts = jpaQueryFactory
                    .selectFrom(labelOfProduct)
                    .where(labelOfProduct.productId.eq(updateProduct.getProductId()))
                    .fetch();

            // 기존에 존재했던 매핑 라벨들을 우선 삭제
            if (!labelOfProducts.isEmpty()) {
                jpaQueryFactory
                        .delete(labelOfProduct)
                        .where(labelOfProduct.productId.eq(updateProduct.getProductId()))
                        .execute();
            }

            // 새롭게 업데이트 하고자 하는 매핑 라벨들을 추가
            List<LabelOfProduct> updateLabelOfProducts = productUpdateRequestDto.getLabelList()
                    .stream()
                    .map(eachUpdateMappingLabel ->
                            LabelOfProduct.builder()
                                    .productId(updateProduct.getProductId())
                                    .labelId(eachUpdateMappingLabel)
                                    .build()
                    ).collect(Collectors.toList());

            labelOfProductRepository.saveAll(updateLabelOfProducts);
        } else {
            // 수정할 제품에 해당되는 기존 라벨 및 제품 매핑 정보 리스트 호출
            List<LabelOfProduct> labelOfProducts = jpaQueryFactory
                    .selectFrom(labelOfProduct)
                    .where(labelOfProduct.productId.eq(updateProduct.getProductId()))
                    .fetch();

            // 기존에 존재했던 매핑 라벨들을 우선 삭제
            if (!labelOfProducts.isEmpty()) {
                jpaQueryFactory
                        .delete(labelOfProduct)
                        .where(labelOfProduct.productId.eq(updateProduct.getProductId()))
                        .execute();
            }
        }

        // 이미지 수정 (추가할 새로운 이미지들이 존재하는 경우)
        if (updateProductImgs != null) {

            // 만약 제품 수정 요청에 기존에 등록된 이미지 중 삭제될 이미지가 존재할 경우 진입
            if (!productUpdateRequestDto.getDeleteImageIds().isEmpty()) {

                // 삭제 요청받은 실제 이미지 데이터들을 호출
                List<Media> deleteMediaImages = productUpdateRequestDto.getDeleteImageIds().stream()
                        .map(eachDeleteImages ->
                                jpaQueryFactory
                                        .selectFrom(media)
                                        .where(media.mediaId.eq(eachDeleteImages))
                                        .fetchOne()
                        )
                        .collect(Collectors.toList());

                // 삭제 요청받은 실제 이미지 데이터들을 기준으로 실제로 업로드된 파일 및 연관 데이터 들까지 같이 삭제 처리
                deleteMediaImages.forEach(eachDeletePrevImage -> {

                    if (!eachDeletePrevImage.getImgUuidTitle().contains("electronics")) {
                        // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
                        File deleteImage = new File(eachDeletePrevImage.getImgUploadUrl());

                        // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                        if (deleteImage.delete()) {
                            // 매핑 정보 삭제
                            jpaQueryFactory
                                    .delete(productOfMedia)
                                    .where(productOfMedia.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                    .execute();

                            // 이미지 데이터 삭제
                            jpaQueryFactory
                                    .delete(media)
                                    .where(media.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                    .execute();
                        }
                    } else {
                        // 매핑 정보 삭제
                        jpaQueryFactory
                                .delete(productOfMedia)
                                .where(productOfMedia.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                .execute();

                        // 이미지 데이터 삭제
                        jpaQueryFactory
                                .delete(media)
                                .where(media.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                .execute();

                    }

                });

            }


            // 새로운 이미지들을 업로드 및 저장 후 해당 데이터 호출
            List<HashMap<String, String>> newUploadProductImageList = mediaUploadInterface.uploadProductImage(updateProductImgs);
            // 새롭게 저장될 Media 데이터들을 담을 리스트 생성
            List<Media> saveMediaList = new ArrayList<>();

            // 업로드한 이미지들의 정보들을 조회하여 Media 데이터 저장 처리
            for (HashMap<String, String> eachProductImageInfo : newUploadProductImageList) {
                // 새로운 이미지들의 정보들을 추출
                String imgUploadUrl = eachProductImageInfo.get("imgUploadUrl");
                String imgUrl = eachProductImageInfo.get("imgUrl");
                String imgTitle = eachProductImageInfo.get("imgTitle");
                String imgUuidTitle = eachProductImageInfo.get("imgUuidTitle");

                // 대표 이미지를 제외한 나머지 이미지 설정
                Media saveMedia = Media.builder()
                        .imgUploadUrl(imgUploadUrl)
                        .imgUrl(imgUrl)
                        .imgTitle(imgTitle)
                        .imgUuidTitle(imgUuidTitle)
                        .representCheck("N")
                        .type("product")
                        .mappingContentId(updateProduct.getProductId())
                        .build();

                saveMediaList.add(saveMedia);
            }

            // 이미지 파일들 한 번에 저장
            List<Media> createMedias = mediaRepository.saveAll(saveMediaList);

            // 연관된 제품과 이미지 파일을 저장한 정보를 담고 있는 ProductOfMedia들을 한번에 저장하기 위한 리스트
            List<ProductOfMedia> relatedProductOfMediaList = new ArrayList<>();

            List<Long> relateImgsOrder = productUpdateRequestDto.getRemainMediaIdList();

            log.info("기존 유지 이미지 id 리스트 크기 : {}", productUpdateRequestDto.getRemainMediaIdList().size());
            log.info("추가 이미지 인덱스 리스트 크기 : {}", productUpdateRequestDto.getAddImgIndexList().size());

            // 저장된 Media 정보들을 기준으로 relatedProductOfMediaList 리스트에 담기
            createMedias.forEach(eachMediaInfo -> {
                relatedProductOfMediaList.add(ProductOfMedia.builder()
                        .productId(eachMediaInfo.getMappingContentId())
                        .mediaId(eachMediaInfo.getMediaId())
                        .build());

                relateImgsOrder.add(productUpdateRequestDto.getAddImgIndexList().get(createMedias.indexOf(eachMediaInfo)), eachMediaInfo.getMediaId());
            });

            // 제품 연관 이미지들 정렬 정보 수정
            existProductUpdateContent = true;
            log.info("썸네일 순서 리스트 : {}", relateImgsOrder);
            productClause.set(product.relateImgIds, relateImgsOrder.toString());

            // ProductOfMedia 한 번에 저장
            productOfMediaRepository.saveAll(relatedProductOfMediaList);

            // 이제 제품과 연관된 이미지 데이터들 호출
            List<Media> fullProductImages = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.type.eq("product")
                            .and(media.mappingContentId.eq(updateProduct.getProductId())))
                    .orderBy(media.createdAt.asc())
                    .fetch();

            // 전체 제품 이미지들을 조회하며 요청받은 대표 이미지 인덱스 값에 따른 수정 처리
            fullProductImages.forEach(eachProductImage -> {
                // 호출한 전체 제품 이미지들의 인덱스와 요청받은 대표 이미지 인덱스 값이 같을 경우 대표 이미지 처리
                if (fullProductImages.indexOf(eachProductImage) == productUpdateRequestDto.getRepresentImageIndex()) {
                    jpaQueryFactory
                            .update(media)
                            .set(media.representCheck, "Y")
                            .where(media.mediaId.eq(fullProductImages.get(productUpdateRequestDto.getRepresentImageIndex()).getMediaId()))
                            .execute();
                } else { // 호출한 전체 제품 이미지들의 인덱스와 요청받은 대표 이미지 인덱스 값이 같지 않을 경우 일반 이미지 처리
                    if (eachProductImage.getRepresentCheck().equals("Y")) {
                        jpaQueryFactory
                                .update(media)
                                .set(media.representCheck, "N")
                                .where(media.mediaId.eq(eachProductImage.getMediaId()))
                                .execute();
                    }
                }
            });

        } else { // 새롭게 업데이트할 제품 이미지들이 존재하지 않을 경우 진입

            // 삭제 요청 기존 이미지들이 존재할 경우 진입
            if (!productUpdateRequestDto.getDeleteImageIds().isEmpty()) {

                // 요청 받은 삭제 이미지들을 호출
                List<Media> deleteMediaImages = productUpdateRequestDto.getDeleteImageIds().stream()
                        .map(eachDeleteImages ->
                                jpaQueryFactory
                                        .selectFrom(media)
                                        .where(media.mediaId.eq(eachDeleteImages))
                                        .fetchOne()
                        )
                        .collect(Collectors.toList());

                // 삭제 이미지들을 기준으로 실제 업로드된 파일과 연관된 데이터들 삭제 처리
                deleteMediaImages.forEach(eachDeletePrevImage -> {

                    if (!eachDeletePrevImage.getImgUuidTitle().contains("electronics")) {

                        // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
                        File deleteImage = new File(eachDeletePrevImage.getImgUploadUrl());

                        // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                        if (deleteImage.delete()) {
                            // 매핑 정보 삭제
                            jpaQueryFactory
                                    .delete(productOfMedia)
                                    .where(productOfMedia.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                    .execute();

                            // 이미지 데이터 삭제
                            jpaQueryFactory
                                    .delete(media)
                                    .where(media.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                    .execute();
                        }

                    } else {

                        // 매핑 정보 삭제
                        jpaQueryFactory
                                .delete(productOfMedia)
                                .where(productOfMedia.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                .execute();

                        // 이미지 데이터 삭제
                        jpaQueryFactory
                                .delete(media)
                                .where(media.mediaId.eq(eachDeletePrevImage.getMediaId()))
                                .execute();

                    }
                });

            }

            // 제품 연관 이미지들 정렬 정보 수정
            existProductUpdateContent = true;
            productClause.set(product.relateImgIds, productUpdateRequestDto.getRemainMediaIdList().toString());


            // 제품에 연관된 전체 제품 이미지들 호출
            List<Media> fullProductImages = jpaQueryFactory
                    .selectFrom(media)
                    .where(media.type.eq("product")
                            .and(media.mappingContentId.eq(updateProduct.getProductId())))
                    .orderBy(media.createdAt.asc())
                    .fetch();

            // 연관된 전체 제품 이미지들을 기준으로 대표 이미지 설정 처리
            fullProductImages.forEach(eachProductImage -> {
                // 전체 제품 이미지들의 인덱스와 요청받은 대표 이미지 인덱스 값이 같을 경우 대표 이미지 처리
                if (fullProductImages.indexOf(eachProductImage) == productUpdateRequestDto.getRepresentImageIndex()) {
                    jpaQueryFactory
                            .update(media)
                            .set(media.representCheck, "Y")
                            .where(media.mediaId.eq(fullProductImages.get(productUpdateRequestDto.getRepresentImageIndex()).getMediaId()))
                            .execute();
                } else { // 호출한 전체 제품 이미지들의 인덱스와 요청받은 대표 이미지 인덱스 값이 같지 않을 경우 일반 이미지 처리
                    if (eachProductImage.getRepresentCheck().equals("Y")) {
                        jpaQueryFactory
                                .update(media)
                                .set(media.representCheck, "N")
                                .where(media.mediaId.eq(eachProductImage.getMediaId()))
                                .execute();
                    }
                }
            });
        }

        // 수정 요청에서 수정할 기존 제품 상세 정보 이미지들의 id들이 존재할 경우 진입
        if (!productUpdateRequestDto.getProductDetailImageIds().isEmpty()) {
            // 기존 제품 상세 정보 이미지들의 id들을 기준으로 실제 이미지 데이터들 호출
            List<Media> updateProductDetailInfoImages = productUpdateRequestDto.getProductDetailImageIds().stream()
                    .map(eachUpdateProductDetailInfoImageId ->
                            jpaQueryFactory
                                    .selectFrom(media)
                                    .where(media.mediaId.eq(eachUpdateProductDetailInfoImageId))
                                    .fetchOne()
                    )
                    .collect(Collectors.toList());


            // 1. 기존에 등록되어 있고 수정 시에도 계속해서 유지 중인 기존 등록 제품 상세 정보 이미지는 아무 처리 필요 없이 그대로 유지
            // 2. 기존에 등록되어 있었지만 수정 시 삭제된 제품 상세 정보 이미지들 삭제 처리
            // 수정 요청받은 기존 상세 정보 이미지들이 존재할 경우 진입
            if (!updateProductDetailInfoImages.isEmpty()) {

                // 수정 요청받은 제품 상세 정보 이미지들 중 기존에 존재하고 유지시킬 이미지가 하나라도 존재할 경우 진입
                if (updateProductDetailInfoImages.stream()
                        .filter(eachConsistenceImage -> eachConsistenceImage.getMappingContentId() != 0L)
                        .count() != 0L) {

                    // 유지시킬 기존 상세 정보 이미지들의 id 리스트 추출
                    List<Long> consistenceImages = updateProductDetailInfoImages.stream()
                            .filter(eachConsistenceImage -> eachConsistenceImage.getMappingContentId() != 0L)
                            .map(Media::getMediaId)
                            .collect(Collectors.toList());

                    // 제품에 해당되는 제품 상세 정보 이미지 데이터들 호출
                    List<Media> prevProductDetailImages = jpaQueryFactory
                            .selectFrom(media)
                            .where(media.type.eq("productdetail")
                                    .and(media.mappingContentId.eq(updateProduct.getProductId())))
                            .fetch();

                    // 만약 제품에 해당되는 제품 상세 정보 이미지 데이터들이 존재할 경우 진입
                    if (!prevProductDetailImages.isEmpty()) {
                        // 제품 상세 정보 이미지 데이터들을 기준으로 추가 작업 처리
                        prevProductDetailImages.forEach(eachPrevProductDetailImage -> {

                            // 제품 상세 정보 이미지 데이터들을 기준으로 유지시킬 이미지 데이터들에 속하지 않을 경우 진입
                            if (!consistenceImages.contains(eachPrevProductDetailImage.getMediaId())) {
                                // 추출한 난수화 이미지 파일명을 기준으로 기존에 업로드된 삭제할 기존 이미지 파일 호출
                                File deleteImage = new File(eachPrevProductDetailImage.getImgUploadUrl());

                                // 기존에 삭제할 이미지가 해당 경로에 존재하고 삭제에 성공할 경우 진입
                                if (deleteImage.delete()) {
                                    // 매핑 정보 삭제
                                    jpaQueryFactory
                                            .delete(productOfMedia)
                                            .where(productOfMedia.mediaId.eq(eachPrevProductDetailImage.getMediaId()))
                                            .execute();

                                    // 이미지 데이터 삭제
                                    jpaQueryFactory
                                            .delete(media)
                                            .where(media.mediaId.eq(eachPrevProductDetailImage.getMediaId()))
                                            .execute();
                                }
                            }
                        });
                    }
                }

                // 3. 새롭게 추가되는 제품 상세 정보 이미지 처리
                if (updateProductDetailInfoImages.stream()
                        .filter(eachNewUpdateDetailImage -> eachNewUpdateDetailImage.getMappingContentId() == 0L)
                        .count() != 0) {

                    // 연관된 제품과 이미지 파일을 저장한 정보를 담고 있는 ProductOfMedia들을 한번에 저장하기 위한 리스트
                    List<ProductOfMedia> relatedProductOfMediaList = new ArrayList<>();

                    // 수정 시 새롭게 추가된 제품 상세 정보 이미지들 업데이트 및 ProductOfMedia 저장 처리
                    updateProductDetailInfoImages.stream()
                            .filter(eachNewUpdateDetailImage -> eachNewUpdateDetailImage.getMappingContentId() == 0L)
                            .forEach(eachNewUpdateDetailImage -> {
                                // media 연관된 제품 id 데이터 수정
                                jpaQueryFactory
                                        .update(media)
                                        .set(media.mappingContentId, updateProduct.getProductId())
                                        .where(media.mediaId.eq(eachNewUpdateDetailImage.getMediaId()))
                                        .execute();

                                entityManager.flush();
                                entityManager.clear();

                                // 수정된 media를 다시 호출
                                Media updateAdmitMedia = jpaQueryFactory
                                        .selectFrom(media)
                                        .where(media.mediaId.eq(eachNewUpdateDetailImage.getMediaId()))
                                        .fetchOne();

                                assert updateAdmitMedia != null;

                                // 수정된 media 데이터를 반환 리스트 객체에 저장
                                relatedProductOfMediaList.add(
                                        ProductOfMedia.builder()
                                                .productId(updateAdmitMedia.getMappingContentId())
                                                .mediaId(updateAdmitMedia.getMediaId())
                                                .build());
                            });

                    // ProductOfMedia 한 번에 저장
                    productOfMediaRepository.saveAll(relatedProductOfMediaList);
                }
            }
        }

        // 수정할 제품 컨텐츠가 존재할 경우 업데이트 실행
        if (existProductUpdateContent) {
            log.info("제품 정보 수정 성공");
            productClause.execute();
        } else {
            log.info("제품 정보 수정 실패");
        }

        // 수정할 제품 상세 정보 컨텐츠가 존재할 경우 업데이트 실행
        if (existProductDetailInfoUpdateContent) {
            log.info("제품 상세 정보 수정 성공");
            productDetailInfoClause.execute();
        } else {
            log.info("제품 상세 정보 수정 실패");
        }

        entityManager.flush();
        entityManager.clear();

        return jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(updateProduct.getProductId()))
                .fetchOne();
    }


    // 제품 삭제
    @Transactional(transactionManager = "MasterTransactionManager")
    public boolean deleteProduct(Long productId) {

        jpaQueryFactory
                .update(product)
                .set(product.status, "N")
                .where(product.productId.eq(productId))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return true;
    }


    // 생성된 제품 조회
    public ProductCreateResponseDto getProduct(Product getProduct, String checkNeedProductDetailInfo) {
        // 생성된 제품 호출
        Product createProduct = jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(getProduct.getProductId()))
                .fetchOne();

        assert createProduct != null;

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

        // 제품의 상세 정보 내용 추출
        if (checkNeedProductDetailInfo.equals("Y")) {
            productDetailInfoContent = jpaQueryFactory
                    .select(productDetailInfo.content)
                    .from(productDetailInfo)
                    .where(productDetailInfo.productId.eq(createProduct.getProductId()))
                    .fetchOne();
        }

        // 제품에 해당되는 이미지 및 상세 정보 이미지 추출
        List<Media> getMediaList = jpaQueryFactory
                .selectFrom(media)
                .where((media.type.eq("product").or(media.type.eq("productdetail")))
                        .and(media.mappingContentId.eq(createProduct.getProductId())))
                .fetch();


        // 제품 이미지 정보를 저장할 리스트 생성
        List<MediaResponseDto> productImageList = new ArrayList<>();
        // 제품 상세 정보 이미지를 저장할 리스트 생성
        List<ProductDetailImageInfoResponseDto> productDetailInfoImageList = new ArrayList<>();

        if (createProduct.getRelateImgIds() != null) {
            List<Long> RelateImgIdsList = convertStringToList(createProduct.getRelateImgIds());

            productImageList.addAll(
                    RelateImgIdsList.stream()
                            .map(eachRelateImage -> {
                                Media eachImage = jpaQueryFactory
                                        .selectFrom(media)
                                        .where((media.mediaId.eq(eachRelateImage))
                                                .and(media.mappingContentId.eq(createProduct.getProductId())))
                                        .fetchOne();

                                return MediaResponseDto.builder()
                                        .mediaId(eachImage.getMediaId())
                                        .imgUploadUrl(eachImage.getImgUploadUrl())
                                        .imgUrl(eachImage.getImgUrl())
                                        .imgTitle(eachImage.getImgTitle())
                                        .imgUuidTitle(eachImage.getImgUuidTitle())
                                        .representCheck(eachImage.getRepresentCheck())
                                        .build();
                            })
                            .collect(Collectors.toList())
            );
        } else {
            // 만약 제품에 연관된 이미지들이 존재할 경우 진입
            if (!getMediaList.isEmpty()) {
                getMediaList.forEach(eachMedia -> {
                    // 제품 이미지 정보 반환 데이터 저장
                    if (eachMedia.getType().equals("product")) {
                        productImageList.add(
                                MediaResponseDto.builder()
                                        .mediaId(eachMedia.getMediaId())
                                        .imgUploadUrl(eachMedia.getImgUploadUrl())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .imgTitle(eachMedia.getImgTitle())
                                        .imgUuidTitle(eachMedia.getImgUuidTitle())
                                        .representCheck(eachMedia.getRepresentCheck())
                                        .build()
                        );
                    } else if (eachMedia.getType().equals("productdetail")) {
                        // 제품 상세 정보 이미지 반환 데이터 저장
                        productDetailInfoImageList.add(
                                ProductDetailImageInfoResponseDto.builder()
                                        .productDetailImageId(eachMedia.getMediaId())
                                        .type(eachMedia.getType())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .build()
                        );
                    }
                });
            }
        }

        return ProductCreateResponseDto.builder()
                .supplierId(createProduct.getSupplierId())
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
                .eventStartDate(createProduct.getEventStartDate())
                .eventEndDate(createProduct.getEventEndDate())
                .eventDescription(createProduct.getEventDescription())
                .optionCheck(createProduct.getOptionCheck())
                .productOptionList(productOptionList)
                .productDetailInfo(productDetailInfoContent)
                .mediaList(productImageList)
                .relateImgIds(createProduct.getRelateImgIds())
                .productDetailInfoImages(productDetailInfoImageList)
                .manufacturer(createProduct.getManufacturer())
                .madeInOrigin(createProduct.getMadeInOrigin())
                .consignmentStore(createProduct.getConsignmentStore())
                .memo(createProduct.getMemo())
                .status(createProduct.getStatus())
                .build();
    }


    // 관리자 용 제품 리스트 검색 조회
    public AdminTotalProductSearchResponseDto getProductsList(AdminAccount loginAccount, ProductSearchRequestDto productSearchRequestDto) {

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
        // 검색된 제품 수
        Long totalSearchCount = 0L;

        List<Product> startEnglishProducts = new ArrayList<>();
        List<Product> startKoreanProducts = new ArrayList<>();

        // 호출된 CategoryInBrand가 존재할 시 진입
        if (!searchCategoryInBrandList.isEmpty()) {

            // 검색된 제품 수량을 input
            totalSearchCount = jpaQueryFactory
                    .select(product.count())
                    .from(product)
                    .where(product.categoryInBrandId.in(searchCategoryInBrandList)
                            .and(product.status.eq("Y"))
                            .and(eqSupplier(loginAccount, productSearchRequestDto.getSupplierId()))
                            .and(containProductNameSearchKeyword(productSearchRequestDto.getSearchKeyword())))
                    .fetchOne();

            // 리스트에 공급사 id와 검색 키워드, 호출된 CategoryInBrand와 일치한 제품들을 담기
            searchProducts = jpaQueryFactory
                    .selectFrom(product)
                    .where(product.categoryInBrandId.in(searchCategoryInBrandList)
                            .and(product.status.eq("Y"))
                            .and(eqSupplier(loginAccount, productSearchRequestDto.getSupplierId()))
                            .and(containProductNameSearchKeyword(productSearchRequestDto.getSearchKeyword())))
                    .orderBy(product.productId.desc())
                    .offset((productSearchRequestDto.getPage() * 10L) - 10)
                    .limit(10)
                    .fetch();

            /**
             .forEach(eachSortProduct -> {

             if (Pattern.matches("^[a-zA-Z]*$", eachSortProduct.getProductName())) {
             startEnglishProducts.add(eachSortProduct);
             } else {
             startKoreanProducts.add(eachSortProduct);
             }
             });
             **/

            /**
             searchProducts = startKoreanProducts;
             searchProducts.addAll(startEnglishProducts);

             if (searchProducts.size() >= 10) {
             if ((productSearchRequestDto.getPage() * 10) <= searchProducts.size()) {
             searchProducts = searchProducts.subList((productSearchRequestDto.getPage() * 10) - 10, productSearchRequestDto.getPage() * 10);
             } else {
             searchProducts = searchProducts.subList((productSearchRequestDto.getPage() * 10) - 10, searchProducts.size());
             }
             } else {
             searchProducts = searchProducts.subList((productSearchRequestDto.getPage() * 10) - 10, searchProducts.size());
             }
             **/
        }

        // 최종적으로 확인하기 위한 반환 리스트 선언
        List<ProductSearchResponseDto> getSearchProductList = new ArrayList<>();

        // 검색 제품들이 존재할 경우 진입
        if (!searchProducts.isEmpty()) {

            // 검색 제품들을 돌려 반환 객체에 맞게끔 매핑 및 Convert
            searchProducts.forEach(eachSearchProduct -> {
                // 검색 제품을 Dto 객체로 매핑하여 변환
                ProductCreateResponseDto convertProductInfo = getProduct(eachSearchProduct, "N");

                int sellOrEventPrice = 0;

                if (convertProductInfo.getEventStartDate().isBefore(LocalDateTime.now()) && convertProductInfo.getEventEndDate().isAfter(LocalDateTime.now())) {
                    sellOrEventPrice = convertProductInfo.getEventPrice();
                } else {
                    sellOrEventPrice = convertProductInfo.getSellPrice();
                }

                // 검색된 제품들 저장 리스트에 Dto 객체 정보들로 매핑하여 저장
                getSearchProductList.add(
                        ProductSearchResponseDto.builder()
                                .supplierId(convertProductInfo.getSupplierId())
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
                                .sellPrice(sellOrEventPrice)
                                .deliveryPrice(convertProductInfo.getDeliveryPrice())
                                .purchasePrice(convertProductInfo.getPurchasePrice())
                                .eventStartDate(convertProductInfo.getEventStartDate())
                                .eventEndDate(convertProductInfo.getEventEndDate())
                                .eventDescription(convertProductInfo.getEventDescription())
                                .optionCheck(convertProductInfo.getOptionCheck())
                                .productOptionList(convertProductInfo.getProductOptionList())
                                .productDetailInfo(convertProductInfo.getProductDetailInfo())
                                .mediaList(convertProductInfo.getMediaList())
                                .manufacturer(convertProductInfo.getManufacturer())
                                .madeInOrigin(convertProductInfo.getMadeInOrigin())
                                .consignmentStore(convertProductInfo.getConsignmentStore())
                                .memo(convertProductInfo.getMemo())
                                .status(convertProductInfo.getStatus())
                                .build()
                );
            });

        }

        return AdminTotalProductSearchResponseDto.builder()
                .totalSearchProductCount(totalSearchCount)
                .searchProductList(getSearchProductList)
                .build();
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
    private BooleanExpression eqSupplier(AdminAccount account, Long supplierId) {

        if (account.getType().equals("admin")) {

            // 공급사 id가 존재하고 0이 아닐 경우 진입
            if (supplierId != null && supplierId != 0L) {
                // 제품의 공급사 id 조건 적용
                return product.supplierId.eq(supplierId);
            } else {
                return null;
            }

        } else {

            Long loginSupplierId = jpaQueryFactory
                    .select(supplier.supplierId)
                    .from(supplier)
                    .where(supplier.adminAccountId.eq(account.getAdminAccountId()))
                    .fetchOne();

            return product.supplierId.eq(loginSupplierId);

        }

    }

    // 제품 명 검색 키워드 조건
    private BooleanExpression containProductNameSearchKeyword(String searchKeyword) {
        // 검색 키워드가 존재하고 한 글자라도 입력했을 경우 진입
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            String filterKeyword = searchKeyword.replaceAll("[\\[\\]]", "%").replace(" ", "%");

            // 제품 명 기준으로 검색 키워드 조건을 적용
            return product.productName.like("%" + filterKeyword + "%");
        }

        return null;
    }


    // 제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출
    public ProductReadyInfoResponseDto getReadyForCreateProductInfo() {

        // 공급사 정보 추출 후 리스트 화
        List<Tuple> supplierInfoList = jpaQueryFactory
                .select(supplier.supplierId, supplier.supplierCompany)
                .from(supplier)
                .where(supplier.status.eq("Y"))
                .orderBy(supplier.supplierCompany.asc())
                .fetch();

        // 반환 객체로 Converting
        List<SupplierResponseDto> supplierInfos = supplierInfoList.stream()
                .map(eachMapSupplier ->
                        SupplierResponseDto.builder()
                                .supplierId(eachMapSupplier.get(supplier.supplierId))
                                .supplierCompany(eachMapSupplier.get(supplier.supplierCompany))
                                .build())
                .collect(Collectors.toList());

        List<Brand> englishBrandList = new ArrayList<>();
        List<Brand> koreanBrandList = new ArrayList<>();

        jpaQueryFactory
                .selectFrom(brand)
                .where(brand.status.eq("Y"))
                .orderBy(brand.brandTitle.asc())
                .fetch()
                .forEach(eachBrand -> {
                    if (Pattern.matches("^[a-zA-Z]*$", eachBrand.getBrandTitle())) {
                        englishBrandList.add(eachBrand);
                    } else {
                        koreanBrandList.add(eachBrand);
                    }
                });

        // 국문 브랜드와 영문 브랜드 총 합친 리스트 생성
        koreanBrandList.addAll(englishBrandList);

        // 반환 객체로 Converting
        List<BrandResponseDto> brandInfos = koreanBrandList.stream()
                .map(eachMapBrand ->
                        BrandResponseDto.builder()
                                .brandId(eachMapBrand.getBrandId())
                                .brandTitle(eachMapBrand.getBrandTitle())
                                .build())
                .collect(Collectors.toList());

        // 카테고리 정보 추출 후 리스트 화
        List<Tuple> categoryInfos = jpaQueryFactory
                .select(category.categoryId,
                        category.categoryGroup,
                        category.motherCode,
                        category.classficationCode,
                        category.categoryName)
                .from(category)
                .orderBy(category.categoryId.asc())
                .fetch();

        // 상위 카테고리 반환 객체로 Converting
        List<UpCategoryResponseDto> upCategoryInfos = categoryInfos.stream()
                .filter(eachUpCategory -> eachUpCategory.get(category.categoryGroup) == 0)
                .map(eachMapUpCategory ->
                        UpCategoryResponseDto.builder()
                                .categoryId(eachMapUpCategory.get(category.categoryId))
                                .categoryGroup(eachMapUpCategory.get(category.categoryGroup))
                                .motherCode(eachMapUpCategory.get(category.motherCode))
                                .classificationCode(eachMapUpCategory.get(category.classficationCode))
                                .categoryName(eachMapUpCategory.get(category.categoryName))
                                .build()
                )
                .collect(Collectors.toList());

        // 중간 카테고리 반환 객체로 Converting
        List<MiddleCategoryResponseDto> middleCategoryInfos = categoryInfos.stream()
                .filter(eachMiddleCategory -> eachMiddleCategory.get(category.categoryGroup) == 1)
                .map(eachMapMiddleCategory ->
                        MiddleCategoryResponseDto.builder()
                                .categoryId(eachMapMiddleCategory.get(category.categoryId))
                                .categoryGroup(eachMapMiddleCategory.get(category.categoryGroup))
                                .motherCode(eachMapMiddleCategory.get(category.motherCode))
                                .classificationCode(eachMapMiddleCategory.get(category.classficationCode))
                                .categoryName(eachMapMiddleCategory.get(category.categoryName))
                                .build()
                )
                .collect(Collectors.toList());

        // 하위 카테고리 반환 객체로 Converting
        List<DownCategoryResponseDto> downCategoryInfos = categoryInfos.stream()
                .filter(eachDownCategory -> eachDownCategory.get(category.categoryGroup) == 2)
                .map(eachMapDownCategory ->
                        DownCategoryResponseDto.builder()
                                .categoryId(eachMapDownCategory.get(category.categoryId))
                                .categoryGroup(eachMapDownCategory.get(category.categoryGroup))
                                .motherCode(eachMapDownCategory.get(category.motherCode))
                                .classficationCode(eachMapDownCategory.get(category.classficationCode))
                                .categoryName(eachMapDownCategory.get(category.categoryName))
                                .build()
                )
                .collect(Collectors.toList());

        // 라벨 정보 추출 후 리스트 화
        List<Tuple> labelInfoList = jpaQueryFactory
                .select(label.labelId, label.labelTitle)
                .from(label)
                .where(label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))
                .orderBy(label.labelId.asc())
                .fetch();

        List<LabelResponseDto> labelInfos = new ArrayList<>();

        // 게시 범위 내의 라벨이 존재할 경우 반환 객체로 Converting
        if (!labelInfoList.isEmpty()) {
            labelInfos = labelInfoList.stream()
                    .map(eachLabel ->
                            LabelResponseDto.builder()
                                    .labelId(eachLabel.get(label.labelId))
                                    .labelTitle(eachLabel.get(label.labelTitle))
                                    .build())
                    .collect(Collectors.toList());
        }

        return ProductReadyInfoResponseDto.builder()
                .supplierList(supplierInfos)
                .brandList(brandInfos)
                .upCategoryList(upCategoryInfos)
                .middleCategoryList(middleCategoryInfos)
                .downCategoryList(downCategoryInfos)
                .labelList(labelInfos)
                .build();
    }


    // 메인 페이지 신 상품 리스트 호출
    public List<MainPageNewReleaseProductResponseDto> getNewReleaseProducts(String loginMemberType) throws Exception {
        log.info("메인 페이지 신 상품 리스트 호출 QueryData");

        // 신 상품 8개 정보 리스트
        List<NewReleaseProductInfo> newReleaseProducts = productMapper.getProductInfo(loginMemberType);

        // 최종적으로 반환될 페이지에 노출될 신 상품 리스트 생성
        List<MainPageNewReleaseProductResponseDto> newReleaseProductList = new ArrayList<>();

        // 추출한 신 제품들이 존재할 경우 진입
        if (!newReleaseProducts.isEmpty()) {

            // 추출한 신 제품들을 기준으로 매핑 후 반환 처리
            newReleaseProducts.forEach(eachNewReleaseProduct -> {
                try {
                    // 신 상품에 연관된 라벨 정보 리스트
                    List<NewReleaseProductLabelResponseDto> newReleaseProductLabels = labelMapper.getNewReleaseProductLabelInfo(eachNewReleaseProduct.getProduct_id());

                    // 제품의 옵션 매핑 정보 추출
                    List<NewReleaseProductOptionDto> getProductOptions = productMapper.getNewReleaseProductOptionList(eachNewReleaseProduct.getProduct_id());

                    // 신 상품 데이터들의 옵션들이 담길 리스트 생성
                    List<NewReleaseProductOptionResponseDto> productOptionList = new ArrayList<>();

                    // 제품에 매핑된 옵션 정보가 존재할 경우 진입
                    if (!getProductOptions.isEmpty()) {
                        // 매핑 옵션 정보에 따른 ProductOption, ProductDetailOption 추출 후 처리
                        getProductOptions.forEach(eachProductOption -> {
                            try {
                                List<NewReleaseProductDetailOptionDto> productDetailOptionList = productMapper.getNewReleaseProductDetailOptionList(eachProductOption.getProduct_option_id());

                                // 제품 옵션 정보를 반환 리스트 객체에 저장
                                productOptionList.add(
                                        NewReleaseProductOptionResponseDto.builder()
                                                .productOptionId(eachProductOption.getProduct_option_id())
                                                .productOptionTitle(eachProductOption.getOption_title())
                                                .necessaryCheck(eachProductOption.getNecessary_check())
                                                .productDetailOptionList(productDetailOptionList)
                                                .build()
                                );
                            } catch (Exception e) {
                                LogUtil.logException(e);
                            }
                        });
                    }

                    // 제품 이미지 정보들을 담을 리스트 생성
                    List<MediaResponseDto> productImageList = new ArrayList<>();
                    // 제품 상세 정보 이미지를 담을 리스트 생성
                    List<ProductDetailImageInfoResponseDto> productDetailImageInfoList = new ArrayList<>();

                    // 제품 정보에 연관 이미지 id 리스트 정보가 존재할 경우 해당 정보를 가지고 이미지 정보들 호출
                    if (eachNewReleaseProduct.getRelate_img_ids() != null) {
                        List<Long> relateImgIdsList = convertStringToList(eachNewReleaseProduct.getRelate_img_ids());
                        productImageList.addAll(mediaMapper.getProductImagesByRelateImgIds(eachNewReleaseProduct.getProduct_id(), relateImgIdsList));
                    } else { // 연관 이미지 id 리스트 정보가 존재하지 않을 경우 제품 id를 가지고 직접 media 테이블에서 해당 정보들 추출
                        productImageList.addAll(mediaMapper.getProductImagesByProductIdAndType(eachNewReleaseProduct.getProduct_id()));
                        productDetailImageInfoList.addAll(mediaMapper.getProductDetailInfoImageByProductIdAndType(eachNewReleaseProduct.getProduct_id()));
                    }

                    // 판매 가격 변수 생성 및 초기화
                    int sellOrEventPrice = 0;

                    // 현재 날짜가 해당 제품의 이벤트 기간에 걸친 상태라면 판매 가격을 이벤트 가격으로 설정
                    if (eachNewReleaseProduct.getEvent_start_date().isBefore(LocalDateTime.now()) && eachNewReleaseProduct.getEvent_end_date().isAfter(LocalDateTime.now())) {
                        sellOrEventPrice = eachNewReleaseProduct.getEvent_price();
                    } else { // 현재 날짜가 이벤트 기간에 포함되지 않거나 이벤트를 진행하지 않을 경우 판매 가격을 일반 판매 가격으로 설정
                        sellOrEventPrice = eachNewReleaseProduct.getSell_price();
                    }

                    // 최종 반환 리스트 객체에 저장
                    newReleaseProductList.add(
                            MainPageNewReleaseProductResponseDto.builder()
                                    .supplierId(eachNewReleaseProduct.getSupplier_id())
                                    .brandId(eachNewReleaseProduct.getBrand_id())
                                    .brand(eachNewReleaseProduct.getBrand_title())
                                    .upCategoryId(eachNewReleaseProduct.getCategory1id())
                                    .upCategory(eachNewReleaseProduct.getUp_category_name())
                                    .middleCategoryId(eachNewReleaseProduct.getCategory2id())
                                    .middleCategory(eachNewReleaseProduct.getMiddle_category_name())
                                    .downCategoryId(eachNewReleaseProduct.getCategory3id())
                                    .downCategory(eachNewReleaseProduct.getDown_category_name())
                                    .productId(eachNewReleaseProduct.getProduct_id())
                                    .productName(eachNewReleaseProduct.getProduct_name())
                                    .classificationCode(eachNewReleaseProduct.getClassification_code())
                                    .labelList(newReleaseProductLabels)
                                    .modelNumber(eachNewReleaseProduct.getModel_number())
                                    .deliveryType(eachNewReleaseProduct.getDelivery_type())
                                    .sellClassification(eachNewReleaseProduct.getSell_classification())
                                    .expressionCheck(eachNewReleaseProduct.getExpression_check())
                                    .normalPrice(eachNewReleaseProduct.getNormal_price())
                                    .sellPrice(sellOrEventPrice)
                                    .deliveryPrice(eachNewReleaseProduct.getDelivery_price())
                                    .purchasePrice(eachNewReleaseProduct.getPurchase_price())
                                    .eventStartDate(eachNewReleaseProduct.getEvent_start_date())
                                    .eventEndDate(eachNewReleaseProduct.getEvent_end_date())
                                    .eventDescription(eachNewReleaseProduct.getEvent_description())
                                    .optionCheck(eachNewReleaseProduct.getOption_check())
                                    .productOptionList(productOptionList)
                                    .productDetailInfo(eachNewReleaseProduct.getContent())
                                    .mediaList(productImageList)
                                    .manufacturer(eachNewReleaseProduct.getManufacturer())
                                    .madeInOrigin(eachNewReleaseProduct.getMade_in_origin())
                                    .consignmentStore(eachNewReleaseProduct.getConsignment_store())
                                    .memo(eachNewReleaseProduct.getMemo())
                                    .status(eachNewReleaseProduct.getStatus())
                                    .build()
                    );
                } catch (Exception e) {
                    LogUtil.logException(e);
                }
            });
        }

        return newReleaseProductList;
    }


    // 대분류 기준 제품 페이지의 정렬 기준 제품 리스트
//    public TotalProductPageMainProductResponseDto upCategoryPageMainProducts(
//            String loginMemberType, Long upCategoryId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandIdList, List<Long> labelIdList, List<Long> middleCategoryIdList) {
//
//        // 정렬된 제품 수
//        int totalCount = 0;
//
//        // 대분류 카테고리에 해당되는 CategoryInBrand 매핑 정보들 호출
//        List<Long> categoryInBrandIdList = jpaQueryFactory
//                .select(categoryInBrand.categoryInBrandId)
//                .from(categoryInBrand)
//                .where(categoryInBrand.category1Id.eq(upCategoryId)
//                        .and(eqCategoryProductBrand(brandIdList, ""))
//                        .and(eqUpCategoryInMiddleCategory(middleCategoryIdList)))
//                .fetch();
//
//        log.info("(1) 대분류 카테고리 / 브랜드 / 선택한 중분류에 해당되는 CategoryInBrand 매핑 정보들 : {}", categoryInBrandIdList.toString());
//
//        // 대분류 카테고리에 해당되는 제품들의 id 리스트 생성
//        List<Product> productsByUpCategoryList = new ArrayList<>();
//
//        // CategoryInBrand 매핑 정보들에서 정보들을 추출하여 관련된 제품 id 리스트 저장
//        categoryInBrandIdList.forEach(eachCategoryInBrandId -> {
//
//            // 고객 유형이 일반일 경우 C, A 타입의 신 제품들 추출
//            if (loginMemberType.equals("C")) {
//
//                productsByUpCategoryList.addAll(
//                        jpaQueryFactory
//                                .selectFrom(product)
//                                .where(product.categoryInBrandId.eq(eachCategoryInBrandId)
//                                        .and(product.expressionCheck.eq("Y"))
//                                        .and(product.sellClassification.eq("C"))
//                                        .and(product.status.eq("Y"))
//                                        .and(eqLabelOfProduct(labelIdList))
//                                )
//                                .fetch()
//                );
//
//            } else if (loginMemberType.equals("B")) { // 고객 유형이 기업일 경우 B, A 타입의 신 제품들 추출
//                productsByUpCategoryList.addAll(
//                        jpaQueryFactory
//                                .selectFrom(product)
//                                .where(product.categoryInBrandId.eq(eachCategoryInBrandId)
//                                        .and(product.expressionCheck.eq("Y"))
//                                        .and(product.status.eq("Y"))
//                                        .and(eqLabelOfProduct(labelIdList))
//                                )
//                                .fetch()
//                );
//            }
//        });
//
//        List<Long> productsByUpCategory = new ArrayList<>();
//
//        if (endRangePrice != 0) {
//            productsByUpCategory = productsByUpCategoryList.stream()
//                    .filter(eachProduct ->
//                            (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now()) && eachProduct.getEventPrice() >= startRangePrice && eachProduct.getEventPrice() <= endRangePrice)
//                                    || (eachProduct.getSellPrice() >= startRangePrice && eachProduct.getSellPrice() <= endRangePrice)
//                    )
//                    .map(Product::getProductId)
//                    .collect(Collectors.toList());
//        } else {
//
//            if (startRangePrice == 0) {
//                productsByUpCategory = productsByUpCategoryList.stream()
//                        .map(Product::getProductId)
//                        .collect(Collectors.toList());
//            }
//
//        }
//
//        // 실제 제품 페이지에 노출될 제품들을 담을 리스트 생성
//        List<Product> productPageMainProducts = new ArrayList<>();
//        List<LabelResponseDto> labelList = new ArrayList<>();
//        List<BrandDataResponseDto> brandList = new ArrayList<>();
//        List<RelatedCategoryDataResponseDto> middleCategoryList = new ArrayList<>();
//
//        int maxPrice = 0;
//
//        // 해당되는 제품들의 id 리스트가 존재할 경우 진입
//        if (!productsByUpCategory.isEmpty()) {
//            // 총 관련 제품들 수량
//            totalCount = productsByUpCategory.size();
//            List<Product> products = new ArrayList<>();
//
//            // 리스트에 해당되는 카테고리 제품들을 정렬 조건에 맞춰 리스트에 저장
//            if (sort <= 3) { // 1 ~ 3 : 기본적인 판매 가격 혹은 최신 순 정렬 기준
//                products = jpaQueryFactory
//                        .selectFrom(product)
//                        .where(product.productId.in(productsByUpCategory))
//                        .groupBy(product.productId)
//                        .orderBy(orderBySort(sort))
//                        .fetch();
//
//            } else { // 4 : 누적 판매 기준 순
//
//                // 판매 이력이 존재한 제품의 경우 우선 넣기
//                List<String> classificationCodeList = jpaQueryFactory
//                        .select(product.classificationCode)
//                        .from(product)
//                        .where(product.productId.in(productsByUpCategory))
//                        .groupBy(product.classificationCode)
//                        .fetch();
//
//                List<Tuple> orderInProducts = jpaQueryFactory
//                        .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
//                        .from(orderInProduct)
//                        .where(orderInProduct.productClassificationCode.in(classificationCodeList))
//                        .groupBy(orderInProduct.productClassificationCode)
//                        .orderBy(orderInProduct.productTotalAmount.sum().desc())
//                        .fetch();
//
//                if (!orderInProducts.isEmpty()) {
//                    products = orderInProducts.stream()
//                            .filter(Objects::nonNull)
//                            .map(eachOrderInProduct ->
//                                    jpaQueryFactory
//                                            .selectFrom(product)
//                                            .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode)))
//                                            .fetchOne()
//                            )
//                            .collect(Collectors.toList());
//
//                    List<Long> remainOrderInfoProductIdList = products.stream()
//                            .map(Product::getProductId)
//                            .collect(Collectors.toList());
//
//                    products.addAll(
//                            jpaQueryFactory
//                                    .selectFrom(product)
//                                    .where(product.productId.notIn(remainOrderInfoProductIdList)
//                                            .and(product.productId.in(productsByUpCategory)))
//                                    .orderBy(product.createdAt.desc())
//                                    .fetch()
//                    );
//
//                } else {
//                    products = jpaQueryFactory
//                            .selectFrom(product)
//                            .where(product.productId.in(productsByUpCategory))
//                            .groupBy(product.productId)
//                            .orderBy(product.createdAt.desc())
//                            .fetch();
//                }
//
//            }
//
//            // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
//            maxPrice = products.stream()
//                    .map(eachProduct -> {
//                        if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
//                            return eachProduct.getEventPrice();
//                        } else {
//                            return eachProduct.getSellPrice();
//                        }
//                    })
//                    .max(Integer::compare)
//                    .orElse(0);
//
//            // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
//            List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
//                    .selectFrom(categoryInBrand)
//                    .where(categoryInBrand.categoryInBrandId.in(
//                            products.stream()
//                                    .map(Product::getCategoryInBrandId)
//                                    .collect(Collectors.toList())
//                    ))
//                    .groupBy(categoryInBrand.categoryInBrandId)
//                    .fetch();
//
//            List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
//                    .map(CategoryInBrand::getBrandId)
//                    .distinct()
//                    .collect(Collectors.toList());
//
//            brandList = jpaQueryFactory
//                    .selectFrom(brand)
//                    .where(brand.brandId.in(relatedBrandIdList))
//                    .fetch()
//                    .stream()
//                    .map(eachBrand ->
//                            BrandDataResponseDto.builder()
//                                    .brandId(eachBrand.getBrandId())
//                                    .brandTitle(eachBrand.getBrandTitle())
//                                    .build()
//                    )
//                    .collect(Collectors.toList());
//
//
//            // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
//            List<Long> relatedMiddleCategoryIdList = relatedCategoryInBrandList.stream()
//                    .map(CategoryInBrand::getCategory2Id)
//                    .distinct()
//                    .collect(Collectors.toList());
//
//            middleCategoryList = jpaQueryFactory
//                    .selectFrom(category)
//                    .where(category.categoryId.in(relatedMiddleCategoryIdList))
//                    .fetch()
//                    .stream()
//                    .map(eachMiddleCategory ->
//                            RelatedCategoryDataResponseDto.builder()
//                                    .categoryId(eachMiddleCategory.getCategoryId())
//                                    .categoryName(eachMiddleCategory.getCategoryName())
//                                    .build()
//                    )
//                    .collect(Collectors.toList());
//
//
//            // [ 필터링 조건용 라벨 데이터 추출 로직 ]
//            List<Long> relatedTotalLabelList = jpaQueryFactory
//                    .select(labelOfProduct.labelId)
//                    .from(labelOfProduct)
//                    .where(labelOfProduct.productId.in(
//                            products.stream()
//                                    .map(Product::getProductId)
//                                    .collect(Collectors.toList())
//                    ))
//                    .groupBy(labelOfProduct.labelId)
//                    .fetch();
//
//            labelList = jpaQueryFactory
//                    .selectFrom(label)
//                    .where(label.labelId.in(relatedTotalLabelList)
//                            .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
//                    .fetch()
//                    .stream()
//                    .map(eachLabel ->
//                            LabelResponseDto.builder()
//                                    .labelId(eachLabel.getLabelId())
//                                    .labelTitle(eachLabel.getLabelTitle())
//                                    .build()
//                    )
//                    .collect(Collectors.toList());
//
//            if (products.size() >= 20) {
//                if ((page * 20) <= products.size()) {
//                    productPageMainProducts = products.subList((page * 20) - 20, page * 20);
//                } else {
//                    productPageMainProducts = products.subList((page * 20) - 20, products.size());
//                }
//            } else {
//                productPageMainProducts = products.subList((page * 20) - 20, products.size());
//            }
//
//        }
//
//        // 최종적으로 확인하기 위한 반환 리스트 선언
//        List<ProductPageMainProductResponseDto> getPageMainProductList = new ArrayList<>();
//
//        // 실제 제품 페이지에 노출될 제품들을 담은 리스트가 존재할 경우 진입
//        if (!productPageMainProducts.isEmpty()) {
//
//            // 저장된 제품들의 정보를 추출하여 반환 객체에 맞게끔 Converting
//            productPageMainProducts.forEach(eachProduct -> {
//                ProductCreateResponseDto convertProductInfo = getProduct(eachProduct, "N");
//
//                int sellOrEventPrice = 0;
//
//                if (convertProductInfo.getEventStartDate().isBefore(LocalDateTime.now()) && convertProductInfo.getEventEndDate().isAfter(LocalDateTime.now())) {
//                    sellOrEventPrice = convertProductInfo.getEventPrice();
//                } else {
//                    sellOrEventPrice = convertProductInfo.getSellPrice();
//                }
//
//                getPageMainProductList.add(
//                        ProductPageMainProductResponseDto.builder()
//                                .supplierId(convertProductInfo.getSupplierId())
//                                .brandId(convertProductInfo.getBrandId())
//                                .brand(convertProductInfo.getBrand())
//                                .upCategoryId(convertProductInfo.getUpCategoryId())
//                                .upCategory(convertProductInfo.getUpCategory())
//                                .middleCategoryId(convertProductInfo.getMiddleCategoryId())
//                                .middleCategory(convertProductInfo.getMiddleCategory())
//                                .downCategoryId(convertProductInfo.getDownCategoryId())
//                                .downCategory(convertProductInfo.getDownCategory())
//                                .productId(convertProductInfo.getProductId())
//                                .productName(convertProductInfo.getProductName())
//                                .classificationCode(convertProductInfo.getClassificationCode())
//                                .labelList(convertProductInfo.getLabelList())
//                                .modelNumber(convertProductInfo.getModelNumber())
//                                .deliveryType(convertProductInfo.getDeliveryType())
//                                .sellClassification(convertProductInfo.getSellClassification())
//                                .expressionCheck(convertProductInfo.getExpressionCheck())
//                                .normalPrice(convertProductInfo.getNormalPrice())
//                                .sellPrice(sellOrEventPrice)
//                                .deliveryPrice(convertProductInfo.getDeliveryPrice())
//                                .purchasePrice(convertProductInfo.getPurchasePrice())
//                                .eventStartDate(convertProductInfo.getEventStartDate())
//                                .eventEndDate(convertProductInfo.getEventEndDate())
//                                .eventDescription(convertProductInfo.getEventDescription())
//                                .optionCheck(convertProductInfo.getOptionCheck())
//                                .productOptionList(convertProductInfo.getProductOptionList())
//                                .productDetailInfo(convertProductInfo.getProductDetailInfo())
//                                .mediaList(convertProductInfo.getMediaList())
//                                .manufacturer(convertProductInfo.getManufacturer())
//                                .madeInOrigin(convertProductInfo.getMadeInOrigin())
//                                .consignmentStore(convertProductInfo.getConsignmentStore())
//                                .memo(convertProductInfo.getMemo())
//                                .status(convertProductInfo.getStatus())
//                                .build()
//                );
//            });
//        }
//
//        return TotalProductPageMainProductResponseDto.builder()
//                .totalMainProductCount(totalCount)
//                .maxPrice(maxPrice)
//                .mainProductList(getPageMainProductList)
//                .brandList(brandList)
//                .labelList(labelList)
//                .relatedUnderCategoryList(middleCategoryList)
//                .build();
//    }


    // 대분류 기준 제품 페이지의 정렬 기준 제품 리스트 v2
    public TotalProductPageMainProductResponseDto upCategoryPageMainProducts(
            HttpServletRequest request, String loginMemberType, Long upCategoryId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandIdList, List<Long> labelIdList, List<Long> middleCategoryIdList) {

        try {
            // 대분류 제품 페이지에 노출될 제품들 정보 리스트
            List<ReadyProductPageMainProductResponseDto> getReadyPageMainProductList = productMapper.getSelectUpCategoryAndConditionRelateProductList(upCategoryId, brandIdList, "", middleCategoryIdList, loginMemberType, labelIdList, startRangePrice, endRangePrice, sort, (page * 20) - 20);

            log.info("(1) 제품 리스트 호출 - 제품 조회 수량 : {}", getReadyPageMainProductList.size());

            // 호출한 대분류 제품 리스트들에 연관된 라벨, 옵션, 이미지 정보들을 호출하여 통합 객체 리스트로 매핑
            List<ProductPageMainProductResponseDto> getPageMainProductList = getReadyPageMainProductList.stream()
                    .map(eachUpCategoryProduct -> {
                        try {
                            // 대분류 제품에 연관된 라벨 정보 리스트
                            List<LabelDataResponseDto> labelList = productMapper.getEachUpCategoryProductLabelInfo(eachUpCategoryProduct.getProductId());
                            // 대분류 제품에 연관된 옵션 정보 리스트
                            List<ProductOptionCreateResponseDto> productOptionList = productMapper.getEachUpCategoryProductOptionInfo(eachUpCategoryProduct.getProductId());

                            // 옵션 정보 리스트가 하나라도 존재할 경우 진입
                            if (!productOptionList.isEmpty()) {
                                // 옵션 정보 리스트 마다 가지고 있는 상세 옵션 정보 리스트추출
                                productOptionList.forEach(eachProductOption -> {
                                    try {
                                        // 상세 옵션 정보 리스트 호출
                                        List<ProductDetailOptionCreateResponseDto> productDetailOptionList = productMapper.getEachUpCategoryProductDetailOptionInfo(eachProductOption.getProductOptionId());
                                        eachProductOption.setProductDetailOptionList(productDetailOptionList);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }

                            // 대분류 제품에 연관된 이미지 정보 리스트 호출
                            List<MediaResponseDto> mediaList = productMapper.getEachUpCategoryProductMediaInfo(eachUpCategoryProduct.getProductId());

                            // 통합 객체에 빌드하여 매핑
                            return ProductPageMainProductResponseDto.builder()
                                    .supplierId(eachUpCategoryProduct.getSupplierId())
                                    .brandId(eachUpCategoryProduct.getBrandId())
                                    .brand(eachUpCategoryProduct.getBrand())
                                    .upCategoryId(eachUpCategoryProduct.getUpCategoryId())
                                    .upCategory(eachUpCategoryProduct.getUpCategory())
                                    .middleCategoryId(eachUpCategoryProduct.getMiddleCategoryId())
                                    .middleCategory(eachUpCategoryProduct.getMiddleCategory())
                                    .downCategoryId(eachUpCategoryProduct.getDownCategoryId())
                                    .downCategory(eachUpCategoryProduct.getDownCategory())
                                    .productId(eachUpCategoryProduct.getProductId())
                                    .productName(eachUpCategoryProduct.getProductName())
                                    .classificationCode(eachUpCategoryProduct.getClassificationCode())
                                    .labelList(labelList)
                                    .modelNumber(eachUpCategoryProduct.getModelNumber())
                                    .deliveryType(eachUpCategoryProduct.getDeliveryType())
                                    .sellClassification(eachUpCategoryProduct.getSellClassification())
                                    .expressionCheck(eachUpCategoryProduct.getExpressionCheck())
                                    .normalPrice(eachUpCategoryProduct.getNormalPrice())
                                    .sellPrice(eachUpCategoryProduct.getSellPrice())
                                    .deliveryPrice(eachUpCategoryProduct.getDeliveryPrice())
                                    .purchasePrice(eachUpCategoryProduct.getPurchasePrice())
                                    .eventStartDate(eachUpCategoryProduct.getEventStartDate())
                                    .eventEndDate(eachUpCategoryProduct.getEventEndDate())
                                    .eventDescription(eachUpCategoryProduct.getEventDescription())
                                    .optionCheck(eachUpCategoryProduct.getOptionCheck())
                                    .productOptionList(productOptionList)
                                    .productDetailInfo(eachUpCategoryProduct.getProductDetailInfo())
                                    .mediaList(mediaList)
                                    .manufacturer(eachUpCategoryProduct.getManufacturer())
                                    .madeInOrigin(eachUpCategoryProduct.getMadeInOrigin())
                                    .consignmentStore(eachUpCategoryProduct.getConsignmentStore())
                                    .memo(eachUpCategoryProduct.getMemo())
                                    .status(eachUpCategoryProduct.getStatus())
                                    .build();

                        } catch (Exception e) {
                            LogUtil.logException(e, request);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            log.info("(2) 연관 정보들을 포함한 제품 리스트 호출 - 제품 조회 수량 : {}", getPageMainProductList.size());

            // 조회된 대분류 제품들의 총 갯수
            int totalCount = productMapper.getSelectUpCategoryProductsCount(loginMemberType, labelIdList, startRangePrice, endRangePrice, sort);
            log.info("(3) 조회된 대분류 제품들의 총 갯수 호출 확인 : {}", totalCount);

            // 조회된 대분류 제품들의 연관된 브랜드 리스트 정보
            List<BrandDataResponseDto> brandList = productMapper.getSelectUpCategoryProductsRelatedBrand(loginMemberType, labelIdList, startRangePrice, endRangePrice, sort);
            log.info("(4) 조회된 대분류 제품들의 연관된 브랜드 리스트 정보 확인 - 브랜드 리스트 수량 : {}", brandList.size());

            // 조회된 대분류 제품들의 연관된 중분류 카테고리 정보 리스트
            List<RelatedCategoryDataResponseDto> middleCategoryList = productMapper.getSelectUpCategoryProductsRelatedMiddleCategory(loginMemberType, labelIdList, startRangePrice, endRangePrice, sort);
            log.info("(5) 조회된 대분류 제품들과 연관된 중분류 카테고리 정보 리스트 확인 - 연관 중분류 카테고리 정보 리스트 수량 : {}", middleCategoryList.size());

            // 조회된 대분류 제품들의 연관된 라벨 정보 리스트
            List<LabelResponseDto> labelList = productMapper.getSelectUpCategoryProductsRelatedLabel(loginMemberType, labelIdList, startRangePrice, endRangePrice, sort);
            log.info("(6) 조회된 대분류 제품들의 연관된 라벨 정보 리스트 확인 - 연관 라벨 리스트 수량 : {}", labelList.size());

            // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
            int maxPrice = getPageMainProductList.stream()
                    .map(ProductPageMainProductResponseDto::getSellPrice)
                    .max(Integer::compare)
                    .orElse(0);
            log.info("(7) 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격 확인 : {}", maxPrice);


            ////////////////////////////////////////////////////////////////////////////////////////
            //                     4번 정렬 (주문 판매순) 관련 추출 로직 추가해야함                      //
            ////////////////////////////////////////////////////////////////////////////////////////

            return TotalProductPageMainProductResponseDto.builder()
                    .totalMainProductCount(totalCount)
                    .maxPrice(maxPrice)
                    .mainProductList(getPageMainProductList)
                    .brandList(brandList)
                    .labelList(labelList)
                    .relatedUnderCategoryList(middleCategoryList)
                    .build();

        } catch (Exception e) {
            LogUtil.logException(e, request);
            return null;
        }
    }


    // 제품 페이지 가격 범위 정렬 조건
    private BooleanExpression betweenPrice(int startRangePrice, int endRangePrice) {
        // 시작 가격이 0 이상일 경우 진입
        if (startRangePrice > 0) {
            // 끝 가격이 0 이상일 경우 진입
            if (endRangePrice > 0) {
                // 시작 가격과 끝 가격 사이의 판매가 조건 적용
                return product.sellPrice.between(startRangePrice, endRangePrice);
            } else { // 끝 가격이 0이거나 이하일 경우 진입
                // 시작 가격을 끝 가격으로 지정하여 판매가가 그 이하인 조건 적용
                return product.sellPrice.loe(startRangePrice);
            }
        } else { // 시작 가격이 0 이하이거나 0일 경우 진입
            // 끝 가격이 0 이상일 경우 진입
            if (endRangePrice > 0) {
                // 판매가가 0 이하인 조건 적용
                return product.sellPrice.loe(endRangePrice);
            } else { // 끝 가격이 0 이거나 그 이하일 경우 진입
                // 아무런 범위 가격이 존재 하지 않기 때문에 해당 조건 제외
                return null;
            }
        }
    }


    // 제품 이벤트 가격 범위 정렬 조건
    private BooleanExpression betweenEventPrice(int startRangePrice, int endRangePrice) {

        // 이벤트 시작 가격이 0 이상일 경우 진입
        if (startRangePrice > 0) {
            // 이벤트 끝 가격이 0 이상일 경우 진입
            if (endRangePrice > 0) {
                // 이벤트 가격 범위 내 조건 적용
                return product.eventPrice.between(startRangePrice, endRangePrice);
            } else { // 이벤트 끝 가격이 0 이거나 이하일 경우 진입
                // 이벤트 시작 가격이 끝 가격으로 적용되어 이벤트 가격이 그 이하인 조건 적용
                return product.eventPrice.loe(startRangePrice);
            }
        } else { // 이벤트 시작 가격이 0 이하이거나 0일 경우 진입
            // 이벤트 끝 가격이 0 이상일 경우 진입
            if (endRangePrice > 0) {
                // 이벤트 가격이 끝 가격 보다 이하인 조건 적용
                return product.eventPrice.loe(endRangePrice);
            } else { // 끝 가격이 0 이거나 그 이하일 경우 진입
                // 아무런 범위 가격이 존재 하지 않기 때문에 해당 조건 제외
                return null;
            }
        }
    }


    // 대분류 제품 페이지 선택한 브랜드 조건
    private BooleanExpression eqCategoryProductBrand(List<Long> brandIdList, String searchKeyword) {

        log.info("요청 브랜드 리스트 확인 : {}", brandIdList.toString());

        // 브랜드 id에 따른 데이터 호출 시 존재할 경우 진입
        if (!brandIdList.isEmpty()) {
            if (!searchKeyword.isEmpty()) {
                List<Long> realBrandIdList = jpaQueryFactory
                        .select(brand.brandId)
                        .from(brand)
                        .where(brand.brandTitle.like("%" + searchKeyword + "%")
                                .and(brand.brandId.in(brandIdList)))
                        .fetch();

                if (realBrandIdList.isEmpty()) {
                    return null;
                } else {
                    // 해당되는 카테고리와 브랜드 매핑 정보 조건 적용
                    return categoryInBrand.brandId.in(realBrandIdList);
                }
            } else {
                // 해당되는 카테고리와 브랜드 매핑 정보 조건 적용
                return categoryInBrand.brandId.in(brandIdList);
            }

        } else {

            if (!searchKeyword.isEmpty()) {
                List<Long> realBrandIdList = jpaQueryFactory
                        .select(brand.brandId)
                        .from(brand)
                        .where(brand.brandTitle.like("%" + searchKeyword + "%"))
                        .fetch();

                if (realBrandIdList.isEmpty()) {
                    return null;
                } else {
                    // 해당되는 카테고리와 브랜드 매핑 정보 조건 적용
                    return categoryInBrand.brandId.in(realBrandIdList);
                }

            } else {

                return null;
            }

        }
    }

    // 중분류 제품 페이지 선택한 브랜드 조건
    private BooleanExpression eqUpCategoryInMiddleCategory(List<Long> middleCategoryIdList) {

        log.info("요청 중분류 카테고리 리스트 확인 : {}", middleCategoryIdList.toString());

        // 중분류 카테고리 id에 따른 중분류 카테고리 데이터가 존재 시 진입
        if (!middleCategoryIdList.isEmpty()) {
            log.info("요청 카테고리 아이디가 있어서 해당 아이디들을 기준으로 추출");

            // 중분류 id를 기준으로 카테고리와 브랜드 매핑 정보 조건 적용
            return categoryInBrand.category2Id.in(middleCategoryIdList);
        } else {
            log.info("요청 카테고리 아이디가 없어 전부 추출");

            return null;
        }
    }


    // 제품 페이지 선택한 라벨 조건
    private BooleanExpression eqLabelOfProduct(List<Long> labelIdList) {
        log.info("요청 라벨 리스트 확인 : {}", labelIdList.toString());

        // 라벨 id 가 0이 아닐 경우 진입
        if (!labelIdList.isEmpty()) {
            // 라벨 id에 해당되는 라벨과 제품 매핑 정보 데이터들의 id 리스트 추출
            List<Long> labelOfProductIds = jpaQueryFactory
                    .select(labelOfProduct.productId)
                    .from(labelOfProduct)
                    .where(labelOfProduct.labelId.in(labelIdList))
                    .fetch();

            // 라벨과 제품 매핑 정보 id 리스트에 해당되는 제품 id를 가진 제품 조회 조건 적용
            return product.productId.in(labelOfProductIds);
        } else {
            return null;
        }
    }


    // 제품들 리스트 정렬 조건
    private OrderSpecifier orderBySort(int sort) {

        // 최신 순 (기본)
        if (sort == 1) {
            return product.createdAt.desc();
        } else if (sort == 2) { // 낮은 가격 순
            return product.sellPrice.asc();
        } else if (sort == 3) { // 높은 가격 순
            return product.sellPrice.desc();
        }

        return null;
    }


    // 중분류, 소분류 기준 제품 페이지의 정렬 기준 제품 리스트
    public TotalProductPageMainProductResponseDto middleAndDownCategoryPageMainProducts(
            String loginMemberType, Long categoryId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandIdList, List<Long> labelIdList, List<Long> relatedDownCategoryIdList) {

        // 정렬된 제품 수
        int totalCount = 0;

        // 요청된 카테고리 id에 따른 구분값 추출
        Integer categoryGroup = jpaQueryFactory
                .select(category.categoryGroup)
                .from(category)
                .where(category.categoryId.eq(categoryId))
                .fetchOne();

        // 중,소분류 카테고리에 해당되는 CategoryInBrand 매핑 정보들 호출
        List<Long> categoryInBrandIdList = jpaQueryFactory
                .select(categoryInBrand.categoryInBrandId)
                .from(categoryInBrand)
                .where(eqMiddleAndDownCategoryId(categoryGroup, categoryId, relatedDownCategoryIdList)
                        .and(eqCategoryProductBrand(brandIdList, "")))
                .fetch();

        // 중,소분류 카테고리에 해당되는 제품들의 id 리스트 생성
        List<Product> productsByMiddleAndDownCategoryList = new ArrayList<>();

        // CategoryInBrand 매핑 정보들에서 정보들을 추출하여 관련된 제품 id 리스트 저장
        categoryInBrandIdList.forEach(eachCategoryInBrandId -> {

            // 고객 유형이 일반일 경우 C, A 타입의 신 제품들 추출
            if (loginMemberType.equals("C")) {

                productsByMiddleAndDownCategoryList.addAll(
                        jpaQueryFactory
                                .selectFrom(product)
                                .where(product.expressionCheck.eq("Y")
                                        .and(product.categoryInBrandId.eq(eachCategoryInBrandId))
                                        .and(product.sellClassification.eq("C"))
                                        .and(product.status.eq("Y"))
                                        .and(eqLabelOfProduct(labelIdList))
                                )
                                .fetch()
                );

            } else if (loginMemberType.equals("B")) {

                // 고객 유형이 기업일 경우 B, A 타입의 신 제품들 추출
                productsByMiddleAndDownCategoryList.addAll(
                        jpaQueryFactory
                                .selectFrom(product)
                                .where(product.expressionCheck.eq("Y")
                                        .and(product.categoryInBrandId.eq(eachCategoryInBrandId))
                                        .and(product.status.eq("Y"))
                                        .and(eqLabelOfProduct(labelIdList))
                                )
                                .fetch()
                );
            }

        });

        List<Long> productsByMiddleAndDownCategory = new ArrayList<>();

        if (endRangePrice != 0) {
            productsByMiddleAndDownCategory = productsByMiddleAndDownCategoryList.stream()
                    .filter(eachProduct ->
                            (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now()) && eachProduct.getEventPrice() >= startRangePrice && eachProduct.getEventPrice() <= endRangePrice)
                                    || (eachProduct.getSellPrice() >= startRangePrice && eachProduct.getSellPrice() <= endRangePrice)
                    )
                    .map(Product::getProductId)
                    .collect(Collectors.toList());
        } else {

            if (startRangePrice == 0) {
                productsByMiddleAndDownCategory = productsByMiddleAndDownCategoryList.stream()
                        .map(Product::getProductId)
                        .collect(Collectors.toList());
            }

        }

        // 실제 제품 페이지에 노출될 제품들을 담을 리스트 생성
        List<Product> productPageMainProducts = new ArrayList<>();
        List<LabelResponseDto> labelList = new ArrayList<>();
        List<BrandDataResponseDto> brandList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> relatedCategoryList = new ArrayList<>();

        int maxPrice = 0;

        // 해당되는 제품들의 id 리스트가 존재할 경우 진입
        if (!productsByMiddleAndDownCategory.isEmpty()) {
            // 총 관련 제품들 수량
            totalCount = productsByMiddleAndDownCategory.size();

            // 리스트에 해당되는 카테고리 제품들을 정렬 조건에 맞춰 리스트에 저장
            List<Product> products = new ArrayList<>();

            // 기준 정렬에 따른 정렬
            if (sort <= 3) { // 판매 가격 혹은 최신 순
                products = jpaQueryFactory
                        .selectFrom(product)
                        .where(product.productId.in(productsByMiddleAndDownCategory))
                        .groupBy(product.productId)
                        .orderBy(orderBySort(sort))
                        .fetch();
            } else { // 판매 누적 순

                // 판매 이력이 존재한 제품의 경우 우선 넣기
                List<String> classificationCodeList = jpaQueryFactory
                        .select(product.classificationCode)
                        .from(product)
                        .where(product.productId.in(productsByMiddleAndDownCategory))
                        .groupBy(product.classificationCode)
                        .fetch();

                List<Tuple> orderInProducts = jpaQueryFactory
                        .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                        .from(orderInProduct)
                        .where(orderInProduct.productClassificationCode.in(classificationCodeList))
                        .groupBy(orderInProduct.productClassificationCode)
                        .orderBy(orderInProduct.productTotalAmount.sum().desc())
                        .fetch();

                if (!orderInProducts.isEmpty()) {
                    products = orderInProducts
                            .stream()
                            .filter(Objects::nonNull)
                            .map(eachOrderInProduct ->
                                    jpaQueryFactory
                                            .selectFrom(product)
                                            .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode)))
                                            .fetchOne()
                            )
                            .collect(Collectors.toList());

                    List<Long> remainOrderInfoProductIdList = products.stream()
                            .map(Product::getProductId)
                            .collect(Collectors.toList());

                    products.addAll(
                            jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.productId.notIn(remainOrderInfoProductIdList)
                                            .and(product.productId.in(productsByMiddleAndDownCategory)))
                                    .orderBy(product.createdAt.desc())
                                    .fetch()
                    );
                } else {
                    products = jpaQueryFactory
                            .selectFrom(product)
                            .where(product.productId.in(productsByMiddleAndDownCategory))
                            .groupBy(product.productId)
                            .orderBy(product.createdAt.desc())
                            .fetch();
                }

            }

            assert products != null;

            // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
            maxPrice = products.stream()
                    .map(eachProduct -> {
                        if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                            return eachProduct.getEventPrice();
                        } else {
                            return eachProduct.getSellPrice();
                        }
                    })
                    .max(Integer::compare)
                    .orElse(0);

            // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
            List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                    .selectFrom(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.in(
                            products.stream()
                                    .map(Product::getCategoryInBrandId)
                                    .collect(Collectors.toList())
                    ))
                    .fetch();

            List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getBrandId)
                    .distinct()
                    .collect(Collectors.toList());

            brandList = jpaQueryFactory
                    .selectFrom(brand)
                    .where(brand.brandId.in(relatedBrandIdList))
                    .fetch()
                    .stream()
                    .map(eachBrand ->
                            BrandDataResponseDto.builder()
                                    .brandId(eachBrand.getBrandId())
                                    .brandTitle(eachBrand.getBrandTitle())
                                    .build()
                    )
                    .collect(Collectors.toList());


            // [ 중분류를 선택했을 경우 중분류에 속한 소분류 카테고리 데이터 추출 로직 ]
            // # 소분류를 선택했을 시, 해당 카테고리 데이터는 없음
            if (categoryGroup == 1) {
                List<Long> relatedCategoryIdList = relatedCategoryInBrandList.stream()
                        .map(CategoryInBrand::getCategory3Id)
                        .distinct()
                        .collect(Collectors.toList());

                relatedCategoryList = jpaQueryFactory
                        .selectFrom(category)
                        .where(category.categoryId.in(relatedCategoryIdList))
                        .fetch()
                        .stream()
                        .map(eachRelatedUnderCategory ->
                                RelatedCategoryDataResponseDto.builder()
                                        .categoryId(eachRelatedUnderCategory.getCategoryId())
                                        .categoryName(eachRelatedUnderCategory.getCategoryName())
                                        .build()
                        )
                        .collect(Collectors.toList());
            }

            // [ 필터링 조건용 라벨 데이터 추출 로직 ]
            List<Long> relatedTotalLabelList = jpaQueryFactory
                    .select(labelOfProduct.labelId)
                    .from(labelOfProduct)
                    .where(labelOfProduct.productId.in(
                            products.stream()
                                    .map(Product::getProductId)
                                    .collect(Collectors.toList())
                    ))
                    .groupBy(labelOfProduct.labelId)
                    .fetch();

            labelList = jpaQueryFactory
                    .selectFrom(label)
                    .where(label.labelId.in(relatedTotalLabelList)
                            .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                    .fetch()
                    .stream()
                    .map(eachLabel ->
                            LabelResponseDto.builder()
                                    .labelId(eachLabel.getLabelId())
                                    .labelTitle(eachLabel.getLabelTitle())
                                    .build()
                    )
                    .collect(Collectors.toList());

            if (products.size() >= 20) {
                if ((page * 20) <= products.size()) {
                    productPageMainProducts = products.subList((page * 20) - 20, page * 20);
                } else {
                    productPageMainProducts = products.subList((page * 20) - 20, products.size());
                }
            } else {
                productPageMainProducts = products.subList((page * 20) - 20, products.size());
            }

        }

        // 최종적으로 확인하기 위한 반환 리스트 선언
        List<ProductPageMainProductResponseDto> getPageMainProductList = new ArrayList<>();

        // 실제 제품 페이지에 노출될 제품들을 담은 리스트가 존재할 경우 진입
        if (!productPageMainProducts.isEmpty()) {

            // 저장된 제품들의 정보를 추출하여 반환 객체에 맞게끔 Converting
            productPageMainProducts.forEach(eachProduct -> {
                ProductCreateResponseDto convertProductInfo = getProduct(eachProduct, "N");

                int sellOrEventPrice = 0;

                if (convertProductInfo.getEventStartDate().isBefore(LocalDateTime.now()) && convertProductInfo.getEventEndDate().isAfter(LocalDateTime.now())) {
                    sellOrEventPrice = convertProductInfo.getEventPrice();
                } else {
                    sellOrEventPrice = convertProductInfo.getSellPrice();
                }

                getPageMainProductList.add(
                        ProductPageMainProductResponseDto.builder()
                                .supplierId(convertProductInfo.getSupplierId())
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
                                .deliveryType(convertProductInfo.getDeliveryType())
                                .sellClassification(convertProductInfo.getSellClassification())
                                .expressionCheck(convertProductInfo.getExpressionCheck())
                                .normalPrice(convertProductInfo.getNormalPrice())
                                .sellPrice(sellOrEventPrice)
                                .deliveryPrice(convertProductInfo.getDeliveryPrice())
                                .purchasePrice(convertProductInfo.getPurchasePrice())
                                .eventStartDate(convertProductInfo.getEventStartDate())
                                .eventEndDate(convertProductInfo.getEventEndDate())
                                .eventDescription(convertProductInfo.getEventDescription())
                                .optionCheck(convertProductInfo.getOptionCheck())
                                .productOptionList(convertProductInfo.getProductOptionList())
                                .productDetailInfo(convertProductInfo.getProductDetailInfo())
                                .mediaList(convertProductInfo.getMediaList())
                                .manufacturer(convertProductInfo.getManufacturer())
                                .madeInOrigin(convertProductInfo.getMadeInOrigin())
                                .consignmentStore(convertProductInfo.getConsignmentStore())
                                .memo(convertProductInfo.getMemo())
                                .status(convertProductInfo.getStatus())
                                .build()
                );
            });
        }

        return TotalProductPageMainProductResponseDto.builder()
                .totalMainProductCount(totalCount)
                .maxPrice(maxPrice)
                .mainProductList(getPageMainProductList)
                .brandList(brandList)
                .labelList(labelList)
                .relatedUnderCategoryList(relatedCategoryList)
                .build();
    }


    // 중분류, 소분류 제품 페이지 조회 시 조건
    private BooleanExpression eqMiddleAndDownCategoryId(Integer categoryGroup, Long categoryId, List<Long> relatedDownCategoryIdList) {
        // 카테고리 구분 값이 1(중분류)일 경우 진입
        if (categoryGroup == 1) {
            // 중분류에 연관된 소분류 카테고리 id가 0이 아닐 경우 진입
            if (!relatedDownCategoryIdList.isEmpty()) {
                // 중분류 카테고리 id + 소분류 카테고리 id를 기준으로 카테고리 및 브랜드 매핑 정보 조회 조건 적용
                return categoryInBrand.category2Id.eq(categoryId)
                        .and(categoryInBrand.category3Id.in(relatedDownCategoryIdList));
            } else { // 연관된 소분류 카테고리 id가 0이거나 존재하지 않을 경우
                // 중분류 카테고리 id만 가지고 카테고리 및 브랜드 매핑 정보 조회 조건 적용
                return categoryInBrand.category2Id.eq(categoryId);
            }
        } else if (categoryGroup == 2) { // 카테고리 구분 값이 2(소분류)일 경우 진입
            // 소분류 카테고리 및 브랜드 매핑 정보 조회 조건 적용
            return categoryInBrand.category3Id.eq(categoryId);
        } else { // 카테고리 구분 값이 존재하지 않을 경우 진입
            // 해당조건 제외
            return null;
        }
    }


    // 고객 제품 리스트 검색 조회
    public TotalProductSearchResponseDto searchProducts(
            String loginMemberType, int sort, String searchKeyword, int page, int startRangePrice, int endRangePrice, List<Long> brandIdList, List<Long> labelIdList, List<Long> relatedMiddleCategoryIdList) {

        // 검색된 제품 수
        int totalSearchCount = 0;

        String filterKeyword = searchKeyword.replaceAll("[\\[\\]]", "%").replace(" ", "%");

        // 제품과 연관된 카테고리 및 브랜드 매핑 정보들의 id들을 담을 리스트 생성
        // 만약 브랜드나 중분류 정렬 기준을 선택했을 경우 추가 조건을 적용하여 관련된 CategoryInBrand를 추출
        List<Long> relatedCategoryAndBrandIds = eqBrandAndCategorySearchProducts(brandIdList, relatedMiddleCategoryIdList, filterKeyword);

        // 검색된 제품들을 담을 리스트 생성
        List<Product> searchProducts = new ArrayList<>();
        List<LabelResponseDto> labelList = new ArrayList<>();
        List<BrandDataResponseDto> brandList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> upCategoryList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> middleCategoryList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> downCategoryList = new ArrayList<>();

        int maxPrice = 0;

        // 브랜드, 중분류 정렬 기준을 반영하여 추출한 CategoryInBrand 리스트에 관련 데이터가 존재할 경우 검색한 제품에 해당 조건을 적용
        //if (!relatedCategoryAndBrandIds.isEmpty()) {

        log.info("추출한 카테고리 및 브랜드 아이디 리스트 수 : {}", relatedCategoryAndBrandIds.size());

        // 고객 유형이 일반일 경우 C, A 타입의 신 제품들 추출
        if (loginMemberType.equals("C")) {

            List<Product> products = new ArrayList<>();

            if (sort <= 3) {

                if (!relatedCategoryAndBrandIds.isEmpty()) {
                    if (!brandIdList.isEmpty()) {
                        if (!relatedMiddleCategoryIdList.isEmpty()) {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productName.like("%" + filterKeyword + "%")
                                                    .and(product.categoryInBrandId.in(
                                                                    jpaQueryFactory
                                                                            .select(categoryInBrand.categoryInBrandId)
                                                                            .from(categoryInBrand)
                                                                            .where(categoryInBrand.brandId.in(
                                                                                            jpaQueryFactory
                                                                                                    .select(brand.brandId)
                                                                                                    .from(brand)
                                                                                                    .where(brand.brandId.in(brandIdList))
                                                                                                    .fetch()
                                                                                    )
                                                                            )
                                                                            .fetch()
                                                            )
                                                    )
                                                    .and(product.categoryInBrandId.in(
                                                                    jpaQueryFactory
                                                                            .select(categoryInBrand.categoryInBrandId)
                                                                            .from(categoryInBrand)
                                                                            .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                            .fetch()
                                                            )
                                                    )
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.brandId.in(
                                                                                                    jpaQueryFactory
                                                                                                            .select(brand.brandId)
                                                                                                            .from(brand)
                                                                                                            .where(brand.brandId.in(brandIdList))
                                                                                                            .fetch()
                                                                                            )
                                                                                    )
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.brandId.in(
                                                                                                    jpaQueryFactory
                                                                                                            .select(brand.brandId)
                                                                                                            .from(brand)
                                                                                                            .where(brand.brandId.in(brandIdList))
                                                                                                            .fetch()
                                                                                            )
                                                                                    )
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.brandId.in(
                                                                                                    jpaQueryFactory
                                                                                                            .select(brand.brandId)
                                                                                                            .from(brand)
                                                                                                            .where(brand.brandId.in(brandIdList))
                                                                                                            .fetch()
                                                                                            )
                                                                                    )
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productName.like("%" + filterKeyword + "%")
                                                    .and(product.categoryInBrandId.in(
                                                                    jpaQueryFactory
                                                                            .select(categoryInBrand.categoryInBrandId)
                                                                            .from(categoryInBrand)
                                                                            .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                            .fetch()
                                                            )
                                                    )
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                    )

                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        }

                    } else {
                        if (!relatedMiddleCategoryIdList.isEmpty()) {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productName.like("%" + filterKeyword + "%")
                                                    .and(product.categoryInBrandId.in(
                                                                    jpaQueryFactory
                                                                            .select(categoryInBrand.categoryInBrandId)
                                                                            .from(categoryInBrand)
                                                                            .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                            .fetch()
                                                            )
                                                    )
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(product.categoryInBrandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                                    .from(categoryInBrand)
                                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )

                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productName.like("%" + filterKeyword + "%")
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    )
                                                    .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        }

                    }

                } else {

                    if (!brandIdList.isEmpty()) {
                        if (!relatedMiddleCategoryIdList.isEmpty()) {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.categoryInBrandId.in(
                                                            jpaQueryFactory
                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                    .from(categoryInBrand)
                                                                    .where(categoryInBrand.brandId.in(
                                                                                    jpaQueryFactory
                                                                                            .select(brand.brandId)
                                                                                            .from(brand)
                                                                                            .where(brand.brandId.in(brandIdList))
                                                                                            .fetch()
                                                                            )
                                                                    )
                                                                    .fetch()
                                                    )
                                                    .and(product.categoryInBrandId.in(
                                                                    jpaQueryFactory
                                                                            .select(categoryInBrand.categoryInBrandId)
                                                                            .from(categoryInBrand)
                                                                            .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                            .fetch()
                                                            )
                                                    )
                                                    .and((product.productName.like("%" + filterKeyword + "%")
                                                                    .and(eqLabelOfProduct(labelIdList))
                                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                                            )
                                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                                            .and(eqLabelOfProduct(labelIdList))
                                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                                            )
                                                                    )
                                                    )
                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.categoryInBrandId.in(
                                                    jpaQueryFactory
                                                            .select(categoryInBrand.categoryInBrandId)
                                                            .from(categoryInBrand)
                                                            .where(categoryInBrand.brandId.in(
                                                                            jpaQueryFactory
                                                                                    .select(brand.brandId)
                                                                                    .from(brand)
                                                                                    .where(brand.brandId.in(brandIdList))
                                                                                    .fetch()
                                                                    )
                                                            )
                                                            .fetch()
                                            ).and((product.productName.like("%" + filterKeyword + "%")
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    )
                                                            .or(product.productName.like("%" + filterKeyword + "%")
                                                                    .and(eqLabelOfProduct(labelIdList))
                                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                                    )
                                                            )
                                            )
                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        }
                    } else {
                        if (!relatedMiddleCategoryIdList.isEmpty()) {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.categoryInBrandId.in(
                                                            jpaQueryFactory
                                                                    .select(categoryInBrand.categoryInBrandId)
                                                                    .from(categoryInBrand)
                                                                    .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                    .fetch()
                                                    )
                                                    .and((product.productName.like("%" + filterKeyword + "%")
                                                                    .and(eqLabelOfProduct(labelIdList))
                                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                                            )
                                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                                            .and(eqLabelOfProduct(labelIdList))
                                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                    )
                                                    )
                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());

                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productName.like("%" + filterKeyword + "%")
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                                    .or(product.productName.like("%" + filterKeyword + "%")
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                    )
                                    .orderBy(orderBySort(sort))
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList());
                        }
                    }
                }

            } else {

                List<Tuple> orderInProducts = jpaQueryFactory
                        .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                        .from(orderInProduct)
                        .groupBy(orderInProduct.productClassificationCode)
                        .orderBy(orderInProduct.productTotalAmount.sum().desc())
                        .fetch();

                if (!orderInProducts.isEmpty()) {
                    // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
                    List<Long> remainOrderInfoProductIdList = orderInProducts
                            .stream()
                            .map(eachOrderInProduct ->
                                    jpaQueryFactory
                                            .select(product.productId)
                                            .from(product)
                                            .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                                    .and(product.status.eq("Y")))
                                            .fetchOne()
                            )
                            .collect(Collectors.toList());

                    if (!relatedCategoryAndBrandIds.isEmpty()) {
                        products = jpaQueryFactory
                                .selectFrom(product)
                                .where((product.productId.in(remainOrderInfoProductIdList)
                                                .and(product.productName.like("%" + filterKeyword + "%"))
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(betweenPrice(startRangePrice, endRangePrice))
                                        )
                                                .or(product.productId.in(remainOrderInfoProductIdList)
                                                        .and(product.productName.like("%" + filterKeyword + "%"))
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(product.eventStartDate.before(LocalDateTime.now())
                                                                .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                )
                                                .or(product.productId.in(remainOrderInfoProductIdList)
                                                        .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                )
                                                .or(product.productId.in(remainOrderInfoProductIdList)
                                                        .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(product.eventStartDate.before(LocalDateTime.now())
                                                                .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                )
                                )
                                .orderBy(orderBySort(sort))
                                .fetch()
                                .stream()
                                .filter(eachProduct ->
                                        eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                )
                                .collect(Collectors.toList());

                    } else {
                        if (!brandIdList.isEmpty()) {
                            if (!relatedMiddleCategoryIdList.isEmpty()) {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.brandId.in(
                                                                                        jpaQueryFactory
                                                                                                .select(brand.brandId)
                                                                                                .from(brand)
                                                                                                .where(brand.brandId.in(brandIdList))
                                                                                                .fetch()
                                                                                )
                                                                        )
                                                                        .fetch()
                                                        )
                                                        .and(product.categoryInBrandId.in(
                                                                        jpaQueryFactory
                                                                                .select(categoryInBrand.categoryInBrandId)
                                                                                .from(categoryInBrand)
                                                                                .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                .fetch()
                                                                )
                                                        )
                                                        .and((product.productId.in(remainOrderInfoProductIdList)
                                                                        .and(product.productName.like("%" + filterKeyword + "%"))
                                                                        .and(eqLabelOfProduct(labelIdList))
                                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                                )
                                                                        .or(product.productId.in(remainOrderInfoProductIdList)
                                                                                .and(product.productName.like("%" + filterKeyword + "%"))
                                                                                .and(eqLabelOfProduct(labelIdList))
                                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                        )
                                                        )
                                        )
                                        .orderBy(orderBySort(sort))
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            } else {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.brandId.in(
                                                                                        jpaQueryFactory
                                                                                                .select(brand.brandId)
                                                                                                .from(brand)
                                                                                                .where(brand.brandId.in(brandIdList))
                                                                                                .fetch()
                                                                                )
                                                                        )
                                                                        .fetch()
                                                        )
                                                        .and((product.productId.in(remainOrderInfoProductIdList)
                                                                        .and(product.productName.like("%" + filterKeyword + "%"))
                                                                        .and(eqLabelOfProduct(labelIdList))
                                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                                )
                                                                        .or(product.productId.in(remainOrderInfoProductIdList)
                                                                                .and(product.productName.like("%" + filterKeyword + "%"))
                                                                                .and(eqLabelOfProduct(labelIdList))
                                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                        )
                                                        )
                                        )
                                        .orderBy(orderBySort(sort))
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            }

                        } else {

                            if (!relatedMiddleCategoryIdList.isEmpty()) {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                        .fetch()
                                                        )
                                                        .and((product.productId.in(remainOrderInfoProductIdList)
                                                                        .and(product.productName.like("%" + filterKeyword + "%"))
                                                                        .and(eqLabelOfProduct(labelIdList))
                                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                                )
                                                                        .or(product.productId.in(remainOrderInfoProductIdList)
                                                                                .and(product.productName.like("%" + filterKeyword + "%"))
                                                                                .and(eqLabelOfProduct(labelIdList))
                                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                        )
                                                        )
                                        )
                                        .orderBy(orderBySort(sort))
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            } else {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where((product.productId.in(remainOrderInfoProductIdList)
                                                        .and(product.productName.like("%" + filterKeyword + "%"))
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                )
                                                        .or(product.productId.in(remainOrderInfoProductIdList)
                                                                .and(product.productName.like("%" + filterKeyword + "%"))
                                                                .and(eqLabelOfProduct(labelIdList))
                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                        )
                                        )
                                        .orderBy(orderBySort(sort))
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            }

                        }
                    }

                } else {

                    if (!relatedCategoryAndBrandIds.isEmpty()) {
                        products = jpaQueryFactory
                                .selectFrom(product)
                                .where((product.productName.like("%" + filterKeyword + "%")
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(betweenPrice(startRangePrice, endRangePrice))
                                        )
                                                .or(product.productName.like("%" + filterKeyword + "%")
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(product.eventStartDate.before(LocalDateTime.now())
                                                                .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                )
                                                .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                )
                                                .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(product.eventStartDate.before(LocalDateTime.now())
                                                                .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                )
                                )
                                .orderBy(product.createdAt.desc())
                                .fetch()
                                .stream()
                                .filter(eachProduct ->
                                        eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                )
                                .collect(Collectors.toList());
                    } else {
                        if (!brandIdList.isEmpty()) {

                            if (!relatedMiddleCategoryIdList.isEmpty()) {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.brandId.in(
                                                                                        jpaQueryFactory
                                                                                                .select(brand.brandId)
                                                                                                .from(brand)
                                                                                                .where(brand.brandId.in(brandIdList))
                                                                                                .fetch()
                                                                                )
                                                                        )
                                                                        .fetch()
                                                        )
                                                        .and(product.categoryInBrandId.in(
                                                                        jpaQueryFactory
                                                                                .select(categoryInBrand.categoryInBrandId)
                                                                                .from(categoryInBrand)
                                                                                .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                                .fetch()
                                                                )
                                                        )
                                                        .and((product.productName.like("%" + filterKeyword + "%")
                                                                        .and(eqLabelOfProduct(labelIdList))
                                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                                )
                                                                        .or(product.productName.like("%" + filterKeyword + "%")
                                                                                .and(eqLabelOfProduct(labelIdList))
                                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                        )
                                                        )
                                        )
                                        .orderBy(product.createdAt.desc())
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            } else {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.brandId.in(
                                                                                        jpaQueryFactory
                                                                                                .select(brand.brandId)
                                                                                                .from(brand)
                                                                                                .where(brand.brandId.in(brandIdList))
                                                                                                .fetch()
                                                                                )
                                                                        )
                                                                        .fetch()
                                                        )
                                                        .and((product.productName.like("%" + filterKeyword + "%")
                                                                        .and(eqLabelOfProduct(labelIdList))
                                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                                )
                                                                        .or(product.productName.like("%" + filterKeyword + "%")
                                                                                .and(eqLabelOfProduct(labelIdList))
                                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                        )
                                                        )
                                        )
                                        .orderBy(product.createdAt.desc())
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            }

                        } else {

                            if (!relatedMiddleCategoryIdList.isEmpty()) {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where(product.categoryInBrandId.in(
                                                                jpaQueryFactory
                                                                        .select(categoryInBrand.categoryInBrandId)
                                                                        .from(categoryInBrand)
                                                                        .where(categoryInBrand.category2Id.in(relatedMiddleCategoryIdList))
                                                                        .fetch()
                                                        )
                                                        .and((product.productName.like("%" + filterKeyword + "%")
                                                                        .and(eqLabelOfProduct(labelIdList))
                                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                                )
                                                                        .or(product.productName.like("%" + filterKeyword + "%")
                                                                                .and(eqLabelOfProduct(labelIdList))
                                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                                        )
                                                        )
                                        )
                                        .orderBy(product.createdAt.desc())
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            } else {
                                products = jpaQueryFactory
                                        .selectFrom(product)
                                        .where((product.productName.like("%" + filterKeyword + "%")
                                                        .and(eqLabelOfProduct(labelIdList))
                                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                                )
                                                        .or(product.productName.like("%" + filterKeyword + "%")
                                                                .and(eqLabelOfProduct(labelIdList))
                                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                        )
                                        )
                                        .orderBy(product.createdAt.desc())
                                        .fetch()
                                        .stream()
                                        .filter(eachProduct ->
                                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getSellClassification().equals("C") && eachProduct.getStatus().equals("Y")
                                        )
                                        .collect(Collectors.toList());
                            }
                        }

                    }
                }
            }

            // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
            maxPrice = products.stream()
                    .map(eachProduct -> {
                        if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                            return eachProduct.getEventPrice();
                        } else {
                            return eachProduct.getSellPrice();
                        }
                    })
                    .max(Integer::compare)
                    .orElse(0);

            // 검색 데이터 갯수
            totalSearchCount = products.size();

            // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
            List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                    .selectFrom(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.in(
                            products.stream()
                                    .map(Product::getCategoryInBrandId)
                                    .collect(Collectors.toList())
                    ))
                    .fetch();

            List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getBrandId)
                    .distinct()
                    .collect(Collectors.toList());

            brandList = jpaQueryFactory
                    .selectFrom(brand)
                    .where(brand.brandId.in(relatedBrandIdList))
                    .fetch()
                    .stream()
                    .map(eachBrand ->
                            BrandDataResponseDto.builder()
                                    .brandId(eachBrand.getBrandId())
                                    .brandTitle(eachBrand.getBrandTitle())
                                    .build()
                    )
                    .collect(Collectors.toList());

            // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
            // # 대분류
            List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getCategory1Id)
                    .distinct()
                    .collect(Collectors.toList());

            upCategoryList = jpaQueryFactory
                    .selectFrom(category)
                    .where(category.categoryId.in(relatedUpCategoryIdList))
                    .fetch()
                    .stream()
                    .map(eachUpCategory ->
                            RelatedCategoryDataResponseDto.builder()
                                    .categoryId(eachUpCategory.getCategoryId())
                                    .categoryName(eachUpCategory.getCategoryName())
                                    .build()
                    )
                    .collect(Collectors.toList());

            // # 중분류
            List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getCategory2Id)
                    .distinct()
                    .collect(Collectors.toList());

            middleCategoryList = jpaQueryFactory
                    .selectFrom(category)
                    .where(category.categoryId.in(relatedMiddleCategoryIds))
                    .fetch()
                    .stream()
                    .map(eachMiddleCategory ->
                            RelatedCategoryDataResponseDto.builder()
                                    .categoryId(eachMiddleCategory.getCategoryId())
                                    .categoryName(eachMiddleCategory.getCategoryName())
                                    .build()
                    )
                    .collect(Collectors.toList());

            // # 소분류
            List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getCategory3Id)
                    .distinct()
                    .collect(Collectors.toList());

            downCategoryList = jpaQueryFactory
                    .selectFrom(category)
                    .where(category.categoryId.in(relatedDownCategoryIdList))
                    .fetch()
                    .stream()
                    .map(eachDownCategory ->
                            RelatedCategoryDataResponseDto.builder()
                                    .categoryId(eachDownCategory.getCategoryId())
                                    .categoryName(eachDownCategory.getCategoryName())
                                    .build()
                    )
                    .collect(Collectors.toList());


            // [ 필터링 조건용 라벨 데이터 추출 로직 ]
            List<Long> relatedTotalLabelList = jpaQueryFactory
                    .select(labelOfProduct.labelId)
                    .from(labelOfProduct)
                    .where(labelOfProduct.productId.in(
                            products.stream()
                                    .map(Product::getProductId)
                                    .collect(Collectors.toList())
                    ))
                    .groupBy(labelOfProduct.labelId)
                    .fetch();

            labelList = jpaQueryFactory
                    .selectFrom(label)
                    .where(label.labelId.in(relatedTotalLabelList)
                            .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                    .fetch()
                    .stream()
                    .map(eachLabel ->
                            LabelResponseDto.builder()
                                    .labelId(eachLabel.getLabelId())
                                    .labelTitle(eachLabel.getLabelTitle())
                                    .build()
                    )
                    .collect(Collectors.toList());

            if (products.size() >= 20) {
                if ((page * 20) <= products.size()) {
                    searchProducts = products.subList((page * 20) - 20, page * 20);
                } else {
                    searchProducts = products.subList((page * 20) - 20, products.size());
                }
            } else {
                searchProducts = products.subList((page * 20) - 20, products.size());
            }


        } else if (loginMemberType.equals("B")) {
            List<Product> products = new ArrayList<>();

            if (sort <= 3) {

                products = jpaQueryFactory
                        .selectFrom(product)
                        .where((product.productName.like("%" + filterKeyword + "%")
                                        .and(eqLabelOfProduct(labelIdList))
                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                )
                                        .or(product.productName.like("%" + filterKeyword + "%")
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                        )
                                        .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(betweenPrice(startRangePrice, endRangePrice))
                                        )
                                        .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                        )
                        )
                        .orderBy(orderBySort(sort))
                        .fetch()
                        .stream()
                        .filter(eachProduct ->
                                eachProduct.getExpressionCheck().equals("Y") && eachProduct.getStatus().equals("Y")
                        )
                        .collect(Collectors.toList());
            } else {

                List<Tuple> orderInProducts = jpaQueryFactory
                        .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                        .from(orderInProduct)
                        .groupBy(orderInProduct.productClassificationCode)
                        .orderBy(orderInProduct.productTotalAmount.sum().desc())
                        .fetch();

                if (!orderInProducts.isEmpty()) {
                    // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
                    List<Long> remainOrderInfoProductIdList = orderInProducts
                            .stream()
                            .map(eachOrderInProduct ->
                                    jpaQueryFactory
                                            .select(product.productId)
                                            .from(product)
                                            .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                                    .and(product.status.eq("Y")))
                                            .fetchOne()
                            )
                            .collect(Collectors.toList());

                    // 판매 이력이 존재한 제품의 경우 우선 넣기
                    products = jpaQueryFactory
                            .selectFrom(product)
                            .where((product.productId.in(remainOrderInfoProductIdList)
                                            .and(product.productName.like("%" + filterKeyword + "%"))
                                            .and(eqLabelOfProduct(labelIdList))
                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                    )
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.productName.like("%" + filterKeyword + "%"))
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                            )
                            .orderBy(product.createdAt.desc())
                            .fetch()
                            .stream()
                            .filter(eachProduct ->
                                    eachProduct.getExpressionCheck().equals("Y") && eachProduct.getStatus().equals("Y")
                            )
                            .collect(Collectors.toList());

                    // 판매 이력이 존재한 제품의 경우 우선 넣기
                    products.addAll(
                            jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productId.notIn(remainOrderInfoProductIdList)
                                                    .and(product.productName.like("%" + filterKeyword + "%"))
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.productName.like("%" + filterKeyword + "%"))
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    )
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                            .and(eqLabelOfProduct(labelIdList))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                    )
                                    .orderBy(product.createdAt.desc())
                                    .fetch()
                                    .stream()
                                    .filter(eachProduct ->
                                            eachProduct.getExpressionCheck().equals("Y") && eachProduct.getStatus().equals("Y")
                                    )
                                    .collect(Collectors.toList())
                    );

                } else {

                    products = jpaQueryFactory
                            .selectFrom(product)
                            .where((product.productName.like("%" + filterKeyword + "%")
                                            .and(eqLabelOfProduct(labelIdList))
                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                    )
                                            .or(product.productName.like("%" + filterKeyword + "%")
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                                            .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            )
                                            .or(product.categoryInBrandId.in(relatedCategoryAndBrandIds)
                                                    .and(eqLabelOfProduct(labelIdList))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                            )
                            .orderBy(product.createdAt.desc())
                            .fetch()
                            .stream()
                            .filter(eachProduct ->
                                    eachProduct.getExpressionCheck().equals("Y") && eachProduct.getStatus().equals("Y")
                            )
                            .collect(Collectors.toList());
                }
            }


            // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
            maxPrice = products.stream()
                    .map(eachProduct -> {
                        if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                            return eachProduct.getEventPrice();
                        } else {
                            return eachProduct.getSellPrice();
                        }
                    })
                    .max(Integer::compare)
                    .orElse(0);

            // 검색 데이터 갯수
            totalSearchCount = products.size();

            // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
            List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                    .selectFrom(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.in(
                            products.stream()
                                    .map(Product::getCategoryInBrandId)
                                    .collect(Collectors.toList())
                    ))
                    .fetch();

            List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getBrandId)
                    .distinct()
                    .collect(Collectors.toList());

            brandList = jpaQueryFactory
                    .selectFrom(brand)
                    .where(brand.brandId.in(relatedBrandIdList))
                    .fetch()
                    .stream()
                    .map(eachBrand ->
                            BrandDataResponseDto.builder()
                                    .brandId(eachBrand.getBrandId())
                                    .brandTitle(eachBrand.getBrandTitle())
                                    .build()
                    )
                    .collect(Collectors.toList());

            // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
            // # 대분류
            List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getCategory1Id)
                    .distinct()
                    .collect(Collectors.toList());

            upCategoryList = jpaQueryFactory
                    .selectFrom(category)
                    .where(category.categoryId.in(relatedUpCategoryIdList))
                    .fetch()
                    .stream()
                    .map(eachUpCategory ->
                            RelatedCategoryDataResponseDto.builder()
                                    .categoryId(eachUpCategory.getCategoryId())
                                    .categoryName(eachUpCategory.getCategoryName())
                                    .build()
                    )
                    .collect(Collectors.toList());

            // # 중분류
            List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getCategory2Id)
                    .distinct()
                    .collect(Collectors.toList());

            middleCategoryList = jpaQueryFactory
                    .selectFrom(category)
                    .where(category.categoryId.in(relatedMiddleCategoryIds))
                    .fetch()
                    .stream()
                    .map(eachMiddleCategory ->
                            RelatedCategoryDataResponseDto.builder()
                                    .categoryId(eachMiddleCategory.getCategoryId())
                                    .categoryName(eachMiddleCategory.getCategoryName())
                                    .build()
                    )
                    .collect(Collectors.toList());

            // # 소분류
            List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
                    .map(CategoryInBrand::getCategory3Id)
                    .distinct()
                    .collect(Collectors.toList());

            downCategoryList = jpaQueryFactory
                    .selectFrom(category)
                    .where(category.categoryId.in(relatedDownCategoryIdList))
                    .fetch()
                    .stream()
                    .map(eachDownCategory ->
                            RelatedCategoryDataResponseDto.builder()
                                    .categoryId(eachDownCategory.getCategoryId())
                                    .categoryName(eachDownCategory.getCategoryName())
                                    .build()
                    )
                    .collect(Collectors.toList());


            // [ 필터링 조건용 라벨 데이터 추출 로직 ]
            List<Long> relatedTotalLabelList = jpaQueryFactory
                    .select(labelOfProduct.labelId)
                    .from(labelOfProduct)
                    .where(labelOfProduct.productId.in(
                            products.stream()
                                    .map(Product::getProductId)
                                    .collect(Collectors.toList())
                    ))
                    .groupBy(labelOfProduct.labelId)
                    .fetch();

            labelList = jpaQueryFactory
                    .selectFrom(label)
                    .where(label.labelId.in(relatedTotalLabelList)
                            .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                    .fetch()
                    .stream()
                    .map(eachLabel ->
                            LabelResponseDto.builder()
                                    .labelId(eachLabel.getLabelId())
                                    .labelTitle(eachLabel.getLabelTitle())
                                    .build()
                    )
                    .collect(Collectors.toList());

            if (products.size() >= 20) {
                if ((page * 20) <= products.size()) {
                    searchProducts = products.subList((page * 20) - 20, page * 20);
                } else {
                    searchProducts = products.subList((page * 20) - 20, products.size());
                }
            } else {
                searchProducts = products.subList((page * 20) - 20, products.size());
            }

        }

//        } else { // 브랜드, 중분류 정렬 기준을 반영하지 않는다면 해당 조건을 제외하고 나머지 검색 조건으로 제품들 추출
//
//            // 고객 유형이 일반일 경우 C, A 타입의 신 제품들 추출
//            if (loginMember.getType().equals("C")) {
//
//                List<Product> products = new ArrayList<>();
//
//                if (sort <= 3) {
//                    products = jpaQueryFactory
//                            .selectFrom(product)
//                            .where(product.productName.like("%" + filterKeyword + "%")
//                                    .and(product.expressionCheck.eq("Y"))
//                                    .and(product.status.eq("Y"))
//                                    .and(product.sellClassification.eq("C"))
//                                    .and(betweenPrice(startRangePrice, endRangePrice))
//                                    .and(eqLabelOfProduct(labelIdList)))
//                            .orderBy(orderBySort(sort))
//                            .fetch();
//
//                } else {
//
//                    List<Tuple> orderInProducts = jpaQueryFactory
//                            .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
//                            .from(orderInProduct)
//                            .groupBy(orderInProduct.productClassificationCode)
//                            .orderBy(orderInProduct.productTotalAmount.sum().desc())
//                            .fetch();
//
//                    if (!orderInProducts.isEmpty()) {
//                        // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
//                        List<Long> remainOrderInfoProductIdList = orderInProducts
//                                .stream()
//                                .map(eachOrderInProduct ->
//                                        jpaQueryFactory
//                                                .select(product.productId)
//                                                .from(product)
//                                                .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
//                                                        .and(product.status.eq("Y")))
//                                                .fetchOne()
//                                )
//                                .collect(Collectors.toList());
//
//                        // 판매 이력이 존재한 제품의 경우 우선 넣기
//                        products = jpaQueryFactory
//                                .selectFrom(product)
//                                .where((product.productId.in(remainOrderInfoProductIdList)
//                                        .and(product.productName.like("%" + filterKeyword + "%")
//                                                .and(product.expressionCheck.eq("Y"))
//                                                .and(product.status.eq("Y"))
//                                                .and(product.sellClassification.eq("C"))
//                                                .and(eqLabelOfProduct(labelIdList))
//                                                .and(betweenPrice(startRangePrice, endRangePrice))
//                                        ))
//                                        .or(product.productId.in(remainOrderInfoProductIdList)
//                                                .and(product.productName.like("%" + filterKeyword + "%")
//                                                        .and(product.expressionCheck.eq("Y"))
//                                                        .and(product.status.eq("Y"))
//                                                        .and(product.sellClassification.eq("C"))
//                                                        .and(eqLabelOfProduct(labelIdList))
//                                                        .and(product.eventStartDate.before(LocalDateTime.now())
//                                                                .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                                .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                                        )
//                                                )
//                                        )
//                                )
//                                .fetch();
//
//                        // 판매 이력이 없을 경우 최신 순으로 누적 저장
//                        products.addAll(
//                                jpaQueryFactory
//                                        .selectFrom(product)
//                                        .where((product.productId.notIn(remainOrderInfoProductIdList)
//                                                .and(product.productName.like("%" + filterKeyword + "%")
//                                                        .and(product.expressionCheck.eq("Y"))
//                                                        .and(product.status.eq("Y"))
//                                                        .and(product.sellClassification.eq("C"))
//                                                        .and(eqLabelOfProduct(labelIdList))
//                                                        .and(betweenPrice(startRangePrice, endRangePrice))
//                                                ))
//                                                .or(product.productId.notIn(remainOrderInfoProductIdList)
//                                                        .and(product.productName.like("%" + filterKeyword + "%")
//                                                                .and(product.expressionCheck.eq("Y"))
//                                                                .and(product.status.eq("Y"))
//                                                                .and(product.sellClassification.eq("C"))
//                                                                .and(eqLabelOfProduct(labelIdList))
//                                                                .and(product.eventStartDate.before(LocalDateTime.now())
//                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                                                )
//                                                        )
//                                                )
//                                        )
//                                        .orderBy(product.createdAt.desc())
//                                        .fetch()
//                        );
//                    } else {
//                        products = jpaQueryFactory
//                                .selectFrom(product)
//                                .where((product.productName.like("%" + filterKeyword + "%")
//                                        .and(product.expressionCheck.eq("Y"))
//                                        .and(product.status.eq("Y"))
//                                        .and(product.sellClassification.eq("C"))
//                                        .and(eqLabelOfProduct(labelIdList))
//                                        .and(betweenPrice(startRangePrice, endRangePrice)
//                                        ))
//                                        .or(product.productName.like("%" + filterKeyword + "%")
//                                                .and(product.expressionCheck.eq("Y"))
//                                                .and(product.status.eq("Y"))
//                                                .and(product.sellClassification.eq("C"))
//                                                .and(eqLabelOfProduct(labelIdList))
//                                                .and(product.eventStartDate.before(LocalDateTime.now())
//                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                                )
//                                        )
//                                )
//                                .orderBy(product.createdAt.desc())
//                                .fetch();
//                    }
//
//                }
//
//                // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
//                maxPrice = products.stream()
//                        .map(eachProduct -> {
//                            if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
//                                return eachProduct.getEventPrice();
//                            } else {
//                                return eachProduct.getSellPrice();
//                            }
//                        })
//                        .max(Integer::compare)
//                        .orElse(0);
//
//                // 검색 데이터 갯수
//                totalSearchCount = products.size();
//
//                // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
//                List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
//                        .selectFrom(categoryInBrand)
//                        .where(categoryInBrand.categoryInBrandId.in(
//                                products.stream()
//                                        .map(Product::getCategoryInBrandId)
//                                        .collect(Collectors.toList())
//                        ))
//                        .fetch();
//
//                List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getBrandId)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                brandList = jpaQueryFactory
//                        .selectFrom(brand)
//                        .where(brand.brandId.in(relatedBrandIdList))
//                        .fetch()
//                        .stream()
//                        .map(eachBrand ->
//                                BrandDataResponseDto.builder()
//                                        .brandId(eachBrand.getBrandId())
//                                        .brandTitle(eachBrand.getBrandTitle())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//
//                // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
//                // # 대분류
//                List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getCategory1Id)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                upCategoryList = jpaQueryFactory
//                        .selectFrom(category)
//                        .where(category.categoryId.in(relatedUpCategoryIdList))
//                        .fetch()
//                        .stream()
//                        .map(eachUpCategory ->
//                                RelatedCategoryDataResponseDto.builder()
//                                        .categoryId(eachUpCategory.getCategoryId())
//                                        .categoryName(eachUpCategory.getCategoryName())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//                // # 중분류
//                List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getCategory2Id)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                middleCategoryList = jpaQueryFactory
//                        .selectFrom(category)
//                        .where(category.categoryId.in(relatedMiddleCategoryIds))
//                        .fetch()
//                        .stream()
//                        .map(eachMiddleCategory ->
//                                RelatedCategoryDataResponseDto.builder()
//                                        .categoryId(eachMiddleCategory.getCategoryId())
//                                        .categoryName(eachMiddleCategory.getCategoryName())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//                // # 소분류
//                List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getCategory3Id)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                downCategoryList = jpaQueryFactory
//                        .selectFrom(category)
//                        .where(category.categoryId.in(relatedDownCategoryIdList))
//                        .fetch()
//                        .stream()
//                        .map(eachDownCategory ->
//                                RelatedCategoryDataResponseDto.builder()
//                                        .categoryId(eachDownCategory.getCategoryId())
//                                        .categoryName(eachDownCategory.getCategoryName())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//
//                // [ 필터링 조건용 라벨 데이터 추출 로직 ]
//                List<Long> relatedTotalLabelList = jpaQueryFactory
//                        .select(labelOfProduct.labelId)
//                        .from(labelOfProduct)
//                        .where(labelOfProduct.productId.in(
//                                products.stream()
//                                        .map(Product::getProductId)
//                                        .collect(Collectors.toList())
//                        ))
//                        .groupBy(labelOfProduct.labelId)
//                        .fetch();
//
//                labelList = jpaQueryFactory
//                        .selectFrom(label)
//                        .where(label.labelId.in(relatedTotalLabelList)
//                                .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
//                        .fetch()
//                        .stream()
//                        .map(eachLabel ->
//                                LabelResponseDto.builder()
//                                        .labelId(eachLabel.getLabelId())
//                                        .labelTitle(eachLabel.getLabelTitle())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//                if (products.size() >= 20) {
//                    if ((page * 20) <= products.size()) {
//                        searchProducts = products.subList((page * 20) - 20, page * 20);
//                    } else {
//                        searchProducts = products.subList((page * 20) - 20, products.size());
//                    }
//                } else {
//                    searchProducts = products.subList((page * 20) - 20, products.size());
//                }
//
//
//            } else if (loginMember.getType().equals("B")) {
//
//                List<Product> products = new ArrayList<>();
//
//                if (sort <= 3) {
//                    products = jpaQueryFactory
//                            .selectFrom(product)
//                            .where((product.productName.like("%" + filterKeyword + "%")
//                                    .and(product.expressionCheck.eq("Y"))
//                                    .and(product.status.eq("Y"))
//                                    .and(betweenPrice(startRangePrice, endRangePrice))
//                                    .and(eqLabelOfProduct(labelIdList)))
//                                    .or(product.productName.like("%" + filterKeyword + "%")
//                                            .and(product.expressionCheck.eq("Y"))
//                                            .and(product.status.eq("Y"))
//                                            .and(product.eventStartDate.before(LocalDateTime.now())
//                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                    .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                            )
//                                            .and(eqLabelOfProduct(labelIdList))
//                                    )
//                            )
//                            .orderBy(orderBySort(sort))
//                            .fetch();
//                } else {
//
//                    List<Tuple> orderInProducts = jpaQueryFactory
//                            .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
//                            .from(orderInProduct)
//                            .groupBy(orderInProduct.productClassificationCode)
//                            .orderBy(orderInProduct.productTotalAmount.sum().desc())
//                            .fetch();
//
//                    if (!orderInProducts.isEmpty()) {
//                        // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
//                        List<Long> remainOrderInfoProductIdList = orderInProducts
//                                .stream()
//                                .map(eachOrderInProduct ->
//                                        jpaQueryFactory
//                                                .select(product.productId)
//                                                .from(product)
//                                                .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
//                                                        .and(product.status.eq("Y")))
//                                                .fetchOne()
//                                )
//                                .collect(Collectors.toList());
//
//                        // 판매 이력이 존재한 제품의 경우 우선 넣기
//                        products = jpaQueryFactory
//                                .selectFrom(product)
//                                .where((product.productId.in(remainOrderInfoProductIdList)
//                                        .and(product.productName.like("%" + filterKeyword + "%")
//                                                .and(product.status.eq("Y"))
//                                                .and(product.expressionCheck.eq("Y"))
//                                                .and(betweenPrice(startRangePrice, endRangePrice))
//                                                .and(eqLabelOfProduct(labelIdList))
//                                        ))
//                                        .or(product.productId.in(remainOrderInfoProductIdList)
//                                                .and(product.productName.like("%" + filterKeyword + "%")
//                                                        .and(product.status.eq("Y"))
//                                                        .and(product.expressionCheck.eq("Y"))
//                                                        .and(product.eventStartDate.before(LocalDateTime.now())
//                                                                .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                                .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                                        )
//                                                        .and(eqLabelOfProduct(labelIdList))
//                                                )
//                                        )
//                                )
//                                .fetch();
//
//                        // 판매 이력이 없을 경우 최신 순으로 누적 저장
//                        products.addAll(
//                                jpaQueryFactory
//                                        .selectFrom(product)
//                                        .where((product.productId.notIn(remainOrderInfoProductIdList)
//                                                .and(product.productName.like("%" + filterKeyword + "%")
//                                                        .and(product.expressionCheck.eq("Y"))
//                                                        .and(product.status.eq("Y"))
//                                                        .and(betweenPrice(startRangePrice, endRangePrice))
//                                                        .and(eqLabelOfProduct(labelIdList))
//                                                ))
//                                                .or(product.productId.notIn(remainOrderInfoProductIdList)
//                                                        .and(product.productName.like("%" + filterKeyword + "%")
//                                                                .and(product.expressionCheck.eq("Y"))
//                                                                .and(product.status.eq("Y"))
//                                                                .and(product.eventStartDate.before(LocalDateTime.now())
//                                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                                                )
//                                                                .and(eqLabelOfProduct(labelIdList))
//                                                        )
//                                                )
//                                        )
//                                        .orderBy(product.createdAt.desc())
//                                        .fetch()
//                        );
//                    } else {
//                        products = jpaQueryFactory
//                                .selectFrom(product)
//                                .where((product.productName.like("%" + filterKeyword + "%")
//                                        .and(product.expressionCheck.eq("Y"))
//                                        .and(product.status.eq("Y"))
//                                        .and(betweenPrice(startRangePrice, endRangePrice))
//                                        .and(eqLabelOfProduct(labelIdList)))
//                                        .or(product.productName.like("%" + filterKeyword + "%")
//                                                .and(product.expressionCheck.eq("Y"))
//                                                .and(product.status.eq("Y"))
//                                                .and(product.eventStartDate.before(LocalDateTime.now())
//                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
//                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
//                                                )
//                                                .and(eqLabelOfProduct(labelIdList))
//                                        )
//                                )
//                                .orderBy(product.createdAt.desc())
//                                .fetch();
//                    }
//
//                }
//
//
//                // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
//                maxPrice = products.stream()
//                        .map(eachProduct -> {
//                            if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
//                                return eachProduct.getEventPrice();
//                            } else {
//                                return eachProduct.getSellPrice();
//                            }
//                        })
//                        .max(Integer::compare)
//                        .orElse(0);
//
//                // 검색 데이터 갯수
//                totalSearchCount = products.size();
//
//                // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
//                List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
//                        .selectFrom(categoryInBrand)
//                        .where(categoryInBrand.categoryInBrandId.in(
//                                products.stream()
//                                        .map(Product::getCategoryInBrandId)
//                                        .collect(Collectors.toList())
//                        ))
//                        .fetch();
//
//                List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getBrandId)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                brandList = jpaQueryFactory
//                        .selectFrom(brand)
//                        .where(brand.brandId.in(relatedBrandIdList))
//                        .fetch()
//                        .stream()
//                        .map(eachBrand ->
//                                BrandDataResponseDto.builder()
//                                        .brandId(eachBrand.getBrandId())
//                                        .brandTitle(eachBrand.getBrandTitle())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//
//                // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
//                // # 대분류
//                List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getCategory1Id)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                upCategoryList = jpaQueryFactory
//                        .selectFrom(category)
//                        .where(category.categoryId.in(relatedUpCategoryIdList))
//                        .fetch()
//                        .stream()
//                        .map(eachUpCategory ->
//                                RelatedCategoryDataResponseDto.builder()
//                                        .categoryId(eachUpCategory.getCategoryId())
//                                        .categoryName(eachUpCategory.getCategoryName())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//                // # 중분류
//                List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getCategory2Id)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                middleCategoryList = jpaQueryFactory
//                        .selectFrom(category)
//                        .where(category.categoryId.in(relatedMiddleCategoryIds))
//                        .fetch()
//                        .stream()
//                        .map(eachMiddleCategory ->
//                                RelatedCategoryDataResponseDto.builder()
//                                        .categoryId(eachMiddleCategory.getCategoryId())
//                                        .categoryName(eachMiddleCategory.getCategoryName())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//                // # 소분류
//                List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
//                        .map(CategoryInBrand::getCategory3Id)
//                        .distinct()
//                        .collect(Collectors.toList());
//
//                downCategoryList = jpaQueryFactory
//                        .selectFrom(category)
//                        .where(category.categoryId.in(relatedDownCategoryIdList))
//                        .fetch()
//                        .stream()
//                        .map(eachDownCategory ->
//                                RelatedCategoryDataResponseDto.builder()
//                                        .categoryId(eachDownCategory.getCategoryId())
//                                        .categoryName(eachDownCategory.getCategoryName())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//
//                // [ 필터링 조건용 라벨 데이터 추출 로직 ]
//                List<Long> relatedTotalLabelList = jpaQueryFactory
//                        .select(labelOfProduct.labelId)
//                        .from(labelOfProduct)
//                        .where(labelOfProduct.productId.in(
//                                products.stream()
//                                        .map(Product::getProductId)
//                                        .collect(Collectors.toList())
//                        ))
//                        .groupBy(labelOfProduct.labelId)
//                        .fetch();
//
//                labelList = jpaQueryFactory
//                        .selectFrom(label)
//                        .where(label.labelId.in(relatedTotalLabelList)
//                                .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
//                        .fetch()
//                        .stream()
//                        .map(eachLabel ->
//                                LabelResponseDto.builder()
//                                        .labelId(eachLabel.getLabelId())
//                                        .labelTitle(eachLabel.getLabelTitle())
//                                        .build()
//                        )
//                        .collect(Collectors.toList());
//
//                if (products.size() >= 20) {
//                    if ((page * 20) <= products.size()) {
//                        searchProducts = products.subList((page * 20) - 20, page * 20);
//                    } else {
//                        searchProducts = products.subList((page * 20) - 20, products.size());
//                    }
//                } else {
//                    searchProducts = products.subList((page * 20) - 20, products.size());
//                }
//
//            }

        //}

        // 최종적으로 확인하기 위한 반환 리스트 선언
        List<ProductSearchResponseDto> getSearchProductList = new ArrayList<>();

        // 검색된 제품들이 존재할 경우 검색된 제품 수량 저장 및 반환 객체에 매핑하여 리스트 저장
        if (!searchProducts.isEmpty()) {

            // 검색 제품들을 돌려 반환 객체에 맞게끔 매핑 및 Convert
            searchProducts.forEach(eachSearchProduct -> {
                ProductCreateResponseDto convertProductInfo = getProduct(eachSearchProduct, "N");

                int sellOrEventPrice = 0;

                if (convertProductInfo.getEventStartDate().isBefore(LocalDateTime.now()) && convertProductInfo.getEventEndDate().isAfter(LocalDateTime.now())) {
                    sellOrEventPrice = convertProductInfo.getEventPrice();
                } else {
                    sellOrEventPrice = convertProductInfo.getSellPrice();
                }

                getSearchProductList.add(
                        ProductSearchResponseDto.builder()
                                .supplierId(convertProductInfo.getSupplierId())
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
                                .deliveryType(convertProductInfo.getDeliveryType())
                                .sellClassification(convertProductInfo.getSellClassification())
                                .expressionCheck(convertProductInfo.getExpressionCheck())
                                .normalPrice(convertProductInfo.getNormalPrice())
                                .sellPrice(sellOrEventPrice)
                                .deliveryPrice(convertProductInfo.getDeliveryPrice())
                                .purchasePrice(convertProductInfo.getPurchasePrice())
                                .eventStartDate(convertProductInfo.getEventStartDate())
                                .eventEndDate(convertProductInfo.getEventEndDate())
                                .eventDescription(convertProductInfo.getEventDescription())
                                .optionCheck(convertProductInfo.getOptionCheck())
                                .productOptionList(convertProductInfo.getProductOptionList())
                                .productDetailInfo(convertProductInfo.getProductDetailInfo())
                                .mediaList(convertProductInfo.getMediaList())
                                .manufacturer(convertProductInfo.getManufacturer())
                                .madeInOrigin(convertProductInfo.getMadeInOrigin())
                                .consignmentStore(convertProductInfo.getConsignmentStore())
                                .memo(convertProductInfo.getMemo())
                                .status(convertProductInfo.getStatus())
                                .build()
                );
            });
        }

        return TotalProductSearchResponseDto.builder()
                .totalSearchProductCount(totalSearchCount)
                .maxPrice(maxPrice)
                .searchProductList(getSearchProductList)
                .brandList(brandList)
                .labelList(labelList)
                .upCategoryList(upCategoryList)
                .middleCategoryList(middleCategoryList)
                .downCategoryList(downCategoryList)
                .build();
    }


    // 검색 시 브랜드, 중분류 기준 정렬 선택 시 적용되는 조건
    private List<Long> eqBrandAndCategorySearchProducts(List<Long> brandIdList, List<Long> relatedMiddleCategoryIdList, String searchKeyword) {

        // 카테고리와 브랜드 매핑 정보 데이터들의 id를 담을 리스트 생성
        List<Long> categoryInBrandIds = new ArrayList<>();

        // 브랜드 id가 존재하고 연관된 중분류 카테고리 id가 존재할 경우 진입
        if (!brandIdList.isEmpty() && !relatedMiddleCategoryIdList.isEmpty()) {

            if (eqCategoryProductBrand(brandIdList, searchKeyword) == null) {
                return categoryInBrandIds;
            }

            // 브랜드와 중분류 카테고리 매핑 정보 추출
            categoryInBrandIds = jpaQueryFactory
                    .select(categoryInBrand.categoryInBrandId)
                    .from(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.gt(0L)
                            .and(eqUpCategoryInMiddleCategory(relatedMiddleCategoryIdList))
                            .and(eqCategoryProductBrand(brandIdList, searchKeyword)))
                    .fetch();

        } else if (brandIdList.isEmpty() && !relatedMiddleCategoryIdList.isEmpty()) { // 브랜드 id가 존재하지 않고 연관된 중분류 카테고리 id가 존재할 경우 진입

            List<Long> relateMiddleCategoryIds = jpaQueryFactory
                    .select(categoryInBrand.categoryInBrandId)
                    .from(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.gt(0L)
                            .and(eqUpCategoryInMiddleCategory(relatedMiddleCategoryIdList))
                            .and(eqCategoryProductBrand(brandIdList, searchKeyword)))
                    .fetch();

            if (relateMiddleCategoryIds.isEmpty()) {
                return categoryInBrandIds;
            }

            // 중분류 카테고리 기준으로 브랜드 / 카테고리 매핑 정보 추출
            categoryInBrandIds = relateMiddleCategoryIds;

        } else if (!brandIdList.isEmpty() && relatedMiddleCategoryIdList.isEmpty()) { // 브랜드 id가 존재하고 연관된 중분류 카테고리 id가 존재하지 않을 경우 진입

            if (eqCategoryProductBrand(brandIdList, searchKeyword) == null) {
                return categoryInBrandIds;
            }

            // 브랜드 기준으로 브랜드 / 카테고리 매핑 정보 추출
            categoryInBrandIds = jpaQueryFactory
                    .select(categoryInBrand.categoryInBrandId)
                    .from(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.gt(0L)
                            .and(eqCategoryProductBrand(brandIdList, searchKeyword)))
                    .fetch();

        } else if (brandIdList.isEmpty() && relatedMiddleCategoryIdList.isEmpty()) {

            if (eqCategoryProductBrand(brandIdList, searchKeyword) == null) {
                return categoryInBrandIds;
            }

            categoryInBrandIds = jpaQueryFactory
                    .select(categoryInBrand.categoryInBrandId)
                    .from(categoryInBrand)
                    .where(categoryInBrand.categoryInBrandId.gt(0L)
                            .and(eqCategoryProductBrand(brandIdList, searchKeyword)))
                    .fetch();

        }

        return categoryInBrandIds;


    }


    // 라벨 기준 제품 페이지 제품 리스트 조회
    public TotalLabelProductPageResponseDto labelPageMainProducts(
            String loginMemberType, Long labelId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandIdList, List<Long> relatedMiddleCategoryIdList) {

        // 라벨 이미지 호출 url
        Label callLabel = jpaQueryFactory
                .selectFrom(label)
                .where(label.labelId.eq(labelId))
                .fetchOne();

        // 라벨이 존재하지 않을 경우 null 반환
        if (callLabel == null) {
            return null;
        }

        // 라벨 제품 수 초기 값 설정
        int totalLabelProductCount = 0;

        // 연관된 카테고리 및 브랜드 매핑 정보 id 리스트 생성
        // 만약 브랜드나 중분류 정렬 기준을 선택했을 경우 추가 조건을 적용하여 관련된 CategoryInBrand를 추출
        List<Long> relatedCategoryAndBrandIds = eqBrandAndCategorySearchProducts(brandIdList, relatedMiddleCategoryIdList, "");

        // 호출할 제품들을 담을 리스트 생성
        List<Product> labelProducts = new ArrayList<>();
        List<LabelResponseDto> labelList = new ArrayList<>();
        List<BrandDataResponseDto> brandList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> upCategoryList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> middleCategoryList = new ArrayList<>();
        List<RelatedCategoryDataResponseDto> downCategoryList = new ArrayList<>();

        int maxPrice = 0;

        // 라벨 id에 해당되는 라벨과 제품 매핑 정보 데이터들의 id 리스트 추출
        List<Long> labelOfProductIds = jpaQueryFactory
                .select(labelOfProduct.productId)
                .from(labelOfProduct)
                .where(labelOfProduct.labelId.eq(callLabel.getLabelId()))
                .groupBy(labelOfProduct.productId)
                .fetch();

        // 라벨과 제품 매핑 정보 데이터들의 id 리스트가 존재할 경우 진입
        if (!labelOfProductIds.isEmpty()) {
            // 브랜드, 중분류 정렬 기준을 반영하여 추출한 CategoryInBrand 리스트에 관련 데이터가 존재할 경우 검색한 제품에 해당 조건을 적용
            if (!relatedCategoryAndBrandIds.isEmpty()) {

                // 고객 유형이 일반일 경우 C, A 타입의 신 제품들 추출
                if (loginMemberType.equals("C")) {

                    List<Product> products = new ArrayList<>();

                    if (sort <= 3) {
                        products = jpaQueryFactory
                                .selectFrom(product)
                                .where((product.productId.in(labelOfProductIds)
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(product.status.eq("Y"))
                                        .and(product.sellClassification.eq("C"))
                                        .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                        .and(betweenPrice(startRangePrice, endRangePrice)))
                                        .or(product.productId.in(labelOfProductIds)
                                                .and(product.expressionCheck.eq("Y"))
                                                .and(product.status.eq("Y"))
                                                .and(product.sellClassification.eq("C"))
                                                .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                )
                                        )
                                )
                                .orderBy(orderBySort(sort))
                                .fetch();
                    } else {

                        List<Tuple> orderInProducts = jpaQueryFactory
                                .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                                .from(orderInProduct)
                                .where(orderInProduct.productClassificationCode.in(
                                        jpaQueryFactory
                                                .select(product.classificationCode)
                                                .from(product)
                                                .where(product.productId.in(labelOfProductIds))
                                                .fetch())
                                )
                                .groupBy(orderInProduct.productClassificationCode)
                                .orderBy(orderInProduct.productTotalAmount.sum().desc())
                                .fetch();

                        if (!orderInProducts.isEmpty()) {
                            // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
                            List<Long> remainOrderInfoProductIdList = orderInProducts
                                    .stream()
                                    .map(eachOrderInProduct ->
                                            jpaQueryFactory
                                                    .select(product.productId)
                                                    .from(product)
                                                    .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                                            .and(product.status.eq("Y")))
                                                    .fetchOne()
                                    )
                                    .collect(Collectors.toList());

                            // 판매 이력이 존재한 제품의 경우 우선 넣기
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productId.in(remainOrderInfoProductIdList)
                                            .and(product.expressionCheck.eq("Y"))
                                            .and(product.status.eq("Y"))
                                            .and(product.sellClassification.eq("C"))
                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                            .and(betweenPrice(startRangePrice, endRangePrice)))
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.sellClassification.eq("C"))
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                                    )
                                    .fetch();

                            // 판매 이력이 없을 경우 최신 순으로 누적 저장
                            products.addAll(
                                    jpaQueryFactory
                                            .selectFrom(product)
                                            .where(product.productId.notIn(remainOrderInfoProductIdList)
                                                    .and(product.productId.in(labelOfProductIds))
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.sellClassification.eq("C"))
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.productId.in(labelOfProductIds))
                                                            .and(product.expressionCheck.eq("Y"))
                                                            .and(product.status.eq("Y"))
                                                            .and(product.sellClassification.eq("C"))
                                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                                    )
                                            )
                                            .orderBy(product.createdAt.desc())
                                            .fetch()
                            );
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.productId.in(labelOfProductIds)
                                            .and(product.expressionCheck.eq("Y"))
                                            .and(product.status.eq("Y"))
                                            .and(product.sellClassification.eq("C"))
                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                            .or(product.productId.in(labelOfProductIds)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.sellClassification.eq("C"))
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                                    )
                                    .orderBy(product.createdAt.desc())
                                    .fetch();
                        }

                    }


                    // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
                    maxPrice = products.stream()
                            .map(eachProduct -> {
                                if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                                    return eachProduct.getEventPrice();
                                } else {
                                    return eachProduct.getSellPrice();
                                }
                            })
                            .max(Integer::compare)
                            .orElse(0);

                    // 라벨 제품 수
                    totalLabelProductCount = products.size();

                    // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
                    List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                            .selectFrom(categoryInBrand)
                            .where(categoryInBrand.categoryInBrandId.in(
                                    products.stream()
                                            .map(Product::getCategoryInBrandId)
                                            .collect(Collectors.toList())
                            ))
                            .fetch();

                    List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getBrandId)
                            .distinct()
                            .collect(Collectors.toList());

                    brandList = jpaQueryFactory
                            .selectFrom(brand)
                            .where(brand.brandId.in(relatedBrandIdList))
                            .fetch()
                            .stream()
                            .map(eachBrand ->
                                    BrandDataResponseDto.builder()
                                            .brandId(eachBrand.getBrandId())
                                            .brandTitle(eachBrand.getBrandTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
                    // # 대분류
                    List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory1Id)
                            .distinct()
                            .collect(Collectors.toList());

                    upCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedUpCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachUpCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachUpCategory.getCategoryId())
                                            .categoryName(eachUpCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 중분류
                    List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory2Id)
                            .distinct()
                            .collect(Collectors.toList());

                    middleCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedMiddleCategoryIds))
                            .fetch()
                            .stream()
                            .map(eachMiddleCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachMiddleCategory.getCategoryId())
                                            .categoryName(eachMiddleCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 소분류
                    List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory3Id)
                            .distinct()
                            .collect(Collectors.toList());

                    downCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedDownCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachDownCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachDownCategory.getCategoryId())
                                            .categoryName(eachDownCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 라벨 데이터 추출 로직 ]
                    List<Long> relatedTotalLabelList = jpaQueryFactory
                            .select(labelOfProduct.labelId)
                            .from(labelOfProduct)
                            .where(labelOfProduct.productId.in(
                                    products.stream()
                                            .map(Product::getProductId)
                                            .collect(Collectors.toList())
                            ))
                            .groupBy(labelOfProduct.labelId)
                            .fetch();

                    labelList = jpaQueryFactory
                            .selectFrom(label)
                            .where(label.labelId.in(relatedTotalLabelList)
                                    .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                            .fetch()
                            .stream()
                            .map(eachLabel ->
                                    LabelResponseDto.builder()
                                            .labelId(eachLabel.getLabelId())
                                            .labelTitle(eachLabel.getLabelTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    if (products.size() >= 20) {
                        if ((page * 20) <= products.size()) {
                            labelProducts = products.subList((page * 20) - 20, page * 20);
                        } else {
                            labelProducts = products.subList((page * 20) - 20, products.size());
                        }
                    } else {
                        labelProducts = products.subList((page * 20) - 20, products.size());
                    }

                } else if (loginMemberType.equals("B")) { // 고객 유형이 일반일 경우 B, A 타입의 신 제품들 추출

                    List<Product> products = new ArrayList<>();

                    if (sort <= 3) {
                        products = jpaQueryFactory
                                .selectFrom(product)
                                .where(product.productId.in(labelOfProductIds)
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(product.status.eq("Y"))
                                        .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                        .or(product.productId.in(labelOfProductIds)
                                                .and(product.expressionCheck.eq("Y"))
                                                .and(product.status.eq("Y"))
                                                .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                )
                                        )
                                )
                                .orderBy(orderBySort(sort))
                                .fetch();
                    } else {

                        List<Tuple> orderInProducts = jpaQueryFactory
                                .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                                .from(orderInProduct)
                                .where(orderInProduct.productClassificationCode.in(
                                        jpaQueryFactory
                                                .select(product.classificationCode)
                                                .from(product)
                                                .where(product.productId.in(labelOfProductIds))
                                                .fetch())
                                )
                                .groupBy(orderInProduct.productClassificationCode)
                                .orderBy(orderInProduct.productTotalAmount.sum().desc())
                                .fetch();

                        if (!orderInProducts.isEmpty()) {
                            // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
                            List<Long> remainOrderInfoProductIdList = orderInProducts
                                    .stream()
                                    .map(eachOrderInProduct ->
                                            jpaQueryFactory
                                                    .select(product.productId)
                                                    .from(product)
                                                    .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                                            .and(product.status.eq("Y")))
                                                    .fetchOne()
                                    )
                                    .collect(Collectors.toList());

                            // 판매 이력이 존재한 제품의 경우 우선 넣기
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.productId.in(remainOrderInfoProductIdList)
                                            .and(product.expressionCheck.eq("Y"))
                                            .and(product.status.eq("Y"))
                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice)))
                                            )
                                    )
                                    .fetch();

                            // 판매 이력이 없을 경우 최신 순으로 누적 저장
                            products.addAll(
                                    jpaQueryFactory
                                            .selectFrom(product)
                                            .where(product.productId.notIn(remainOrderInfoProductIdList)
                                                    .and(product.productId.in(labelOfProductIds))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.productId.in(labelOfProductIds))
                                                            .and(product.status.eq("Y"))
                                                            .and(product.expressionCheck.eq("Y"))
                                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                            )
                                                    )
                                            )
                                            .orderBy(product.createdAt.desc())
                                            .fetch()
                            );
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.productId.in(labelOfProductIds)
                                            .and(product.expressionCheck.eq("Y"))
                                            .and(product.status.eq("Y"))
                                            .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                            .or(product.productId.in(labelOfProductIds)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.categoryInBrandId.in(relatedCategoryAndBrandIds))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                    )
                                            )
                                    )
                                    .orderBy(product.createdAt.desc())
                                    .fetch();
                        }

                    }

                    // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
                    maxPrice = products.stream()
                            .map(eachProduct -> {
                                if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                                    return eachProduct.getEventPrice();
                                } else {
                                    return eachProduct.getSellPrice();
                                }
                            })
                            .max(Integer::compare)
                            .orElse(0);

                    // 라벨 제품 갯수
                    totalLabelProductCount = products.size();

                    // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
                    List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                            .selectFrom(categoryInBrand)
                            .where(categoryInBrand.categoryInBrandId.in(
                                    products.stream()
                                            .map(Product::getCategoryInBrandId)
                                            .collect(Collectors.toList())
                            ))
                            .fetch();

                    List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getBrandId)
                            .distinct()
                            .collect(Collectors.toList());

                    brandList = jpaQueryFactory
                            .selectFrom(brand)
                            .where(brand.brandId.in(relatedBrandIdList))
                            .fetch()
                            .stream()
                            .map(eachBrand ->
                                    BrandDataResponseDto.builder()
                                            .brandId(eachBrand.getBrandId())
                                            .brandTitle(eachBrand.getBrandTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
                    // # 대분류
                    List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory1Id)
                            .distinct()
                            .collect(Collectors.toList());

                    upCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedUpCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachUpCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachUpCategory.getCategoryId())
                                            .categoryName(eachUpCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 중분류
                    List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory2Id)
                            .distinct()
                            .collect(Collectors.toList());

                    middleCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedMiddleCategoryIds))
                            .fetch()
                            .stream()
                            .map(eachMiddleCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachMiddleCategory.getCategoryId())
                                            .categoryName(eachMiddleCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 소분류
                    List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory3Id)
                            .distinct()
                            .collect(Collectors.toList());

                    downCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedDownCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachDownCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachDownCategory.getCategoryId())
                                            .categoryName(eachDownCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 라벨 데이터 추출 로직 ]
                    List<Long> relatedTotalLabelList = jpaQueryFactory
                            .select(labelOfProduct.labelId)
                            .from(labelOfProduct)
                            .where(labelOfProduct.productId.in(
                                    products.stream()
                                            .map(Product::getProductId)
                                            .collect(Collectors.toList())
                            ))
                            .groupBy(labelOfProduct.labelId)
                            .fetch();

                    labelList = jpaQueryFactory
                            .selectFrom(label)
                            .where(label.labelId.in(relatedTotalLabelList)
                                    .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                            .fetch()
                            .stream()
                            .map(eachLabel ->
                                    LabelResponseDto.builder()
                                            .labelId(eachLabel.getLabelId())
                                            .labelTitle(eachLabel.getLabelTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    if (products.size() >= 20) {
                        if ((page * 20) <= products.size()) {
                            labelProducts = products.subList((page * 20) - 20, page * 20);
                        } else {
                            labelProducts = products.subList((page * 20) - 20, products.size());
                        }
                    } else {
                        labelProducts = products.subList((page * 20) - 20, products.size());
                    }

                }

            } else { // 브랜드, 중분류 정렬 기준을 반영하지 않는다면 해당 조건을 제외하고 나머지 검색 조건으로 제품들 추출

                // 고객 유형이 일반일 경우 C, A 타입의 신 제품들 추출
                if (loginMemberType.equals("C")) {

                    List<Product> products = new ArrayList<>();

                    if (sort <= 3) {
                        products = jpaQueryFactory
                                .selectFrom(product)
                                .where((product.productId.in(labelOfProductIds)
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(product.status.eq("Y"))
                                        .and(product.sellClassification.eq("C"))
                                        .and(betweenPrice(startRangePrice, endRangePrice)))
                                        .or(product.productId.in(labelOfProductIds)
                                                .and(product.expressionCheck.eq("Y"))
                                                .and(product.status.eq("Y"))
                                                .and(product.sellClassification.eq("C"))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                )
                                        )
                                )
                                .orderBy(orderBySort(sort))
                                .fetch();
                    } else {

                        List<Tuple> orderInProducts = jpaQueryFactory
                                .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                                .from(orderInProduct)
                                .groupBy(orderInProduct.productClassificationCode)
                                .orderBy(orderInProduct.productTotalAmount.sum().desc())
                                .fetch();

                        if (!orderInProducts.isEmpty()) {
                            // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
                            List<Long> remainOrderInfoProductIdList = orderInProducts
                                    .stream()
                                    .map(eachOrderInProduct ->
                                            jpaQueryFactory
                                                    .select(product.productId)
                                                    .from(product)
                                                    .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                                            .and(product.status.eq("Y")))
                                                    .fetchOne()
                                    )
                                    .collect(Collectors.toList());

                            // 판매 이력이 존재한 제품의 경우 우선 넣기
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productId.in(remainOrderInfoProductIdList)
                                            .and(product.productId.in(labelOfProductIds)
                                                    .and(product.status.eq("Y"))
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.sellClassification.eq("C"))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))
                                            ))
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.productId.in(labelOfProductIds)
                                                            .and(product.status.eq("Y"))
                                                            .and(product.expressionCheck.eq("Y"))
                                                            .and(product.sellClassification.eq("C"))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                            )
                                                    )
                                            )
                                    )
                                    .fetch();

                            // 판매 이력이 없을 경우 최신 순으로 누적 저장
                            products.addAll(
                                    jpaQueryFactory
                                            .selectFrom(product)
                                            .where((product.productId.notIn(remainOrderInfoProductIdList)
                                                    .and(product.productId.in(labelOfProductIds)
                                                            .and(product.status.eq("Y"))
                                                            .and(product.expressionCheck.eq("Y"))
                                                            .and(product.sellClassification.eq("C"))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    ))
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.productId.in(labelOfProductIds)
                                                                    .and(product.status.eq("Y"))
                                                                    .and(product.expressionCheck.eq("Y"))
                                                                    .and(product.sellClassification.eq("C"))
                                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                                    )
                                                            )
                                                    )
                                            )
                                            .orderBy(product.createdAt.desc())
                                            .fetch()
                            );
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productId.in(labelOfProductIds)
                                            .and(product.expressionCheck.eq("Y"))
                                            .and(product.status.eq("Y"))
                                            .and(product.sellClassification.eq("C"))
                                            .and(betweenPrice(startRangePrice, endRangePrice)))
                                            .or(product.productId.in(labelOfProductIds)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.sellClassification.eq("C"))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                    )
                                            )
                                    )
                                    .orderBy(product.createdAt.desc())
                                    .fetch();
                        }

                    }

                    // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
                    maxPrice = products.stream()
                            .map(eachProduct -> {
                                if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                                    return eachProduct.getEventPrice();
                                } else {
                                    return eachProduct.getSellPrice();
                                }
                            })
                            .max(Integer::compare)
                            .orElse(0);

                    // 라벨 제품 갯수
                    totalLabelProductCount = products.size();

                    // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
                    List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                            .selectFrom(categoryInBrand)
                            .where(categoryInBrand.categoryInBrandId.in(
                                    products.stream()
                                            .map(Product::getCategoryInBrandId)
                                            .collect(Collectors.toList())
                            ))
                            .fetch();

                    List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getBrandId)
                            .distinct()
                            .collect(Collectors.toList());

                    brandList = jpaQueryFactory
                            .selectFrom(brand)
                            .where(brand.brandId.in(relatedBrandIdList))
                            .fetch()
                            .stream()
                            .map(eachBrand ->
                                    BrandDataResponseDto.builder()
                                            .brandId(eachBrand.getBrandId())
                                            .brandTitle(eachBrand.getBrandTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
                    // # 대분류
                    List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory1Id)
                            .distinct()
                            .collect(Collectors.toList());

                    upCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedUpCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachUpCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachUpCategory.getCategoryId())
                                            .categoryName(eachUpCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 중분류
                    List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory2Id)
                            .distinct()
                            .collect(Collectors.toList());

                    middleCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedMiddleCategoryIds))
                            .fetch()
                            .stream()
                            .map(eachMiddleCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachMiddleCategory.getCategoryId())
                                            .categoryName(eachMiddleCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 소분류
                    List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory3Id)
                            .distinct()
                            .collect(Collectors.toList());

                    downCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedDownCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachDownCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachDownCategory.getCategoryId())
                                            .categoryName(eachDownCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 라벨 데이터 추출 로직 ]
                    List<Long> relatedTotalLabelList = jpaQueryFactory
                            .select(labelOfProduct.labelId)
                            .from(labelOfProduct)
                            .where(labelOfProduct.productId.in(
                                    products.stream()
                                            .map(Product::getProductId)
                                            .collect(Collectors.toList())
                            ))
                            .groupBy(labelOfProduct.labelId)
                            .fetch();

                    labelList = jpaQueryFactory
                            .selectFrom(label)
                            .where(label.labelId.in(relatedTotalLabelList)
                                    .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                            .fetch()
                            .stream()
                            .map(eachLabel ->
                                    LabelResponseDto.builder()
                                            .labelId(eachLabel.getLabelId())
                                            .labelTitle(eachLabel.getLabelTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    if (products.size() >= 20) {
                        if ((page * 20) <= products.size()) {
                            labelProducts = products.subList((page * 20) - 20, page * 20);
                        } else {
                            labelProducts = products.subList((page * 20) - 20, products.size());
                        }
                    } else {
                        labelProducts = products.subList((page * 20) - 20, products.size());
                    }

                } else if (loginMemberType.equals("B")) { // 고객 유형이 기업일 경우 B, A 타입의 신 제품들 추출

                    List<Product> products = new ArrayList<>();

                    if (sort <= 3) {
                        products = jpaQueryFactory
                                .selectFrom(product)
                                .where((product.productId.in(labelOfProductIds)
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(product.status.eq("Y"))
                                        .and(betweenPrice(startRangePrice, endRangePrice)))
                                        .or(product.productId.in(labelOfProductIds)
                                                .and(product.expressionCheck.eq("Y"))
                                                .and(product.status.eq("Y"))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                )
                                        )
                                )
                                .orderBy(orderBySort(sort))
                                .fetch();
                    } else {

                        List<Tuple> orderInProducts = jpaQueryFactory
                                .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                                .from(orderInProduct)
                                .groupBy(orderInProduct.productClassificationCode)
                                .orderBy(orderInProduct.productTotalAmount.sum().desc())
                                .fetch();

                        if (!orderInProducts.isEmpty()) {
                            // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
                            List<Long> remainOrderInfoProductIdList = orderInProducts
                                    .stream()
                                    .map(eachOrderInProduct ->
                                            jpaQueryFactory
                                                    .select(product.productId)
                                                    .from(product)
                                                    .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                                            .and(product.status.eq("Y")))
                                                    .fetchOne()
                                    )
                                    .collect(Collectors.toList());

                            // 판매 이력이 존재한 제품의 경우 우선 넣기
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productId.in(remainOrderInfoProductIdList)
                                            .and(product.productId.in(labelOfProductIds)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(betweenPrice(startRangePrice, endRangePrice))))
                                            .or(product.productId.in(remainOrderInfoProductIdList)
                                                    .and(product.productId.in(labelOfProductIds)
                                                            .and(product.expressionCheck.eq("Y"))
                                                            .and(product.status.eq("Y"))
                                                            .and(product.eventStartDate.before(LocalDateTime.now())
                                                                    .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                    .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                            )
                                                    )
                                            )
                                    )
                                    .fetch();

                            // 판매 이력이 없을 경우 최신 순으로 누적 저장
                            products.addAll(
                                    jpaQueryFactory
                                            .selectFrom(product)
                                            .where((product.productId.notIn(remainOrderInfoProductIdList)
                                                    .and(product.productId.in(labelOfProductIds)
                                                            .and(product.expressionCheck.eq("Y"))
                                                            .and(product.status.eq("Y"))
                                                            .and(betweenPrice(startRangePrice, endRangePrice))
                                                    ))
                                                    .or(product.productId.notIn(remainOrderInfoProductIdList)
                                                            .and(product.productId.in(labelOfProductIds)
                                                                    .and(product.expressionCheck.eq("Y"))
                                                                    .and(product.status.eq("Y"))
                                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                                    )
                                                            )
                                                    )
                                            )
                                            .orderBy(product.createdAt.desc())
                                            .fetch()
                            );
                        } else {
                            products = jpaQueryFactory
                                    .selectFrom(product)
                                    .where((product.productId.in(labelOfProductIds)
                                            .and(product.expressionCheck.eq("Y"))
                                            .and(product.status.eq("Y"))
                                            .and(betweenPrice(startRangePrice, endRangePrice)))
                                            .or(product.productId.in(labelOfProductIds)
                                                    .and(product.expressionCheck.eq("Y"))
                                                    .and(product.status.eq("Y"))
                                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                    )
                                            )
                                    )
                                    .orderBy(product.createdAt.desc())
                                    .fetch();
                        }

                    }

                    // 조회한 제품들 중 가격 범위 최대치로 등록할 제품의 맥시멈 가격
                    maxPrice = products.stream()
                            .map(eachProduct -> {
                                if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                                    return eachProduct.getEventPrice();
                                } else {
                                    return eachProduct.getSellPrice();
                                }
                            })
                            .max(Integer::compare)
                            .orElse(0);

                    // 라벨 제품 갯수
                    totalLabelProductCount = products.size();

                    // [ 필터링 조건용 브랜드 데이터 추출 로직 ]
                    List<CategoryInBrand> relatedCategoryInBrandList = jpaQueryFactory
                            .selectFrom(categoryInBrand)
                            .where(categoryInBrand.categoryInBrandId.in(
                                    products.stream()
                                            .map(Product::getCategoryInBrandId)
                                            .collect(Collectors.toList())
                            ))
                            .fetch();

                    List<Long> relatedBrandIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getBrandId)
                            .distinct()
                            .collect(Collectors.toList());

                    brandList = jpaQueryFactory
                            .selectFrom(brand)
                            .where(brand.brandId.in(relatedBrandIdList))
                            .fetch()
                            .stream()
                            .map(eachBrand ->
                                    BrandDataResponseDto.builder()
                                            .brandId(eachBrand.getBrandId())
                                            .brandTitle(eachBrand.getBrandTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 중분류 카테고리 데이터 추출 로직 ]
                    // # 대분류
                    List<Long> relatedUpCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory1Id)
                            .distinct()
                            .collect(Collectors.toList());

                    upCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedUpCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachUpCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachUpCategory.getCategoryId())
                                            .categoryName(eachUpCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 중분류
                    List<Long> relatedMiddleCategoryIds = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory2Id)
                            .distinct()
                            .collect(Collectors.toList());

                    middleCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedMiddleCategoryIds))
                            .fetch()
                            .stream()
                            .map(eachMiddleCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachMiddleCategory.getCategoryId())
                                            .categoryName(eachMiddleCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // # 소분류
                    List<Long> relatedDownCategoryIdList = relatedCategoryInBrandList.stream()
                            .map(CategoryInBrand::getCategory3Id)
                            .distinct()
                            .collect(Collectors.toList());

                    downCategoryList = jpaQueryFactory
                            .selectFrom(category)
                            .where(category.categoryId.in(relatedDownCategoryIdList))
                            .fetch()
                            .stream()
                            .map(eachDownCategory ->
                                    RelatedCategoryDataResponseDto.builder()
                                            .categoryId(eachDownCategory.getCategoryId())
                                            .categoryName(eachDownCategory.getCategoryName())
                                            .build()
                            )
                            .collect(Collectors.toList());


                    // [ 필터링 조건용 라벨 데이터 추출 로직 ]
                    List<Long> relatedTotalLabelList = jpaQueryFactory
                            .select(labelOfProduct.labelId)
                            .from(labelOfProduct)
                            .where(labelOfProduct.productId.in(
                                    products.stream()
                                            .map(Product::getProductId)
                                            .collect(Collectors.toList())
                            ))
                            .groupBy(labelOfProduct.labelId)
                            .fetch();

                    labelList = jpaQueryFactory
                            .selectFrom(label)
                            .where(label.labelId.in(relatedTotalLabelList)
                                    .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                            .fetch()
                            .stream()
                            .map(eachLabel ->
                                    LabelResponseDto.builder()
                                            .labelId(eachLabel.getLabelId())
                                            .labelTitle(eachLabel.getLabelTitle())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    if (products.size() >= 20) {
                        if ((page * 20) <= products.size()) {
                            labelProducts = products.subList((page * 20) - 20, page * 20);
                        } else {
                            labelProducts = products.subList((page * 20) - 20, products.size());
                        }
                    } else {
                        labelProducts = products.subList((page * 20) - 20, products.size());
                    }

                }
            }
        }

        // 최종적으로 확인하기 위한 반환 리스트 선언
        List<LabelProductResponseDto> getLabelProductList = new ArrayList<>();

        // 검색된 제품들이 존재할 경우 검색된 제품 수량 저장 및 반환 객체에 매핑하여 리스트 저장
        if (!labelProducts.isEmpty()) {

            // 검색 제품들을 돌려 반환 객체에 맞게끔 매핑 및 Convert
            labelProducts.forEach(eachLabelProduct -> {
                ProductCreateResponseDto convertProductInfo = getProduct(eachLabelProduct, "N");

                int sellOrEventPrice = 0;

                if (convertProductInfo.getEventStartDate().isBefore(LocalDateTime.now()) && convertProductInfo.getEventEndDate().isAfter(LocalDateTime.now())) {
                    sellOrEventPrice = convertProductInfo.getEventPrice();
                } else {
                    sellOrEventPrice = convertProductInfo.getSellPrice();
                }

                getLabelProductList.add(
                        LabelProductResponseDto.builder()
                                .supplierId(convertProductInfo.getSupplierId())
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
                                .deliveryType(convertProductInfo.getDeliveryType())
                                .sellClassification(convertProductInfo.getSellClassification())
                                .expressionCheck(convertProductInfo.getExpressionCheck())
                                .normalPrice(convertProductInfo.getNormalPrice())
                                .sellPrice(sellOrEventPrice)
                                .deliveryPrice(convertProductInfo.getDeliveryPrice())
                                .purchasePrice(convertProductInfo.getPurchasePrice())
                                .eventStartDate(convertProductInfo.getEventStartDate())
                                .eventEndDate(convertProductInfo.getEventEndDate())
                                .eventDescription(convertProductInfo.getEventDescription())
                                .optionCheck(convertProductInfo.getOptionCheck())
                                .productOptionList(convertProductInfo.getProductOptionList())
                                .productDetailInfo(convertProductInfo.getProductDetailInfo())
                                .mediaList(convertProductInfo.getMediaList())
                                .manufacturer(convertProductInfo.getManufacturer())
                                .madeInOrigin(convertProductInfo.getMadeInOrigin())
                                .consignmentStore(convertProductInfo.getConsignmentStore())
                                .memo(convertProductInfo.getMemo())
                                .status(convertProductInfo.getStatus())
                                .build()
                );
            });
        }

        return TotalLabelProductPageResponseDto.builder()
                .totalLabelProductCount(totalLabelProductCount)
                .maxPrice(maxPrice)
                .labelImgUrl(callLabel.getImgUrl())
                .labelProductList(getLabelProductList)
                .brandList(brandList)
                .labelList(labelList)
                .upCategoryList(upCategoryList)
                .middleCategoryList(middleCategoryList)
                .downCategoryList(downCategoryList)
                .build();
    }


    // 제품 상세 페이지 정보 조회
    //@Async("threadPoolTaskExecutor")
    public ProductDetailPageResponseDto productDetailPageInfo(Long productId) {
        // 제품 id를 가진 제품 호출
        Product callProduct = jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(productId))
                .fetchOne();

        // 제품이 존재하지 않을 경우 null 반환
        if (callProduct == null) {
            return null;
        }

        // 제품이 가지고 있는 카테고리 및 브랜드 매핑 정보의 id를 기준으로 매핑 정보 추출
        CategoryInBrand getCategoryInBrand = jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.eq(callProduct.getCategoryInBrandId()))
                .fetchOne();

        assert getCategoryInBrand != null;

        // 추출한 카테고리 및 브랜드 매핑 정보에 해당되는 브랜드 id를 기준으로 브랜드 데이터 호출
        Brand getBrand = jpaQueryFactory
                .selectFrom(brand)
                .where(brand.brandId.eq(getCategoryInBrand.getBrandId()))
                .fetchOne();

        // 브랜드 id 초기 값 설정 (브랜드가 존재하지 않을 경우 초기 값으로 반환)
        Long brandId = 0L;
        // 브랜드 명 초기 값 설정 (브랜드가 존재하지 않을 경우 초기 값으로 반환)
        String brandTitle = "";

        // 브랜드가 존재할 경우 진입
        if (getBrand != null) {
            // 브랜드 id, 브랜드 명을 설정
            brandId = getBrand.getBrandId();
            brandTitle = getBrand.getBrandTitle();
        }

        // 카테고리 및 브랜드 매핑 정보에 해당되는 대분류 카테고리 데이터 호출
        Category upCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory1Id()))
                .fetchOne();

        // 카테고리 및 브랜드 매핑 정보에 해당되는 중분류 카테고리 데이터 호출
        Category middleCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory2Id()))
                .fetchOne();

        // 카테고리 및 브랜드 매핑 정보에 해당되는 소분류 카테고리 데이터 호출
        Category downCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory3Id()))
                .fetchOne();

        // 제품에 해당되는 라벨 및 제품 매핑 정보들의 id 리스트 추출
        List<Long> getLabelOfProduct = jpaQueryFactory
                .select(labelOfProduct.labelId)
                .from(labelOfProduct)
                .where(labelOfProduct.productId.eq(callProduct.getProductId()))
                .fetch();

        // 라벨 정보들을 담을 리스트 생성
        List<LabelDataResponseDto> labelList = new ArrayList<>();

        // 라벨 및 제품 매핑 정보들의 id 리스트가 존재할 경우 진입
        if (!getLabelOfProduct.isEmpty()) {
            // id 리스트를 조회하여 라벨 데이터 처리
            getLabelOfProduct.forEach(eachLabelOfProduct -> {
                // 라벨 데이터 호출
                Label getLabel = jpaQueryFactory
                        .selectFrom(label)
                        .where(label.labelId.eq(eachLabelOfProduct)
                                .and(label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now()))))
                        .fetchOne();

                if (getLabel != null) {
                    // 리스트에 라벨 데이터 정보 저장
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

        // 제품에 해당되는 제품과 옵션 매핑 정보 리스트 호출
        List<ProductOfOption> getProductOfOptions = jpaQueryFactory
                .selectFrom(productOfOption)
                .where(productOfOption.productId.eq(callProduct.getProductId()))
                .fetch();

        // 제품 옵션 정보들을 담을 리스트 생성
        List<ProductOptionCreateResponseDto> productOptionList = new ArrayList<>();

        // 제품과 옵션 매핑 정보 리스트가 존재할 경우 진입
        if (!getProductOfOptions.isEmpty()) {
            // 매핑 정보 리스트를 조회하며 옵션 데이터 처리
            getProductOfOptions.forEach(eachProductOfOption -> {
                // 연관된 제품 옵션 호출
                ProductOption getProductOption = jpaQueryFactory
                        .selectFrom(productOption)
                        .where(productOption.productOptionId.eq(eachProductOfOption.getProductOptionId()))
                        .fetchOne();

                // 제품 옵션에 연관된 제품 상세 옵션 리스트 호출
                List<ProductDetailOption> getProductDetailOptions = jpaQueryFactory
                        .selectFrom(productDetailOption)
                        .where(productDetailOption.productOption.eq(getProductOption))
                        .fetch();

                // 제품 상세 옵션 정보들을 담을 리스트 생성
                List<ProductDetailOptionCreateResponseDto> productDetailOptionList = new ArrayList<>();

                // 제품 상세 옵션이 존재할 경우 진입
                if (!getProductDetailOptions.isEmpty()) {
                    // 제품 상세 옵션들을 조회하며 리스트에 저장 처리
                    getProductDetailOptions.forEach(eachProductDetailOption -> {
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

                // 제품 옵션 및 상세 옵션 정보들 리스트에 저장
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

        // 제품의 상세 정보 내용 추출
        String productDetailInfoContent = jpaQueryFactory
                .select(productDetailInfo.content)
                .from(productDetailInfo)
                .where(productDetailInfo.productId.eq(callProduct.getProductId()))
                .fetchOne();

        // 제품에 속한 제품 이미지 및 제품 상세 정보 이미지 데이터들 호출
        List<Media> getMediaList = jpaQueryFactory
                .selectFrom(media)
                .where((media.type.eq("product").or(media.type.eq("productdetail")))
                        .and(media.mappingContentId.eq(callProduct.getProductId())))
                .fetch();

        // 제품 이미지 정보들을 담을 리스트 생성
        List<MediaResponseDto> productImageList = new ArrayList<>();
        // 제품 상세 정보 이미지들을 담을 리스트 생성
        List<ProductDetailImageInfoResponseDto> productDetailInfoImageList = new ArrayList<>();

        if (callProduct.getRelateImgIds() != null) {
            List<Long> RelateImgIdsList = convertStringToList(callProduct.getRelateImgIds());

            productImageList.addAll(
                    RelateImgIdsList.stream()
                            .map(eachRelateImage -> {
                                Media eachImage = jpaQueryFactory
                                        .selectFrom(media)
                                        .where((media.mediaId.eq(eachRelateImage))
                                                .and(media.mappingContentId.eq(callProduct.getProductId())))
                                        .fetchOne();

                                return MediaResponseDto.builder()
                                        .mediaId(eachImage.getMediaId())
                                        .imgUploadUrl(eachImage.getImgUploadUrl())
                                        .imgUrl(eachImage.getImgUrl())
                                        .imgTitle(eachImage.getImgTitle())
                                        .imgUuidTitle(eachImage.getImgUuidTitle())
                                        .representCheck(eachImage.getRepresentCheck())
                                        .build();
                            })
                            .collect(Collectors.toList())
            );
        } else {
            // 만약 제품에 연관된 이미지 데이터들이 존재할 경우 진입
            if (!getMediaList.isEmpty()) {

                // 이미지 데이터들을 조회하여 데이터처리
                getMediaList.forEach(eachMedia -> {
                    // 만약 이미지 데이터 타입이 product 일 경우 제품 이미지로 판단하여 처리
                    if (eachMedia.getType().equals("product")) {

                        // if()

                        productImageList.add(
                                MediaResponseDto.builder()
                                        .mediaId(eachMedia.getMediaId())
                                        .imgUploadUrl(eachMedia.getImgUploadUrl())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .imgTitle(eachMedia.getImgTitle())
                                        .imgUuidTitle(eachMedia.getImgUuidTitle())
                                        .representCheck(eachMedia.getRepresentCheck())
                                        .build()
                        );
                    } else if (eachMedia.getType().equals("productdetail")) {
                        // 만약 이미지 데이터 타입이 productdetail 일 경우 제품 상세 정보 이미지로 판단하여 처리
                        productDetailInfoImageList.add(
                                ProductDetailImageInfoResponseDto.builder()
                                        .productDetailImageId(eachMedia.getMediaId())
                                        .type(eachMedia.getType())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .build()
                        );
                    }
                });

            }
        }

        // 제품에 속한 공급사 데이터 호출
        Supplier relatedSupplier = jpaQueryFactory
                .selectFrom(supplier)
                .where(supplier.supplierId.eq(callProduct.getSupplierId()))
                .fetchOne();

        int sellOrEventPrice = 0;

        if (callProduct.getEventStartDate().isBefore(LocalDateTime.now()) && callProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
            sellOrEventPrice = callProduct.getEventPrice();
        } else {
            sellOrEventPrice = callProduct.getSellPrice();
        }

        return ProductDetailPageResponseDto.builder()
                .supplierId(callProduct.getSupplierId())
                .supplierCompany(relatedSupplier.getSupplierCompany())
                .businessNumber(relatedSupplier.getBusinessNumber())
                .frcNumber(relatedSupplier.getFrcNumber())
                .represent(relatedSupplier.getRepresent())
                .address(relatedSupplier.getAddress())
                .recallAddress(relatedSupplier.getRecalladdress())
                .tel(relatedSupplier.getTel())
                .csCall(relatedSupplier.getCscall())
                .csInfo(relatedSupplier.getCsInfo())
                .personInCharge(relatedSupplier.getPersonInCharge())
                .contactCall(relatedSupplier.getContactCall())
                .email(relatedSupplier.getEmail())
                .brandId(brandId)
                .brand(brandTitle)
                .upCategoryId(upCategory.getCategoryId())
                .upCategory(upCategory.getCategoryName())
                .middleCategoryId(middleCategory.getCategoryId())
                .middleCategory(middleCategory.getCategoryName())
                .downCategoryId(downCategory.getCategoryId())
                .downCategory(downCategory.getCategoryName())
                .productId(callProduct.getProductId())
                .productName(callProduct.getProductName())
                .classificationCode(callProduct.getClassificationCode())
                .labelList(labelList)
                .modelNumber(callProduct.getModelNumber())
                .deliveryType(callProduct.getDeliveryType())
                .sellClassification(callProduct.getSellClassification())
                .expressionCheck(callProduct.getExpressionCheck())
                .normalPrice(callProduct.getNormalPrice())
                .sellPrice(sellOrEventPrice)
                .deliveryPrice(callProduct.getDeliveryPrice())
                .purchasePrice(callProduct.getPurchasePrice())
                .eventStartDate(callProduct.getEventStartDate())
                .eventEndDate(callProduct.getEventEndDate())
                .eventDescription(callProduct.getEventDescription())
                .optionCheck(callProduct.getOptionCheck())
                .productOptionList(productOptionList)
                .productDetailInfo(productDetailInfoContent)
                .mediaList(productImageList)
                .productDetailInfoImages(productDetailInfoImageList)
                .manufacturer(callProduct.getManufacturer())
                .madeInOrigin(callProduct.getMadeInOrigin())
                .consignmentStore(callProduct.getConsignmentStore())
                .memo(callProduct.getMemo())
                .status(callProduct.getStatus())
                .build();
    }


    // 제품 생성 등록 시 사전에 먼저 생성된 상세 정보 이미지들의 mappingContentId를 생성 등록한 제품의 id로 업데이트 시키기 위한 쿼리 함수
    @Transactional(transactionManager = "MasterTransactionManager")
    public void updateProductDetailInfoImagesMappingId(Product createProduct, List<Long> productDetailInfoImageIds, List<Media> createMedias) {

        List<Long> createImageIdLIst = createMedias.stream()
                .map(Media::getMediaId)
                .collect(Collectors.toList());

        jpaQueryFactory
                .update(product)
                .set(product.relateImgIds, createImageIdLIst.toString())
                .where(product.productId.eq(createProduct.getProductId()))
                .execute();


        if (!productDetailInfoImageIds.isEmpty()) {
            // 제품 상세 정보 이미지들 id 리스트를 조회하며 media 데이터 업데이트 처리
            productDetailInfoImageIds.forEach(eachDetailInfoImageId -> {
                // 이미지와 연관된 매핑 컨텐츠의 id 수정
                jpaQueryFactory
                        .update(media)
                        .set(media.mappingContentId, createProduct.getProductId())
                        .where(media.mediaId.eq(eachDetailInfoImageId))
                        .execute();
            });

            entityManager.flush();
            entityManager.clear();

            // 제품 상세 정보 이미지들 id 리스트를 조회하며 실제 media 데이터 추출
            List<Media> updateProductDetailImageMedias = productDetailInfoImageIds.stream()
                    .map(eachUpdateDetailInfoImageId ->
                            jpaQueryFactory
                                    .selectFrom(media)
                                    .where(media.mediaId.eq(eachUpdateDetailInfoImageId))
                                    .fetchOne()
                    )
                    .collect(Collectors.toList());

            // 연관된 제품과 이미지 파일을 저장한 정보를 담고 있는 ProductOfMedia들을 한번에 저장하기 위한 리스트
            List<ProductOfMedia> relatedProductOfMediaList = new ArrayList<>();

            // 저장된 Media 정보들을 기준으로 relatedProductOfMediaList 리스트에 담기
            updateProductDetailImageMedias.forEach(eachDetailInfoMedia -> {
                relatedProductOfMediaList.add(
                        ProductOfMedia.builder()
                                .productId(eachDetailInfoMedia.getMappingContentId())
                                .mediaId(eachDetailInfoMedia.getMediaId())
                                .build());
            });

            // ProductOfMedia 한 번에 저장
            productOfMediaRepository.saveAll(relatedProductOfMediaList);
        } else {
            entityManager.flush();
            entityManager.clear();
        }

    }


    // 메인 페이지 카테고리 베스트 제품 리스트 조회
    public List<MainPageCategoryBestProductResponseDto> getCategoryBestProducts(String loginMemberType, Long categoryId) {

        List<MainPageCategoryBestProductResponseDto> categoryBestProductsResponseDto = new ArrayList<>();

        // 선택한 대분류 카테고리에 따른 연관된 CategoryInBrand id 리스트 추출
        List<Long> relatedCategoryInBrandIdList = jpaQueryFactory
                .select(categoryInBrand.categoryInBrandId)
                .from(categoryInBrand)
                .where(categoryInBrand.category1Id.eq(categoryId))
                .fetch();

        // BEST 결제 구매 이력 정보 조회
        List<String> orderInProducts = jpaQueryFactory
                .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                .from(orderInProduct)
                .groupBy(orderInProduct.productClassificationCode)
                .orderBy(orderInProduct.productTotalAmount.sum().desc())
                .fetch()
                .stream()
                .map(eachOrderInProductInfo ->
                        eachOrderInProductInfo.get(orderInProduct.productClassificationCode)
                )
                .collect(Collectors.toList());

        // BEST 결제 구매 이력 정보가 존재할 경우 해당 제품들을 리스트에 우선 저장
        if (!orderInProducts.isEmpty()) {
            // !!!! 지금은 베스트를 측정할 수 있는 판단 데이터가 존재하지 않기 때문에 우선 기본적으로 최신 순으로 각 카테고리의 6개의 제품들을 리스트화 하여 노출
            // 추출한 카테고리와 제품의 매핑 정보에 속한 제품들을 추출하며 최종 반환 객체 리스트에 추가 필요 처리 로직 수행
            categoryBestProductsResponseDto = jpaQueryFactory
                    .selectFrom(product)
                    .where(product.categoryInBrandId.in(relatedCategoryInBrandIdList)
                            .and(product.expressionCheck.eq("Y"))
                            .and(product.status.eq("Y"))
                            .and(product.classificationCode.in(orderInProducts))
                            .and(checkProductType(loginMemberType)))
                    .limit(8)
                    .fetch()
                    .stream()
                    .map(this::getRelateCategoryBestProductInfo)
                    .collect(Collectors.toList());

        }


        // BEST 결제 구매 이력 제품들이 존재하고 만약 그 제품들 개수가 6개 이하일 경우 나머지 채워야할 제품 데이터를 최신순 저장
        if (categoryBestProductsResponseDto.size() != 8 && !categoryBestProductsResponseDto.isEmpty()) {

            List<Long> existBESTProductIdList = categoryBestProductsResponseDto.stream()
                    .map(MainPageCategoryBestProductResponseDto::getProductId)
                    .collect(Collectors.toList());

            categoryBestProductsResponseDto.addAll(
                    // 추출한 카테고리와 제품의 매핑 정보에 속한 제품들을 추출하며 최종 반환 객체 리스트에 추가 필요 처리 로직 수행
                    jpaQueryFactory
                            .selectFrom(product)
                            .where(product.categoryInBrandId.in(relatedCategoryInBrandIdList)
                                    .and(product.expressionCheck.eq("Y"))
                                    .and(product.status.eq("Y"))
                                    .and(notExistBESTProducts(existBESTProductIdList))
                                    .and(checkProductType(loginMemberType)))
                            .orderBy(product.createdAt.desc()) // !!!! 지금은 베스트를 측정할 수 있는 판단 데이터가 존재하지 않기 때문에 우선 기본적으로 최신 순으로 각 카테고리의 6개의 제품들을 리스트화 하여 노출
                            .limit(8 - categoryBestProductsResponseDto.size())
                            .fetch()
                            .stream()
                            .map(this::getRelateCategoryBestProductInfo)
                            .collect(Collectors.toList())
            );

        } else {

            categoryBestProductsResponseDto.addAll(
                    // 추출한 카테고리와 제품의 매핑 정보에 속한 제품들을 추출하며 최종 반환 객체 리스트에 추가 필요 처리 로직 수행
                    jpaQueryFactory
                            .selectFrom(product)
                            .where(product.categoryInBrandId.in(relatedCategoryInBrandIdList)
                                    .and(product.expressionCheck.eq("Y"))
                                    .and(product.status.eq("Y"))
                                    .and(checkProductType(loginMemberType)))
                            .orderBy(product.createdAt.desc()) // !!!! 지금은 베스트를 측정할 수 있는 판단 데이터가 존재하지 않기 때문에 우선 기본적으로 최신 순으로 각 카테고리의 6개의 제품들을 리스트화 하여 노출
                            .limit(8)
                            .fetch()
                            .stream()
                            .map(this::getRelateCategoryBestProductInfo)
                            .collect(Collectors.toList())
            );
        }

        return categoryBestProductsResponseDto;
    }


    // BEST 관련 제품 정보 추출 함수
    private MainPageCategoryBestProductResponseDto getRelateCategoryBestProductInfo(Product eachProduct) {
        // 각 제품에 연관된 카테고리 및 브랜드 매핑 정보 추출
        CategoryInBrand relateCategoryInBrand = jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.eq(eachProduct.getCategoryInBrandId()))
                .fetchOne();

        assert relateCategoryInBrand != null;

        // 연관된 브랜드 명 추출
        Tuple brandInfo = jpaQueryFactory
                .select(brand.brandId, brand.brandTitle)
                .from(brand)
                .where(brand.brandId.eq(relateCategoryInBrand.getBrandId()))
                .fetchOne();

        String brandName = "";
        Long brandId = 0L;

        // 브랜드가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (brandInfo != null) {
            brandId = brandInfo.get(brand.brandId);
            brandName = brandInfo.get(brand.brandTitle);
        }

        // 연관된 대분류 카테고리 명 추출
        Tuple upCategoryInfo = jpaQueryFactory
                .select(category.categoryId, category.categoryName)
                .from(category)
                .where(category.categoryId.eq(relateCategoryInBrand.getCategory1Id()))
                .fetchOne();

        String upCategoryName = "";
        Long upCategoryId = 0L;

        // 연관된 중분류 카테고리 명 추출
        Tuple middleCategoryInfo = jpaQueryFactory
                .select(category.categoryId, category.categoryName)
                .from(category)
                .where(category.categoryId.eq(relateCategoryInBrand.getCategory2Id()))
                .fetchOne();

        String middleCategoryName = "";
        Long middleCategoryId = 0L;

        // 연관된 소분류 카테고리 명 추출
        Tuple downCategoryInfo = jpaQueryFactory
                .select(category.categoryId, category.categoryName)
                .from(category)
                .where(category.categoryId.eq(relateCategoryInBrand.getCategory3Id()))
                .fetchOne();

        String downCategoryName = "";
        Long downCategoryId = 0L;

        // 대분류 카테고리가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (upCategoryInfo != null) {
            upCategoryId = upCategoryInfo.get(category.categoryId);
            upCategoryName = upCategoryInfo.get(category.categoryName);
        }

        // 중분류 카테고리가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (middleCategoryInfo != null) {
            middleCategoryId = middleCategoryInfo.get(category.categoryId);
            middleCategoryName = middleCategoryInfo.get(category.categoryName);
        }

        // 소분류 카테고리가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (downCategoryInfo != null) {
            downCategoryId = downCategoryInfo.get(category.categoryId);
            downCategoryName = downCategoryInfo.get(category.categoryName);
        }

        // 판매가 추출
        int price = eachProduct.getSellPrice();

        // 만약 이벤트 진행 중이고 해당 기간 내의 제품일 경우 price를 이벤트 가격으로 처리
        if (eachProduct.getEventStartDate() != null && eachProduct.getEventEndDate() != null && eachProduct.getEventPrice() != 0) {
            if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                price = eachProduct.getEventPrice();
            }
        }

        // 제품이 가지고 있는 라벨 id 리스트
        List<Long> labelIdList = jpaQueryFactory
                .select(labelOfProduct.labelId)
                .from(labelOfProduct)
                .where(labelOfProduct.productId.eq(eachProduct.getProductId()))
                .fetch();

        // 라벨 id 리스트에 속한 라벨 정보를 추출하고 반환 객체에 넣을 수 있게끔 가공하여 저장
        List<LabelDataResponseDto> labelList = jpaQueryFactory
                .selectFrom(label)
                .where(label.labelId.in(labelIdList)
                        .and(label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))
                )
                .fetch()
                .stream()
                .map(eachLabel ->
                        LabelDataResponseDto.builder()
                                .labelId(eachLabel.getLabelId())
                                .labelTitle(eachLabel.getLabelTitle())
                                .colorCode(eachLabel.getColorCode())
                                .startPostDate(eachLabel.getStartPostDate())
                                .endPostDate(eachLabel.getEndPostDate())
                                .imgUrl(eachLabel.getImgUrl())
                                .topExpression(eachLabel.getTopExpression())
                                .build()
                )
                .collect(Collectors.toList());

        // 제품에 속한 제품 옵션 id 리스트 추출
        List<Long> productOptionIdList = jpaQueryFactory
                .select(productOfOption.productOptionId)
                .from(productOfOption)
                .where(productOfOption.productId.eq(eachProduct.getProductId()))
                .fetch();

        // 제품 옵션 정보 리스트 추출 및 처리 저장
        List<CategoryBestProductOptionResponseDto> productOptionList = jpaQueryFactory
                .selectFrom(productOption)
                .where(productOption.productOptionId.in(productOptionIdList))
                .fetch()
                .stream()
                .map(eachProductOption -> {

                    // 제품의 상세 옵션 정보 리스트 추출 및 저장
                    List<CategoryBestProductDetailOptionResponseDto> productDetailOptionList = jpaQueryFactory
                            .selectFrom(productDetailOption)
                            .where(productDetailOption.productOption.eq(eachProductOption))
                            .fetch()
                            .stream()
                            .map(eachProductDetailOption ->
                                    CategoryBestProductDetailOptionResponseDto.builder()
                                            .detailOptionId(eachProductDetailOption.getProductDetailOptionId())
                                            .detailOptionName(eachProductDetailOption.getDetailOptionName())
                                            .optionPrice(eachProductDetailOption.getOptionPrice())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    return CategoryBestProductOptionResponseDto.builder()
                            .productOptionId(eachProductOption.getProductOptionId())
                            .productOptionTitle(eachProductOption.getOptionTitle())
                            .necessaryCheck(eachProductOption.getNecessaryCheck())
                            .productDetailOptionList(productDetailOptionList)
                            .build();
                })
                .collect(Collectors.toList());

        // 제품이 가지고 있는 제품 이미지 정보 추출
        List<Media> realMediaList = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(eachProduct.getProductId())
                        .and((media.type.eq("product"))))
                .fetch();

        // 제품 상세 정보 이미지를 저장할 리스트 생성
        List<MediaResponseDto> mediaList = new ArrayList<>();

        if (eachProduct.getRelateImgIds() != null) {
            List<Long> RelateImgIdsList = convertStringToList(eachProduct.getRelateImgIds());

            mediaList.addAll(
                    RelateImgIdsList.stream()
                            .map(eachRelateImage -> {
                                Media eachImage = jpaQueryFactory
                                        .selectFrom(media)
                                        .where((media.mediaId.eq(eachRelateImage))
                                                .and(media.mappingContentId.eq(eachProduct.getProductId())))
                                        .fetchOne();

                                return MediaResponseDto.builder()
                                        .mediaId(eachImage.getMediaId())
                                        .imgUploadUrl(eachImage.getImgUploadUrl())
                                        .imgUrl(eachImage.getImgUrl())
                                        .imgTitle(eachImage.getImgTitle())
                                        .imgUuidTitle(eachImage.getImgUuidTitle())
                                        .representCheck(eachImage.getRepresentCheck())
                                        .build();
                            })
                            .collect(Collectors.toList())
            );
        } else {
            // 만약 제품에 연관된 이미지들이 존재할 경우 진입
            if (!realMediaList.isEmpty()) {
                realMediaList.forEach(eachMedia -> {
                    // 제품 이미지 정보 반환 데이터 저장
                    if (eachMedia.getType().equals("product")) {
                        mediaList.add(
                                MediaResponseDto.builder()
                                        .mediaId(eachMedia.getMediaId())
                                        .imgUploadUrl(eachMedia.getImgUploadUrl())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .imgTitle(eachMedia.getImgTitle())
                                        .imgUuidTitle(eachMedia.getImgUuidTitle())
                                        .representCheck(eachMedia.getRepresentCheck())
                                        .build()
                        );
                    }
                });
            }
        }

        return MainPageCategoryBestProductResponseDto.builder()
                .supplierId(eachProduct.getSupplierId())
                .brandId(brandId)
                .brand(brandName)
                .upCategoryId(upCategoryId)
                .upCategory(upCategoryName)
                .middleCategoryId(middleCategoryId)
                .middleCategory(middleCategoryName)
                .downCategoryId(downCategoryId)
                .downCategory(downCategoryName)
                .productId(eachProduct.getProductId())
                .productName(eachProduct.getProductName())
                .classificationCode(eachProduct.getClassificationCode())
                .labelList(labelList)
                .modelNumber(eachProduct.getModelNumber())
                .deliveryType(eachProduct.getDeliveryType())
                .deliveryPrice(eachProduct.getDeliveryPrice())
                .sellClassification(eachProduct.getSellClassification())
                .expressionCheck(eachProduct.getExpressionCheck())
                .sellPrice(price)
                .normalPrice(eachProduct.getNormalPrice())
                .eventStartDate(eachProduct.getEventStartDate())
                .eventEndDate(eachProduct.getEventEndDate())
                .eventDescription(eachProduct.getEventDescription())
                .optionCheck(eachProduct.getOptionCheck())
                .productOptionList(productOptionList)
                .mediaList(mediaList)
                .manufacturer(eachProduct.getManufacturer())
                .madeInOrigin(eachProduct.getMadeInOrigin())
                .consignmentStore(eachProduct.getConsignmentStore())
                .memo(eachProduct.getMemo())
                .status(eachProduct.getStatus())
                .build();
    }


    // 로그인한 고객의 유형에 따른 제품 노출 유형 조건
    private BooleanExpression checkProductType(String memberType) {
        if (memberType.equals("C")) {
            return product.sellClassification.eq("C");
        }

        return null;
    }


    // 메인 페이지 Weekly 베스트 제품 리스트 조회
    public List<MainPageWeeklyBestProductResponseDto> getWeeklyBestProducts(String loginMemberType) {

        List<MainPageWeeklyBestProductResponseDto> weeklyBestProductsResponseDto = new ArrayList<>();

        // BEST 결제 구매 이력 정보 조회
        List<String> orderInProducts = jpaQueryFactory
                .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                .from(orderInProduct, payment)
                .where(orderInProduct.orderNumber.eq(payment.orderNumber)
                        .and(payment.orderedAt.before(LocalDateTime.now()).and(payment.orderedAt.after(LocalDateTime.now().minusDays(7))))
                )
                .groupBy(orderInProduct.productClassificationCode)
                .orderBy(orderInProduct.productTotalAmount.sum().desc())
                .limit(12)
                .fetch()
                .stream()
                .map(eachOrderInProductInfo ->
                        eachOrderInProductInfo.get(orderInProduct.productClassificationCode)
                )
                .collect(Collectors.toList());

        // BEST 결제 구매 이력 정보가 존재할 경우 해당되는 제품들을 리스트에 우선 저장
        if (!orderInProducts.isEmpty()) {
            // 추출한 카테고리와 제품의 매핑 정보에 속한 제품들을 추출하며 최종 반환 객체 리스트에 추가 필요 처리 로직 수행
            weeklyBestProductsResponseDto = jpaQueryFactory
                    .selectFrom(product)
                    .where(product.classificationCode.in(orderInProducts)
                            .and(product.expressionCheck.eq("Y"))
                            .and(product.status.eq("Y"))
                            .and(checkProductType(loginMemberType)))
                    .limit(12)
                    .fetch()
                    .stream()
                    .map(this::getRelateWeeklyBestProductInfo)
                    .collect(Collectors.toList());
        }

        // 판매 BEST 제품들이 8개 보다 적을 경우 최신 제품들을 노출
        if (!weeklyBestProductsResponseDto.isEmpty() && weeklyBestProductsResponseDto.size() <= 8) {
            List<Long> existBESTProductIdList = weeklyBestProductsResponseDto.stream()
                    .map(MainPageWeeklyBestProductResponseDto::getProductId)
                    .collect(Collectors.toList());

            weeklyBestProductsResponseDto.addAll(
                    // 추출한 카테고리와 제품의 매핑 정보에 속한 제품들을 추출하며 최종 반환 객체 리스트에 추가 필요 처리 로직 수행
                    jpaQueryFactory
                            .selectFrom(product)
                            .where(product.productId.goe(1L)
                                    .and(product.expressionCheck.eq("Y"))
                                    .and(product.status.eq("Y"))
                                    .and(checkProductType(loginMemberType))
                                    .and(notExistBESTProducts(existBESTProductIdList)))
                            .orderBy(product.createdAt.desc())
                            .limit(12 - weeklyBestProductsResponseDto.size())
                            .fetch()
                            .stream()
                            .map(this::getRelateWeeklyBestProductInfo)
                            .collect(Collectors.toList())
            );
        } else {
            weeklyBestProductsResponseDto.addAll(
                    // 추출한 카테고리와 제품의 매핑 정보에 속한 제품들을 추출하며 최종 반환 객체 리스트에 추가 필요 처리 로직 수행
                    jpaQueryFactory
                            .selectFrom(product)
                            .where(product.status.eq("Y")
                                    .and(product.expressionCheck.eq("Y"))
                                    .and(checkProductType(loginMemberType)))
                            .orderBy(product.createdAt.desc())
                            .limit(12)
                            .fetch()
                            .stream()
                            .map(this::getRelateWeeklyBestProductInfo)
                            .collect(Collectors.toList())
            );
        }

        return weeklyBestProductsResponseDto;
    }


    // 메인 페이지에 노출될 Weekly BEST 제품들 정보 추출 함수
    private MainPageWeeklyBestProductResponseDto getRelateWeeklyBestProductInfo(Product eachProduct) {
        // 각 제품에 연관된 카테고리 및 브랜드 매핑 정보 추출
        CategoryInBrand relateCategoryInBrand = jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.eq(eachProduct.getCategoryInBrandId()))
                .fetchOne();

        assert relateCategoryInBrand != null;

        // 연관된 브랜드 명 추출
        Tuple brandInfo = jpaQueryFactory
                .select(brand.brandId, brand.brandTitle)
                .from(brand)
                .where(brand.brandId.eq(relateCategoryInBrand.getBrandId()))
                .fetchOne();

        String brandName = "";
        Long brandId = 0L;

        // 브랜드가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (brandInfo != null) {
            brandId = brandInfo.get(brand.brandId);
            brandName = brandInfo.get(brand.brandTitle);
        }

        // 연관된 대분류 카테고리 명 추출
        Tuple upCategoryInfo = jpaQueryFactory
                .select(category.categoryId, category.categoryName)
                .from(category)
                .where(category.categoryId.eq(relateCategoryInBrand.getCategory1Id()))
                .fetchOne();

        String upCategoryName = "";
        Long upCategoryId = 0L;

        // 연관된 중분류 카테고리 명 추출
        Tuple middleCategoryInfo = jpaQueryFactory
                .select(category.categoryId, category.categoryName)
                .from(category)
                .where(category.categoryId.eq(relateCategoryInBrand.getCategory2Id()))
                .fetchOne();

        String middleCategoryName = "";
        Long middleCategoryId = 0L;

        // 연관된 소분류 카테고리 명 추출
        Tuple downCategoryInfo = jpaQueryFactory
                .select(category.categoryId, category.categoryName)
                .from(category)
                .where(category.categoryId.eq(relateCategoryInBrand.getCategory3Id()))
                .fetchOne();

        String downCategoryName = "";
        Long downCategoryId = 0L;

        // 대분류 카테고리가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (upCategoryInfo != null) {
            upCategoryId = upCategoryInfo.get(category.categoryId);
            upCategoryName = upCategoryInfo.get(category.categoryName);
        }

        // 중분류 카테고리가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (middleCategoryInfo != null) {
            middleCategoryId = middleCategoryInfo.get(category.categoryId);
            middleCategoryName = middleCategoryInfo.get(category.categoryName);
        }

        // 소분류 카테고리가 삭제되거나 다른 이유로 존재하지 않을 시 반환 데이터 공백 초기화 후 반환
        if (downCategoryInfo != null) {
            downCategoryId = downCategoryInfo.get(category.categoryId);
            downCategoryName = downCategoryInfo.get(category.categoryName);
        }

        // 판매가 추출
        int price = eachProduct.getSellPrice();

        // 만약 이벤트 진행 중이고 해당 기간 내의 제품일 경우 price를 이벤트 가격으로 처리
        if (eachProduct.getEventStartDate() != null && eachProduct.getEventEndDate() != null && eachProduct.getEventPrice() != 0) {
            if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                price = eachProduct.getEventPrice();
            }
        }

        // 제품이 가지고 있는 라벨 id 리스트
        List<Long> labelIdList = jpaQueryFactory
                .select(labelOfProduct.labelId)
                .from(labelOfProduct)
                .where(labelOfProduct.productId.eq(eachProduct.getProductId()))
                .fetch();

        // 라벨 id 리스트에 속한 라벨 정보를 추출하고 반환 객체에 넣을 수 있게끔 가공하여 저장
        List<LabelDataResponseDto> labelList = jpaQueryFactory
                .selectFrom(label)
                .where(label.labelId.in(labelIdList)
                        .and(label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))
                )
                .fetch()
                .stream()
                .map(eachLabel ->
                        LabelDataResponseDto.builder()
                                .labelId(eachLabel.getLabelId())
                                .labelTitle(eachLabel.getLabelTitle())
                                .colorCode(eachLabel.getColorCode())
                                .startPostDate(eachLabel.getStartPostDate())
                                .endPostDate(eachLabel.getEndPostDate())
                                .imgUrl(eachLabel.getImgUrl())
                                .topExpression(eachLabel.getTopExpression())
                                .build()
                )
                .collect(Collectors.toList());

        // 제품에 속한 제품 옵션 id 리스트 추출
        List<Long> productOptionIdList = jpaQueryFactory
                .select(productOfOption.productOptionId)
                .from(productOfOption)
                .where(productOfOption.productId.eq(eachProduct.getProductId()))
                .fetch();

        // 제품 옵션 정보 리스트 추출 및 처리 저장
        List<CategoryBestProductOptionResponseDto> productOptionList = jpaQueryFactory
                .selectFrom(productOption)
                .where(productOption.productOptionId.in(productOptionIdList))
                .fetch()
                .stream()
                .map(eachProductOption -> {

                    // 제품의 상세 옵션 정보 리스트 추출 및 저장
                    List<CategoryBestProductDetailOptionResponseDto> productDetailOptionList = jpaQueryFactory
                            .selectFrom(productDetailOption)
                            .where(productDetailOption.productOption.eq(eachProductOption))
                            .fetch()
                            .stream()
                            .map(eachProductDetailOption ->
                                    CategoryBestProductDetailOptionResponseDto.builder()
                                            .detailOptionId(eachProductDetailOption.getProductDetailOptionId())
                                            .detailOptionName(eachProductDetailOption.getDetailOptionName())
                                            .optionPrice(eachProductDetailOption.getOptionPrice())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    return CategoryBestProductOptionResponseDto.builder()
                            .productOptionId(eachProductOption.getProductOptionId())
                            .productOptionTitle(eachProductOption.getOptionTitle())
                            .necessaryCheck(eachProductOption.getNecessaryCheck())
                            .productDetailOptionList(productDetailOptionList)
                            .build();
                })
                .collect(Collectors.toList());

        // 제품이 가지고 있는 제품 이미지 정보 추출
        List<Media> realMediaList = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(eachProduct.getProductId())
                        .and((media.type.eq("product"))))
                .fetch();

        List<MediaResponseDto> mediaList = new ArrayList<>();

        if (eachProduct.getRelateImgIds() != null) {
            List<Long> RelateImgIdsList = convertStringToList(eachProduct.getRelateImgIds());

            mediaList.addAll(
                    RelateImgIdsList.stream()
                            .map(eachRelateImage -> {
                                Media eachImage = jpaQueryFactory
                                        .selectFrom(media)
                                        .where((media.mediaId.eq(eachRelateImage))
                                                .and(media.mappingContentId.eq(eachProduct.getProductId())))
                                        .fetchOne();

                                return MediaResponseDto.builder()
                                        .mediaId(eachImage.getMediaId())
                                        .imgUploadUrl(eachImage.getImgUploadUrl())
                                        .imgUrl(eachImage.getImgUrl())
                                        .imgTitle(eachImage.getImgTitle())
                                        .imgUuidTitle(eachImage.getImgUuidTitle())
                                        .representCheck(eachImage.getRepresentCheck())
                                        .build();
                            })
                            .collect(Collectors.toList())
            );
        } else {
            // 만약 제품에 연관된 이미지들이 존재할 경우 진입
            if (!realMediaList.isEmpty()) {
                realMediaList.forEach(eachMedia -> {
                    // 제품 이미지 정보 반환 데이터 저장
                    if (eachMedia.getType().equals("product")) {
                        mediaList.add(
                                MediaResponseDto.builder()
                                        .mediaId(eachMedia.getMediaId())
                                        .imgUploadUrl(eachMedia.getImgUploadUrl())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .imgTitle(eachMedia.getImgTitle())
                                        .imgUuidTitle(eachMedia.getImgUuidTitle())
                                        .representCheck(eachMedia.getRepresentCheck())
                                        .build()
                        );
                    }
                });
            }
        }

        return MainPageWeeklyBestProductResponseDto.builder()
                .supplierId(eachProduct.getSupplierId())
                .brandId(brandId)
                .brand(brandName)
                .upCategoryId(upCategoryId)
                .upCategory(upCategoryName)
                .middleCategoryId(middleCategoryId)
                .middleCategory(middleCategoryName)
                .downCategoryId(downCategoryId)
                .downCategory(downCategoryName)
                .productId(eachProduct.getProductId())
                .productName(eachProduct.getProductName())
                .classificationCode(eachProduct.getClassificationCode())
                .labelList(labelList)
                .modelNumber(eachProduct.getModelNumber())
                .deliveryType(eachProduct.getDeliveryType())
                .deliveryPrice(eachProduct.getDeliveryPrice())
                .sellClassification(eachProduct.getSellClassification())
                .expressionCheck(eachProduct.getExpressionCheck())
                .sellPrice(price)
                .normalPrice(eachProduct.getNormalPrice())
                .eventStartDate(eachProduct.getEventStartDate())
                .eventEndDate(eachProduct.getEventEndDate())
                .eventDescription(eachProduct.getEventDescription())
                .optionCheck(eachProduct.getOptionCheck())
                .productOptionList(productOptionList)
                .mediaList(mediaList)
                .manufacturer(eachProduct.getManufacturer())
                .madeInOrigin(eachProduct.getMadeInOrigin())
                .consignmentStore(eachProduct.getConsignmentStore())
                .memo(eachProduct.getMemo())
                .build();
    }


    // BEST 판매 이력 존재 제품들이 리스트에 존재하는지 아닌지 판별하는 조건
    private BooleanExpression notExistBESTProducts(List<Long> existBESTProductIdList) {
        if (!existBESTProductIdList.isEmpty()) {
            return product.productId.notIn(existBESTProductIdList);
        } else {
            return null;
        }
    }


    public static List<Long> convertStringToList(String str) {
        // 문자열에서 대괄호 제거
        str = str.replace("[", "").replace("]", "");

        // 결과 리스트
        List<Long> list = new ArrayList<>();

        if (str.contains(",")) {
            // 쉼표로 분리
            String[] parts = str.split(",");

            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    // 각 부분을 Long으로 변환하고 리스트에 추가
                    list.add(Long.parseLong(part.trim()));
                }
            }
        } else {
            list.add(Long.parseLong(str.trim()));
        }

        return list;
    }


    // 브랜드관 제품 리스트 호출
    public TotalBrandProductPageResponseDto brandPageMainProducts(
            String loginMemberType, Long brandId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> labelIdList, List<Long> relatedMiddleCategoryIdList) {

        // 접근 플랫폼에 따른 브랜드 이미지 호출 경로 추출
        String brandImgUrl = jpaQueryFactory
                .select(media.imgUrl)
                .from(media)
                .where(media.type.like("%brand%")
                        .and(media.mappingContentId.eq(brandId)))
                .fetchOne();

        List<Product> brandProductList = new ArrayList<>();

        // 정렬 기준 조건이 (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순)에 해당할 경우 정렬
        if (sort <= 3) {
            // 브랜드관 제품들 호출
            brandProductList = jpaQueryFactory
                    .selectFrom(product)
                    .where((product.categoryInBrandId.in(
                                    jpaQueryFactory
                                            .select(categoryInBrand.categoryInBrandId)
                                            .from(categoryInBrand)
                                            .where(categoryInBrand.brandId.eq(brandId)
                                                    .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                            .fetch())
                            .and(product.status.eq("Y"))
                            .and(product.expressionCheck.eq("Y"))
                            .and(checkProductType(loginMemberType))
                            .and(eqLabelOfProduct(labelIdList))
                            .and(betweenPrice(startRangePrice, endRangePrice)))
                            .or(product.categoryInBrandId.in(
                                            jpaQueryFactory
                                                    .select(categoryInBrand.categoryInBrandId)
                                                    .from(categoryInBrand)
                                                    .where(categoryInBrand.brandId.eq(brandId)
                                                            .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                    .fetch())
                                    .and(product.status.eq("Y"))
                                    .and(product.expressionCheck.eq("Y"))
                                    .and(checkProductType(loginMemberType))
                                    .and(eqLabelOfProduct(labelIdList))
                                    .and(product.eventStartDate.before(LocalDateTime.now())
                                            .and(product.eventEndDate.after(LocalDateTime.now()))
                                            .and(betweenEventPrice(startRangePrice, endRangePrice))
                                    )
                            )
                    )
                    .orderBy(orderBySort(sort))
                    .fetch();

        } else { // 정렬 기준 조건이 4 - 누적 판매 순일 경우 정렬
            // 판매 이력이 존재한 제품 코드를 정렬하여 제품 id로 추출
            List<Long> remainOrderInfoProductIdList = jpaQueryFactory
                    .select(orderInProduct.productClassificationCode, orderInProduct.productTotalAmount.sum())
                    .from(orderInProduct)
                    .groupBy(orderInProduct.productClassificationCode)
                    .orderBy(orderInProduct.productTotalAmount.sum().desc())
                    .fetch()
                    .stream()
                    .map(eachOrderInProduct ->
                            jpaQueryFactory
                                    .select(product.productId)
                                    .from(product)
                                    .where(product.classificationCode.eq(eachOrderInProduct.get(orderInProduct.productClassificationCode))
                                            .and(product.status.eq("Y")))
                                    .fetchOne()
                    )
                    .collect(Collectors.toList());

            // 만약 판매 이력이 존재하지 않을 경우,
            if (remainOrderInfoProductIdList.isEmpty()) {
                // 일반적인 최신 순 브랜드관 제품들 호출
                brandProductList = jpaQueryFactory
                        .selectFrom(product)
                        .where((product.categoryInBrandId.in(
                                        jpaQueryFactory
                                                .select(categoryInBrand.categoryInBrandId)
                                                .from(categoryInBrand)
                                                .where(categoryInBrand.brandId.eq(brandId)
                                                        .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                .fetch())
                                .and(product.status.eq("Y"))
                                .and(product.expressionCheck.eq("Y"))
                                .and(checkProductType(loginMemberType))
                                .and(eqLabelOfProduct(labelIdList))
                                .and(betweenPrice(startRangePrice, endRangePrice))
                                .and(product.productId.in(remainOrderInfoProductIdList)))
                                .or(product.categoryInBrandId.in(
                                                jpaQueryFactory
                                                        .select(categoryInBrand.categoryInBrandId)
                                                        .from(categoryInBrand)
                                                        .where(categoryInBrand.brandId.eq(brandId)
                                                                .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                        .fetch())
                                        .and(product.status.eq("Y"))
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(checkProductType(loginMemberType))
                                        .and(eqLabelOfProduct(labelIdList))
                                        .and(product.eventStartDate.before(LocalDateTime.now())
                                                .and(product.eventEndDate.after(LocalDateTime.now()))
                                                .and(betweenEventPrice(startRangePrice, endRangePrice))
                                        )
                                        .and(product.productId.in(remainOrderInfoProductIdList))
                                )
                        )
                        .orderBy(product.createdAt.desc())
                        .fetch();

            } else { // 만약 판매, 결제, 구매 이력이 존재할 경우,

                // 먼저 판매 이력이 존재한 제품들을 추출하여 구매 이력 순으로 나열 후 리스트에 저장
                brandProductList.addAll(
                        jpaQueryFactory
                                .selectFrom(product)
                                .where((product.categoryInBrandId.in(
                                                jpaQueryFactory
                                                        .select(categoryInBrand.categoryInBrandId)
                                                        .from(categoryInBrand)
                                                        .where(categoryInBrand.brandId.eq(brandId)
                                                                .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                        .fetch())
                                        .and(product.status.eq("Y"))
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(checkProductType(loginMemberType))
                                        .and(eqLabelOfProduct(labelIdList))
                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                        .and(product.productId.in(remainOrderInfoProductIdList)))
                                        .or(product.categoryInBrandId.in(
                                                        jpaQueryFactory
                                                                .select(categoryInBrand.categoryInBrandId)
                                                                .from(categoryInBrand)
                                                                .where(categoryInBrand.brandId.eq(brandId)
                                                                        .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                                .fetch())
                                                .and(product.status.eq("Y"))
                                                .and(product.expressionCheck.eq("Y"))
                                                .and(checkProductType(loginMemberType))
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                )
                                                .and(product.productId.in(remainOrderInfoProductIdList))
                                        )
                                )
                                .fetch()
                );

                // 판매 이력 존재 제품들을 제외한 나머지 제품들을 리스트에 저장
                brandProductList.addAll(
                        jpaQueryFactory
                                .selectFrom(product)
                                .where((product.categoryInBrandId.in(
                                                jpaQueryFactory
                                                        .select(categoryInBrand.categoryInBrandId)
                                                        .from(categoryInBrand)
                                                        .where(categoryInBrand.brandId.eq(brandId)
                                                                .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                        .fetch())
                                        .and(product.status.eq("Y"))
                                        .and(product.expressionCheck.eq("Y"))
                                        .and(checkProductType(loginMemberType))
                                        .and(eqLabelOfProduct(labelIdList))
                                        .and(betweenPrice(startRangePrice, endRangePrice))
                                        .and(product.productId.notIn(remainOrderInfoProductIdList)))
                                        .or(product.categoryInBrandId.in(
                                                        jpaQueryFactory
                                                                .select(categoryInBrand.categoryInBrandId)
                                                                .from(categoryInBrand)
                                                                .where(categoryInBrand.brandId.eq(brandId)
                                                                        .and(eqRelatedMiddleCategory(relatedMiddleCategoryIdList)))
                                                                .fetch())
                                                .and(product.status.eq("Y"))
                                                .and(product.expressionCheck.eq("Y"))
                                                .and(checkProductType(loginMemberType))
                                                .and(eqLabelOfProduct(labelIdList))
                                                .and(product.eventStartDate.before(LocalDateTime.now())
                                                        .and(product.eventEndDate.after(LocalDateTime.now()))
                                                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                                                )
                                                .and(product.productId.notIn(remainOrderInfoProductIdList))
                                        )
                                )
                                .orderBy(product.createdAt.desc())
                                .fetch()
                );
            }

        }

        // 브랜드에 속하는 카테고리 id 정보 호출
        List<Long> relateCategoryId = brandProductList.stream()
                .map(Product::getCategoryInBrandId)
                .distinct()
                .collect(Collectors.toList());

        // 브랜드에 해당되는 라벨 정보들 호출
        List<Long> brandProductIdList = brandProductList.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());

        // 총 브랜드관 제품 수
        int totalBrandProductCount = brandProductList.size();

        // 추출한 브랜드관 제품들 페이징
        if (brandProductList.size() >= 20) {
            if ((page * 20) <= brandProductList.size()) {
                brandProductList = brandProductList.subList((page * 20) - 20, page * 20);
            } else {
                brandProductList = brandProductList.subList((page * 20) - 20, brandProductList.size());
            }
        } else {
            brandProductList = brandProductList.subList((page * 20) - 20, brandProductList.size());
        }


        // 브랜드관 상품들 반환객체로 변환
        List<BrandProductResponseDto> brandProducts = brandProductList.stream()
                .map(eachProduct -> {
                    ProductCreateResponseDto convertProductInfo = getProduct(eachProduct, "N");

                    int sellOrEventPrice = 0;

                    // 이벤트 기간 중인 제품이라면 해당 이벤트 가격이 범위 가격 내에 존재하면 해당 이벤트 가격을 판매 가격으로
                    if (convertProductInfo.getEventStartDate().isBefore(LocalDateTime.now()) && convertProductInfo.getEventEndDate().isAfter(LocalDateTime.now())
                            && convertProductInfo.getEventPrice() >= startRangePrice && convertProductInfo.getEventPrice() <= endRangePrice) {
                        sellOrEventPrice = convertProductInfo.getEventPrice();
                    } else {
                        sellOrEventPrice = convertProductInfo.getSellPrice();
                    }

                    return BrandProductResponseDto.builder()
                            .supplierId(convertProductInfo.getSupplierId())
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
                            .deliveryType(convertProductInfo.getDeliveryType())
                            .sellClassification(convertProductInfo.getSellClassification())
                            .expressionCheck(convertProductInfo.getExpressionCheck())
                            .normalPrice(convertProductInfo.getNormalPrice())
                            .sellPrice(sellOrEventPrice)
                            .deliveryPrice(convertProductInfo.getDeliveryPrice())
                            .purchasePrice(convertProductInfo.getPurchasePrice())
                            .eventStartDate(convertProductInfo.getEventStartDate())
                            .eventEndDate(convertProductInfo.getEventEndDate())
                            .eventDescription(convertProductInfo.getEventDescription())
                            .optionCheck(convertProductInfo.getOptionCheck())
                            .productOptionList(convertProductInfo.getProductOptionList())
                            .productDetailInfo(convertProductInfo.getProductDetailInfo())
                            .mediaList(convertProductInfo.getMediaList())
                            .manufacturer(convertProductInfo.getManufacturer())
                            .madeInOrigin(convertProductInfo.getMadeInOrigin())
                            .consignmentStore(convertProductInfo.getConsignmentStore())
                            .memo(convertProductInfo.getMemo())
                            .status(convertProductInfo.getStatus())
                            .build();
                })
                .collect(Collectors.toList());


        // 최대 가격
        int maxPrice = brandProductList.stream()
                .map(eachProduct -> {
                    if (eachProduct.getEventStartDate().isBefore(LocalDateTime.now()) && eachProduct.getEventEndDate().isAfter(LocalDateTime.now())) {
                        return eachProduct.getEventPrice();
                    } else {
                        return eachProduct.getSellPrice();
                    }
                })
                .max(Integer::compare)
                .orElse(0);

        // 브랜드 제품들에 속한 라벨 정보들 추출
        List<LabelResponseDto> labelList = jpaQueryFactory
                .selectFrom(label)
                .where(label.labelId.in(
                        jpaQueryFactory
                                .select(labelOfProduct.labelId)
                                .from(labelOfProduct)
                                .where(labelOfProduct.productId.in(brandProductIdList))
                                .groupBy(labelOfProduct.labelId)
                                .fetch()))
                .fetch()
                .stream()
                .map(eachLabel ->
                        LabelResponseDto.builder()
                                .labelId(eachLabel.getLabelId())
                                .labelTitle(eachLabel.getLabelTitle())
                                .build()
                ).collect(Collectors.toList());

        // 브랜드 제품들에 속한 중분류 카테고리 정보들 추출
        List<RelatedCategoryDataResponseDto> middleCategoryList = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryGroup.eq(1)
                        .and(category.categoryId.in(
                                        jpaQueryFactory
                                                .select(categoryInBrand.category2Id)
                                                .from(categoryInBrand)
                                                .where(categoryInBrand.categoryInBrandId.in(relateCategoryId))
                                                .groupBy(categoryInBrand.category2Id)
                                                .fetch()
                                )
                        )
                )
                .fetch()
                .stream()
                .map(eachMiddleCategory ->
                        RelatedCategoryDataResponseDto.builder()
                                .categoryId(eachMiddleCategory.getCategoryId())
                                .categoryName(eachMiddleCategory.getCategoryName())
                                .build()
                )
                .collect(Collectors.toList());


        return TotalBrandProductPageResponseDto.builder()
                .totalBrandProductCount((long) totalBrandProductCount)
                .maxPrice(maxPrice)
                .brandId(brandId)
                .brandImgUrl(brandImgUrl)
                .brandProductList(brandProducts)
                .labelList(labelList)
                .middleCategoryList(middleCategoryList)
                .build();
    }


    // 브랜드관 제품들 추출 시 관련된 중분류 카테고리 동적 조건
    private BooleanExpression eqRelatedMiddleCategory(List<Long> relatedMiddleCategoryIdList) {

        if (!relatedMiddleCategoryIdList.isEmpty()) {
            return categoryInBrand.category2Id.in(relatedMiddleCategoryIdList);
        }

        return null;
    }


    // 브랜드관 제품들 추출 시 이벤트 진행 중인 제품 및 해당 이벤트 가격 일치 여부 동적 조건
    private BooleanExpression inEventDateRange(int startRangePrice, int endRangePrice) {
        if (jpaQueryFactory
                .select(product.count())
                .from(product)
                .where(product.eventStartDate.before(LocalDateTime.now())
                        .and(product.eventEndDate.after(LocalDateTime.now()))
                        .and(betweenEventPrice(startRangePrice, endRangePrice))
                )
                .fetchOne() != null) {

            return product.eventStartDate.before(LocalDateTime.now())
                    .and(product.eventEndDate.after(LocalDateTime.now()))
                    .and(betweenEventPrice(startRangePrice, endRangePrice));
        }

        return null;
    }
}
