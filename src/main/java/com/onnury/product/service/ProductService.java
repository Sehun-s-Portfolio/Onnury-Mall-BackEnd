package com.onnury.product.service;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.category.domain.CategoryInBrand;
import com.onnury.category.repository.CategoryInBrandRepository;
import com.onnury.common.util.LogUtil;
import com.onnury.exception.product.ProductException;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.label.domain.LabelOfProduct;
import com.onnury.label.repository.LabelOfProductRepository;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUpload;
import com.onnury.member.domain.Member;
import com.onnury.product.domain.*;
import com.onnury.product.repository.*;
import com.onnury.product.request.ProductCreateRequestDto;
import com.onnury.product.request.ProductSearchRequestDto;
import com.onnury.product.request.ProductUpdateRequestDto;
import com.onnury.product.response.*;
import com.onnury.query.category.CategoryQueryData;
import com.onnury.query.product.ProductQueryData;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.product.domain.QProduct.product;
import static com.onnury.supplier.domain.QSupplier.supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final JwtTokenException jwtTokenException;
    private final ProductException productException;
    private final ProductQueryData productQueryData;
    private final ProductRepository productRepository;
    private final LabelOfProductRepository labelOfProductRepository;
    private final CategoryInBrandRepository categoryInBrandRepository;
    private final ProductDetailInfoRepository productDetailInfoRepository;
    private final ProductOfMediaRepository productOfMediaRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOfOptionRepository productOfOptionRepository;
    private final ProductDetailOptionRepository productDetailOptionRepository;
    private final MediaRepository mediaRepository;
    private final MediaUpload mediaUpload;
    private final CategoryQueryData categoryQueryData;
    private final JPAQueryFactory jpaQueryFactory;
    private final JwtTokenProvider jwtTokenProvider;

    // 제품 생성 service
    @Transactional
    public ProductCreateResponseDto createProduct(
            HttpServletRequest request,
            List<MultipartFile> productImgs,
            ProductCreateRequestDto productCreateRequestDto) throws IOException {
        log.info("제품 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 제품 생성 정보 정합성 검증
        if (productException.checkProductImages(productImgs)) {
            log.info("제품 생성 이미지 정보 존재하지 않음");
            return null;
        }

        AdminAccount loginAccount = jwtTokenProvider.getAdminAccountFromAuthentication();

        // 이전에 이미 존재한 CategoryInBrand인지 체크 및 호출
        CategoryInBrand categoryInBrand = categoryQueryData.getCategoryInBrand(
                productCreateRequestDto.getBrandId(), productCreateRequestDto.getUpCategoryId(), productCreateRequestDto.getMiddleCategoryId(), productCreateRequestDto.getDownCategoryId());

        // 만약 CategoryInBrand가 존재하지 않았었다면 우선 생성
        if (categoryInBrand == null) {
            // 제품이 등록될 카테고리에 속한 브랜드를 우선 생성
            categoryInBrand = categoryInBrandRepository.save(
                    CategoryInBrand.builder()
                            .brandId(productCreateRequestDto.getBrandId())
                            .category1Id(productCreateRequestDto.getUpCategoryId())
                            .category2Id(productCreateRequestDto.getMiddleCategoryId())
                            .category3Id(productCreateRequestDto.getDownCategoryId())
                            .build()
            );
        }

        // 제품 코드 생성
        String productClassificationCode = productQueryData.getProductClassificationCode();

        LocalDateTime eventStartDate = null;
        LocalDateTime eventEndDate = null;

        // 만약 이벤트 진행 중인 제품의 경우 이벤트 시작 일자와 마지막 일자를 추출하여 convert
        if (!productCreateRequestDto.getEventStartDate().isEmpty() && !productCreateRequestDto.getEventEndDate().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
            eventStartDate = LocalDateTime.parse(productCreateRequestDto.getEventStartDate() + " 00:00:00", formatter);
            eventEndDate = LocalDateTime.parse(productCreateRequestDto.getEventEndDate() + " 23:59:59", formatter);
        }

        // 제품 생성 정보 입력
        Product product = Product.builder()
                .supplierId(loginAccount.getType().equals("admin") ? productCreateRequestDto.getSupplierId() :
                        jpaQueryFactory
                                .select(supplier.supplierId)
                                .from(supplier)
                                .where(supplier.adminAccountId.eq(loginAccount.getAdminAccountId()))
                                .fetchOne())
                .categoryInBrandId(categoryInBrand.getCategoryInBrandId())
                .productName(productCreateRequestDto.getProductName())
                .modelNumber(productCreateRequestDto.getModelNumber())
                .classificationCode(productClassificationCode)
                .deliveryType(productCreateRequestDto.getDeliveryType())
                .sellClassification(productCreateRequestDto.getSellClassification())
                .expressionCheck(productCreateRequestDto.getExpressionCheck())
                .normalPrice(productCreateRequestDto.getNormalPrice())
                .sellPrice(productCreateRequestDto.getSellPrice())
                .purchasePrice(productCreateRequestDto.getPurchasePrice())
                .eventPrice(productCreateRequestDto.getEventPrice())
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .eventDescription(productCreateRequestDto.getEventDescription())
                .optionCheck(productCreateRequestDto.getOptionCheck())
                .deliveryPrice(productCreateRequestDto.getDeliveryPrice())
                .manufacturer(productCreateRequestDto.getManufacturer())
                .madeInOrigin(productCreateRequestDto.getMadeInOrigin())
                .consignmentStore(productCreateRequestDto.getConsignmentStore())
                .memo(productCreateRequestDto.getMemo())
                .status("Y")
                .build();

        productRepository.save(product);

        // 전달받은 이미지 파일들을 기준으로 이미지 업로드 처리 후 정보들을 추출하여 HashMap 리스트로 전달
        List<HashMap<String, String>> productImageCheckList = mediaUpload.uploadProductImage(productImgs);
        List<Media> saveMediaList = new ArrayList<>();

        // 업로드한 이미지들의 정보들을 조회하여 Media 데이터 저장 처리
        for (HashMap<String, String> eachProductImageInfo : productImageCheckList) {
            String imgUploadUrl = eachProductImageInfo.get("imgUploadUrl");
            String imgUrl = eachProductImageInfo.get("imgUrl");
            String imgTitle = eachProductImageInfo.get("imgTitle");
            String imgUuidTitle = eachProductImageInfo.get("imgUuidTitle");

            // 만약 요청받은 대표 이미지 인덱스와 일치할 경우 대표 이미지 설정
            if (productCreateRequestDto.getRepresentImageIndex() == productImageCheckList.indexOf(eachProductImageInfo)) {
                Media saveMedia = Media.builder()
                        .imgUploadUrl(imgUploadUrl)
                        .imgUrl(imgUrl)
                        .imgTitle(imgTitle)
                        .imgUuidTitle(imgUuidTitle)
                        .representCheck("Y")
                        .type("product")
                        .mappingContentId(product.getProductId())
                        .build();

                saveMediaList.add(saveMedia);
            } else {
                // 대표 이미지를 제외한 나머지 이미지 설정
                Media saveMedia = Media.builder()
                        .imgUploadUrl(imgUploadUrl)
                        .imgUrl(imgUrl)
                        .imgTitle(imgTitle)
                        .imgUuidTitle(imgUuidTitle)
                        .representCheck("N")
                        .type("product")
                        .mappingContentId(product.getProductId())
                        .build();

                saveMediaList.add(saveMedia);
            }
        }

        // 이미지 파일들 한 번에 저장
        List<Media> createMedias = mediaRepository.saveAll(saveMediaList);

        // 연관된 제품과 이미지 파일을 저장한 정보를 담고 있는 ProductOfMedia들을 한번에 저장하기 위한 리스트
        List<ProductOfMedia> relatedProductOfMediaList = new ArrayList<>();

        // 저장된 Media 정보들을 기준으로 relatedProductOfMediaList 리스트에 담기
        createMedias.forEach(eachMediaInfo -> {
            relatedProductOfMediaList.add(ProductOfMedia.builder()
                    .productId(eachMediaInfo.getMappingContentId())
                    .mediaId(eachMediaInfo.getMediaId())
                    .build());
        });

        // ProductOfMedia 한 번에 저장
        productOfMediaRepository.saveAll(relatedProductOfMediaList);

        // 제품 상세 정보 저장
        productDetailInfoRepository.save(
                ProductDetailInfo.builder()
                        .content(productCreateRequestDto.getProductDetailInfo())
                        .productId(product.getProductId())
                        .build()
        );

        // 제품 생성 등록 시 사전에 먼저 생성된 상세 정보 이미지들의 mappingContentId를 생성 등록한 제품의 id로 업데이트
        productQueryData.updateProductDetailInfoImagesMappingId(product, productCreateRequestDto.getProductDetailImageIds(), createMedias);

        // 만약 선택한 라벨들이 존재할 경우
        if (!productCreateRequestDto.getLabelList().isEmpty()) {
            List<LabelOfProduct> saveLabelOfProductList = new ArrayList<>();

            // 선택한 라벨들의 id를 기준으로 LabelOfProduct 정보 담기
            productCreateRequestDto.getLabelList().forEach(eachLabel -> {
                saveLabelOfProductList.add(
                        LabelOfProduct.builder()
                                .productId(product.getProductId())
                                .labelId(eachLabel)
                                .build()
                );
            });

            // LabelOfProduct 한 번에 저장
            labelOfProductRepository.saveAll(saveLabelOfProductList);
        }

        // 만약 생성하고자 하는 제품에 옵션이 들어갈 경우,
        if (product.getOptionCheck().equals("Y") && !productCreateRequestDto.getProductOptionList().isEmpty()) {
            // 최종적으로 제품과 옵션 연관 정보를 담아 한 번에 저장하기 위한 리스트 생성
            List<ProductOfOption> saveProductOfOptionList = new ArrayList<>();

            // 요청받은 제품의 옵션 리스트를 조회하여 옵션 정보 처리
            productCreateRequestDto.getProductOptionList().forEach(eachOption -> {

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
                                .productId(product.getProductId())
                                .productOptionId(saveProductOption.getProductOptionId())
                                .build()
                );

            });

            // ProductOfOption 일괄 저장
            productOfOptionRepository.saveAll(saveProductOfOptionList);
        }

        return productQueryData.getProduct(product, "N");
    }


    // 관리자 특정 제품 정보 호출 service
    public ProductCreateResponseDto getProduct(HttpServletRequest request, Long productId) {
        log.info("관리자 특정 제품 정보 호출 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        Product callProduct = jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(productId))
                .fetchOne();

        return productQueryData.getProduct(callProduct, "Y");
    }


    // 제품 수정 service
    @Transactional
    public ProductUpdateResponseDto updateProduct(
            HttpServletRequest request,
            List<MultipartFile> updateProductImgs,
            ProductUpdateRequestDto productUpdateRequestDto) throws IOException {
        log.info("제품 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        AdminAccount loginAccount = jwtTokenProvider.getAdminAccountFromAuthentication();

        // 새롭게 수정한 제품
        Product product = productQueryData.updateProduct(loginAccount, updateProductImgs, productUpdateRequestDto);
        ProductCreateResponseDto convertResponseDto = productQueryData.getProduct(product, "Y");

        List<ProductOptionUpdateResponseDto> productOptionList = convertResponseDto.getProductOptionList().stream()
                .map(convertProductOption ->
                        ProductOptionUpdateResponseDto.builder()
                                .productOptionId(convertProductOption.getProductOptionId())
                                .productOptionTitle(convertProductOption.getProductOptionTitle())
                                .necessaryCheck(convertProductOption.getNecessaryCheck())
                                .productDetailOptionList(
                                        convertProductOption.getProductDetailOptionList().stream()
                                                .map(convertProductDetailOption ->
                                                        ProductDetailOptionUpdateResponseDto.builder()
                                                                .detailOptionId(convertProductDetailOption.getDetailOptionId())
                                                                .detailOptionName(convertProductDetailOption.getDetailOptionName())
                                                                .optionPrice(convertProductDetailOption.getOptionPrice())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build())
                .collect(Collectors.toList());

        return ProductUpdateResponseDto.builder()
                .supplierId(convertResponseDto.getSupplierId())
                .brandId(convertResponseDto.getBrandId())
                .brand(convertResponseDto.getBrand())
                .upCategoryId(convertResponseDto.getUpCategoryId())
                .upCategory(convertResponseDto.getUpCategory())
                .middleCategoryId(convertResponseDto.getMiddleCategoryId())
                .middleCategory(convertResponseDto.getMiddleCategory())
                .downCategoryId(convertResponseDto.getDownCategoryId())
                .downCategory(convertResponseDto.getDownCategory())
                .productId(convertResponseDto.getProductId())
                .productName(convertResponseDto.getProductName())
                .deliveryType(convertResponseDto.getDeliveryType())
                .classificationCode(convertResponseDto.getClassificationCode())
                .labelList(convertResponseDto.getLabelList())
                .modelNumber(convertResponseDto.getModelNumber())
                .sellClassification(convertResponseDto.getSellClassification())
                .expressionCheck(convertResponseDto.getExpressionCheck())
                .normalPrice(convertResponseDto.getNormalPrice())
                .sellPrice(convertResponseDto.getSellPrice())
                .deliveryPrice(convertResponseDto.getDeliveryPrice())
                .purchasePrice(convertResponseDto.getPurchasePrice())
                .eventPrice(convertResponseDto.getEventPrice())
                .eventStartDate(convertResponseDto.getEventStartDate())
                .eventEndDate(convertResponseDto.getEventEndDate())
                .eventDescription(convertResponseDto.getEventDescription())
                .optionCheck(convertResponseDto.getOptionCheck())
                .productOptionList(productOptionList)
                .productDetailInfo(convertResponseDto.getProductDetailInfo())
                .mediaList(convertResponseDto.getMediaList())
                .relateImgIds(convertResponseDto.getRelateImgIds())
                .productDetailInfoImages(convertResponseDto.getProductDetailInfoImages())
                .manufacturer(convertResponseDto.getManufacturer())
                .madeInOrigin(convertResponseDto.getMadeInOrigin())
                .consignmentStore(convertResponseDto.getConsignmentStore())
                .memo(convertResponseDto.getMemo())
                .status(convertResponseDto.getStatus())
                .build();
    }


    // 제품 삭제 service
    @Transactional
    public boolean deleteProduct(
            HttpServletRequest request,
            Long productId) {
        log.info("제품 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return false;
        }

        return productQueryData.deleteProduct(productId);
    }


    // 제품 리스트 (검색) service
    public AdminTotalProductSearchResponseDto getProductsList(
            HttpServletRequest request,
            ProductSearchRequestDto productSearchRequestDto) {
        log.info("제품 리스트 (검색) service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        AdminAccount loginAccount = jwtTokenProvider.getAdminAccountFromAuthentication();

        return productQueryData.getProductsList(loginAccount, productSearchRequestDto);
    }


    // 제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출 service
    public ProductReadyInfoResponseDto getReadyForCreateProductInfo(HttpServletRequest request) {
        log.info("제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return productQueryData.getReadyForCreateProductInfo();
    }


    // 메인 페이지 신 상품 리스트 호출 service
    //@Async("threadPoolTaskExecutor")
    public List<MainPageNewReleaseProductResponseDto> getNewReleaseProducts(HttpServletRequest request) throws Exception {
        log.info("메인 페이지 신 상품 리스트 호출 service");
        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }

            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.getNewReleaseProducts(loginMember.getType());
        } else {
            return productQueryData.getNewReleaseProducts("C");
        }
    }


    // 대분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 service (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순 (# 추후 반영))
    //@Async("threadPoolTaskExecutor")
    public TotalProductPageMainProductResponseDto upCategoryPageMainProducts(
            HttpServletRequest request, Long upCategoryId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandId, List<Long> labelId, List<Long> middleCategoryId) {
        log.info("대분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 service");

        log.info("(1) upCategoryId : {} - {}", upCategoryId, upCategoryId.getClass());
        log.info("(2) sort : {}", sort);
        log.info("(3) page : {}", page);
        log.info("(4) startRangePrice : {}", startRangePrice);
        log.info("(5) endRangePrice : {}", endRangePrice);
        log.info("(6) brandId : {} - {}", brandId.toString(), brandId.getClass());
        log.info("(7) labelId : {} - {}", labelId.toString(), labelId.getClass());
        log.info("(8) middleCategoryId : {} - {}", middleCategoryId.toString(), middleCategoryId.getClass());

        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }
            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.upCategoryPageMainProducts(loginMember.getType(), upCategoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, middleCategoryId);

        } else {
            return productQueryData.upCategoryPageMainProducts("C", upCategoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, middleCategoryId);

        }
    }


    // 중분류, 소분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 service (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순 (# 추후 반영))
    //@Async("threadPoolTaskExecutor")
    public TotalProductPageMainProductResponseDto middleAndDownCategoryPageMainProducts(
            HttpServletRequest request, Long categoryId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandId, List<Long> labelId, List<Long> relatedDownCategoryId) {
        log.info("중분류, 소분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 service");
        if (request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }
            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.middleAndDownCategoryPageMainProducts(loginMember.getType(), categoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, relatedDownCategoryId);
        } else {
            return productQueryData.middleAndDownCategoryPageMainProducts("C", categoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, relatedDownCategoryId);
        }
    }

    // 고객 제품 검색 service
    public TotalProductSearchResponseDto searchProducts(
            HttpServletRequest request, int sort, String searchKeyword, int page, int startRangePrice, int endRangePrice, List<Long> brandId, List<Long> labelId, List<Long> relatedMiddleCategoryId) {
        log.info("고객 제품 검색 service");
        if (request.getHeader("RefreshToken") != null) {

            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }

            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.searchProducts(loginMember.getType(), sort, searchKeyword, page, startRangePrice, endRangePrice, brandId, labelId, relatedMiddleCategoryId);
        } else {
            return productQueryData.searchProducts("C", sort, searchKeyword, page, startRangePrice, endRangePrice, brandId, labelId, relatedMiddleCategoryId);
        }
    }

    // 라벨 기준 제품 페이지 제품 리스트 호출 service
    public TotalLabelProductPageResponseDto labelPageMainProducts(
            HttpServletRequest request, Long labelId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> brandId, List<Long> relatedMiddleCategoryId) {
        log.info("라벨 기준 제품 페이지 제품 리스트 호출 service");
        if (request.getHeader("RefreshToken") != null) {

            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }

            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.labelPageMainProducts(loginMember.getType(), labelId, sort, page, startRangePrice, endRangePrice, brandId, relatedMiddleCategoryId);
        }else{
            return productQueryData.labelPageMainProducts("C", labelId, sort, page, startRangePrice, endRangePrice, brandId, relatedMiddleCategoryId);
        }

    }

    // 제품 상세 페이지 조회 service
    //@Async("threadPoolTaskExecutor")
    public ProductDetailPageResponseDto productDetailPage(Long productId) {
        log.info("제품 상세 페이지 조회 service");

        return productQueryData.productDetailPageInfo(productId);
    }


    // 재품 상세 정보 이미지 링크 반환 service
    public List<ProductDetailImageInfoResponseDto> saveDetailImage(HttpServletRequest request, List<MultipartFile> detailImages) throws IOException {
        log.info("재품 상세 정보 이미지 링크 반환 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 전달받은 이미지 파일들을 기준으로 이미지 업로드 처리 후 정보들을 추출하여 HashMap 리스트로 전달
        List<HashMap<String, String>> saveProductDetailInfoImages = mediaUpload.uploadProductDetailInfoImage(detailImages);
        List<Media> saveMediaList = new ArrayList<>();

        // 업로드한 이미지들의 정보들을 조회하여 Media 데이터 저장 처리
        for (HashMap<String, String> eachProductDetailInfoImage : saveProductDetailInfoImages) {
            String imgUploadUrl = eachProductDetailInfoImage.get("imgUploadUrl");
            String imgUrl = eachProductDetailInfoImage.get("imgUrl");
            String imgTitle = eachProductDetailInfoImage.get("imgTitle");
            String imgUuidTitle = eachProductDetailInfoImage.get("imgUuidTitle");

            // 제품 상세 정보 이미지 정보들 기입
            Media saveMedia = Media.builder()
                    .imgUploadUrl(imgUploadUrl)
                    .imgUrl(imgUrl)
                    .imgTitle(imgTitle)
                    .imgUuidTitle(imgUuidTitle)
                    .representCheck("N")
                    .type("productdetail")
                    .mappingContentId(0L)
                    .build();

            saveMediaList.add(saveMedia);
        }

        List<Media> createProductDetailInfoImages = mediaRepository.saveAll(saveMediaList);

        return createProductDetailInfoImages.stream()
                .map(eachDetailInfoImage ->
                        ProductDetailImageInfoResponseDto.builder()
                                .productDetailImageId(eachDetailInfoImage.getMediaId())
                                .type(eachDetailInfoImage.getType())
                                .imgUrl(eachDetailInfoImage.getImgUrl())
                                .build()
                )
                .collect(Collectors.toList());
    }


    // 메인 페이지 카테고리 베스트 제품 리스트 조회 service
    //@Async("threadPoolTaskExecutor")
    public List<MainPageCategoryBestProductResponseDto> getCategoryBestProducts(HttpServletRequest request, Long categoryId) {
        log.info("메인 페이지 카테고리 베스트 제품 리스트 조회 service");

        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }
            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.getCategoryBestProducts(loginMember.getType(), categoryId);
        }else{
            return productQueryData.getCategoryBestProducts("C", categoryId);
        }

    }


    // 메인 페이지 Weekly 베스트 제품 리스트 조회 service
    //@Async("threadPoolTaskExecutor")
    public List<MainPageWeeklyBestProductResponseDto> getWeeklyBestProducts(HttpServletRequest request) {
        log.info("메인 페이지 Weekly 베스트 제품 리스트 조회 service");

        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }

            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.getWeeklyBestProducts(loginMember.getType());
        } else {

            return productQueryData.getWeeklyBestProducts("C");
        }
    }


    // 브랜드관 제품 리스트 호출 service
    //@Async("threadPoolTaskExecutor")
    public TotalBrandProductPageResponseDto brandPageMainProducts(
            HttpServletRequest request, Long brandId, int sort, int page, int startRangePrice, int endRangePrice, List<Long> labelIdList, List<Long> relatedMiddleCategoryIdList) {
        log.info("고객 제품 검색 service");
        log.info("접근 플랫폼 : {}", request.getHeader("user-agent"));

        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }

            // 로그인 고객
            Member loginMember = jwtTokenProvider.getMemberFromAuthentication();

            return productQueryData.brandPageMainProducts(loginMember.getType(), brandId, sort, page, startRangePrice, endRangePrice, labelIdList, relatedMiddleCategoryIdList);

        }else{
            return productQueryData.brandPageMainProducts("C", brandId, sort, page, startRangePrice, endRangePrice, labelIdList, relatedMiddleCategoryIdList);
        }
    }

}
