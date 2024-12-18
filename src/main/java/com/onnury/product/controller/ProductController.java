package com.onnury.product.controller;

import com.onnury.product.request.ProductCreateRequestDto;
import com.onnury.product.request.ProductSearchRequestDto;
import com.onnury.product.request.ProductUpdateRequestDto;
import com.onnury.product.response.*;
import com.onnury.product.service.ProductService;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/product")
@RestController
public class ProductController {

    private final ProductService productService;

    // 제품 생성 api
    @Operation(summary = "제품 생성 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createProduct(
            HttpServletRequest request,
            @Parameter(description = "제품 이미지 파일들") @RequestPart List<MultipartFile> productImgs,
            @Parameter(description = "제품 정보") @Valid @RequestPart ProductCreateRequestDto productCreateRequestDto) throws IOException {
        log.info("제품 생성 등록 api");

        ProductCreateResponseDto createResponse = productService.createProduct(request, productImgs, productCreateRequestDto);

        if (createResponse == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_PRODUCT, "제품이 생성되지 않았습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, createResponse), HttpStatus.OK);
        }
    }


    // 관리자 특정 제품 정보 호출 api
    @Operation(summary = "관리자 특정 제품 정보 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/get", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getProduct(
            HttpServletRequest request,
            @Parameter(description = "조회할 제품 id") @RequestParam Long productId){
        log.info("관리자 특정 제품 정보 호출 api");

        ProductCreateResponseDto createResponse = productService.getProduct(request, productId);

        if (createResponse == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, "제품이 존재하지 않습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, createResponse), HttpStatus.OK);
        }
    }


    // 제품 수정 api
    @Operation(summary = "제품 수정 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateProduct(
            HttpServletRequest request,
            @Parameter(description = "수정할 제품 이미지 파일 리스트") @RequestPart(required = false) List<MultipartFile> updateProductImgs,
            @Parameter(description = "수정할 제품 정보") @Valid @RequestPart ProductUpdateRequestDto productUpdateRequestDto) throws IOException {
        log.info("제품 수정 api");

        ProductUpdateResponseDto updateResponse = productService.updateProduct(request, updateProductImgs, productUpdateRequestDto);

        if (updateResponse == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_PRODUCT, "제품이 수정되지 않았습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, updateResponse), HttpStatus.OK);
        }
    }


    // 제품 삭제 api
    @Operation(summary = "제품 삭제 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteProduct(
            HttpServletRequest request,
            @Parameter(description = "삭제할 제품 id") @RequestParam Long productId) {
        log.info("제품 삭제 api");

        if (!productService.deleteProduct(request, productId)) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_PRODUCT, "제품을 삭제할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "제품이 정상적으로 삭제되었습니다."), HttpStatus.OK);
        }

    }


    // 관리자(공급사) 용 제품 리스트 (검색) api
    @Operation(summary = "관리자 용 제품 리스트 (검색) api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminTotalProductSearchResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getProductsList(
            HttpServletRequest request,
            @Parameter(description = "대분류 카테고리 id") @RequestParam(required = false, defaultValue = "0") Long upCategoryId,
            @Parameter(description = "중분류 카테고리 id") @RequestParam(required = false, defaultValue = "0") Long middleCategoryId,
            @Parameter(description = "소분류 카테고리 id") @RequestParam(required = false, defaultValue = "0") Long downCategoryId,
            @Parameter(description = "브랜드 id") @RequestParam(required = false, defaultValue = "0") Long brandId,
            @Parameter(description = "공급사 id") @RequestParam(required = false, defaultValue = "0") Long supplierId,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("제품 리스트 (검색) api");

        ProductSearchRequestDto productSearchRequestDto = ProductSearchRequestDto.builder()
                .page(page)
                .upCategoryId(upCategoryId)
                .middleCategoryId(middleCategoryId)
                .downCategoryId(downCategoryId)
                .brandId(brandId)
                .supplierId(supplierId)
                .searchKeyword(searchKeyword)
                .build();

        AdminTotalProductSearchResponseDto productListResponseDto = productService.getProductsList(request, productSearchRequestDto);

        if (productListResponseDto == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, "제품 리스트를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, productListResponseDto), HttpStatus.OK);
        }
    }


    // 제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출 api
    @Operation(summary = "제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductReadyInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/ready", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getReadyForCreateProductInfo(
            HttpServletRequest request) {
        log.info("제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출 api");

        ProductReadyInfoResponseDto readyForCreateProductInfo = productService.getReadyForCreateProductInfo(request);

        if (readyForCreateProductInfo == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_READY_INFO, "제품 등록을 위한 사전 데이터들이 존재하지 않습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, readyForCreateProductInfo), HttpStatus.OK);
        }
    }


    /**
     * REDIS 반영 여지 있음
     **/
    // 메인 페이지 신 상품 리스트 호출 api
    @Operation(summary = "메인 페이지 신 상품 리스트 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MainPageNewReleaseProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/newrelease", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getNewReleaseProducts(HttpServletRequest request) {
        log.info("메인 페이지 신 상품 리스트 호출 api");

        List<MainPageNewReleaseProductResponseDto> newReleaseProducts = productService.getNewReleaseProducts(request);

        if (newReleaseProducts.isEmpty()) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, newReleaseProducts), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, newReleaseProducts), HttpStatus.OK);
        }
    }


    /**
     * REDIS 반영 여지 있음
     **/
    // 대분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 api
    // (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순)
    // (추가 정렬 기준 : 금액 구간 정렬 , 브랜드 기준 정렬 , 라벨 기준 정렬 , 중분류 기준 정렬)
    @Operation(summary = "대분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalProductPageMainProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/page/up/{upCategoryId}/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> upCategoryPageMainProducts(
            HttpServletRequest request,
            @Parameter(description = "대분류 카테고리 id") @PathVariable Long upCategoryId,
            @Parameter(description = "정렬 기준") @RequestParam(required = false, defaultValue = "1") int sort,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "리스트 호출 범위 시작 가격") @RequestParam(required = false, defaultValue = "0") int startRangePrice,
            @Parameter(description = "리스트 호출 범위 끝 가격") @RequestParam(required = false, defaultValue = "0") int endRangePrice,
            @Parameter(description = "브랜드 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> brandId,
            @Parameter(description = "라벨 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> labelId,
            @Parameter(description = "중분류 카테고리 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> middleCategoryId) {
        log.info("대분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 api");

        TotalProductPageMainProductResponseDto upCategoryProductPageMainProducts =
                productService.upCategoryPageMainProducts(request, upCategoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, middleCategoryId);

        if (upCategoryProductPageMainProducts.getMainProductList().isEmpty()) {
            log.info("아무것도 안 뽑힌 제품들");
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, upCategoryProductPageMainProducts), HttpStatus.OK);
        } else {

            log.info("정상적으로 뽑힌 제품들 갯수 : {}", upCategoryProductPageMainProducts.getMainProductList().size());
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, upCategoryProductPageMainProducts), HttpStatus.OK);
        }
    }


    /**
     * REDIS 반영 여지 있음
     **/
    // 중분류, 소분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 api
    // (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순)
    // (추가 정렬 기준 : 금액 구간 정렬 , 브랜드 기준 정렬 , 라벨 기준 정렬 , 중분류 기준 정렬)
    @Operation(summary = "중분류, 소분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalProductPageMainProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/page/md/{categoryId}/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> middleAndDownCategoryPageMainProducts(
            HttpServletRequest request,
            @Parameter(description = "중분류 카테고리 id") @PathVariable Long categoryId,
            @Parameter(description = "정렬 기준") @RequestParam(required = false, defaultValue = "1") int sort,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "리스트 호출 범위 시작 가격") @RequestParam(required = false, defaultValue = "0") int startRangePrice,
            @Parameter(description = "리스트 호출 범위 끝 가격") @RequestParam(required = false, defaultValue = "0") int endRangePrice,
            @Parameter(description = "브랜드 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> brandId,
            @Parameter(description = "라벨 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> labelId,
            @Parameter(description = "소분류 카테고리 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> relatedDownCategoryId) {
        log.info("중분류, 소분류 기준 제품 페이지의 정렬 기준 제품 리스트 호출 api");

        TotalProductPageMainProductResponseDto middleAndDownCategoryProductPageMainProducts = productService.middleAndDownCategoryPageMainProducts(
                request, categoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, relatedDownCategoryId);

        if (middleAndDownCategoryProductPageMainProducts.getMainProductList().isEmpty()) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, middleAndDownCategoryProductPageMainProducts), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, middleAndDownCategoryProductPageMainProducts), HttpStatus.OK);
        }
    }


    /**
     * REDIS 반영 여지 있음
     **/
    // 고객 제품 검색 api
    // (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순)
    // (추가 정렬 기준 : 금액 구간 정렬 , 브랜드 기준 정렬 , 라벨 기준 정렬 , 중분류 기준 정렬)
    @Operation(summary = "고객 제품 검색 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalProductSearchResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> searchProducts(
            HttpServletRequest request,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "정렬 기준") @RequestParam(required = false, defaultValue = "1") int sort,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "리스트 호출 범위 시작 가격") @RequestParam(required = false, defaultValue = "0") int startRangePrice,
            @Parameter(description = "리스트 호출 범위 끝 가격") @RequestParam(required = false, defaultValue = "0") int endRangePrice,
            @Parameter(description = "브랜드 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> brandId,
            @Parameter(description = "라벨 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> labelId,
            @Parameter(description = "중분류 카테고리 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> middleCategoryId) {
        log.info("고객 제품 검색 api - keyword : {}", searchKeyword);

        TotalProductSearchResponseDto searchResult = productService.searchProducts(
                request, sort, searchKeyword, page, startRangePrice, endRangePrice, brandId, labelId, middleCategoryId);

        if (searchResult.getSearchProductList().isEmpty()) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, searchResult), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, searchResult), HttpStatus.OK);
        }
    }


    /**
     * REDIS 반영 여지 있음
     **/
    // 라벨 기준 제품 페이지 제품 리스트 호출 api
    // (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순)
    // (추가 정렬 기준 : 금액 구간 정렬 , 브랜드 기준 정렬 , 라벨 기준 정렬 , 중분류 기준 정렬)
    @Operation(summary = "라벨 기준 제품 페이지 제품 리스트 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalLabelProductPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/page/label/{labelId}/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> labelPageMainProducts(
            HttpServletRequest request,
            @Parameter(description = "라벨 id") @PathVariable Long labelId,
            @Parameter(description = "정렬 기준") @RequestParam(required = false, defaultValue = "1") int sort,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "검색 범위 시작 가격") @RequestParam(required = false, defaultValue = "0") int startRangePrice,
            @Parameter(description = "검색 범위 끝 가격") @RequestParam(required = false, defaultValue = "0") int endRangePrice,
            @Parameter(description = "선택 브랜드 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> brandId,
            @Parameter(description = "선택 중분류 카테고리 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> relatedMiddleCategoryId) {
        log.info("라벨 기준 제품 페이지 제품 리스트 호출 api");

        TotalLabelProductPageResponseDto labelProducts = productService.labelPageMainProducts(request, labelId, sort, page, startRangePrice, endRangePrice, brandId, relatedMiddleCategoryId);

        if (labelProducts == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_LABEL, "해당 라벨은 현재 존재하지 않습니다."), HttpStatus.OK);
        } else if (labelProducts.getLabelProductList().isEmpty()) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, labelProducts), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, labelProducts), HttpStatus.OK);
        }
    }


    /** REDIS 반영 여지 있음 **/
    // 제품 상세 페이지 조회 api
    @Operation(summary = "제품 상세 페이지 조회 api", tags = { "ProductController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductDetailPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/page/detail/{productId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> productDetailPage(
            @Parameter(description = "제품 id") @PathVariable Long productId){
        log.info("제품 상세 페이지 조회 api");

        ProductDetailPageResponseDto productDetailInfo = productService.productDetailPage(productId);

        if(productDetailInfo == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, "제품이 존재하지 않습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, productDetailInfo), HttpStatus.OK);
        }
    }


    // 제품 상세 정보 이미지 링크 반환 api
    @Operation(summary = "제품 상세 정보 이미지 링크 반환 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductDetailImageInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/detailImage", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> saveDetailImage(
            HttpServletRequest request,
            @Parameter(description = "상세 정보 이미지 파일들") @RequestPart List<MultipartFile> detailImages) throws IOException {
        log.info("재품 상세 정보 이미지 링크 반환 api");

        List<ProductDetailImageInfoResponseDto> detailInfoImages = productService.saveDetailImage(request, detailImages);

        if(detailInfoImages == null || detailInfoImages.isEmpty()){
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_SAVE_DETAIL_INFO_IMAGES, "제품 상세 정보 이미지를 생성하지 못하였습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, detailInfoImages), HttpStatus.OK);
        }

    }


    // 메인 페이지 카테고리 베스트 제품 리스트 조회 api
    @Operation(summary = "메인 페이지 카테고리 베스트 제품 리스트 조회 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MainPageCategoryBestProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/category/best", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getCategoryBestProducts(
            HttpServletRequest request,
            @Parameter(description = "카테고리 id") @RequestParam(required = false, defaultValue = "1") Long categoryId){
        log.info("메인 페이지 카테고리 베스트 제품 리스트 조회 api");

        List<MainPageCategoryBestProductResponseDto> categoryBestProductResponseDto = productService.getCategoryBestProducts(request, categoryId);

        if(categoryBestProductResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_CATEGORY_BEST_PRODUCTS, "카테고리 베스트 제품들을 조회할 수 없습니다."), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryBestProductResponseDto), HttpStatus.OK);
        }
    }


    // 메인 페이지 Weekly 베스트 제품 리스트 조회 api
    @Operation(summary = "메인 페이지 Weekly 베스트 제품 리스트 조회 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MainPageWeeklyBestProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/weekly/best", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getWeeklyBestProducts(HttpServletRequest request){
        log.info("메인 페이지 Weekly 베스트 제품 리스트 조회 api");

        List<MainPageWeeklyBestProductResponseDto> weeklyBestProductResponseDto = productService.getWeeklyBestProducts(request);

        if(weeklyBestProductResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_WEEKLY_BEST_PRODUCTS, "WEEKLY 베스트 제품들을 조회할 수 없습니다."), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, weeklyBestProductResponseDto), HttpStatus.OK);
        }
    }


    /**
     * REDIS 반영 여지 있음
     **/
    // 브랜드관 제품 리스트 호출 api
    // (정렬 기준 : 1 - 최신(기본) , 2 - 낮은 가격 순 , 3 - 높은 가격 순 , 4 - 누적 판매 순)
    // (추가 정렬 기준 : 금액 구간 정렬 , 라벨 기준 정렬 , 중분류 기준 정렬)
    @Operation(summary = "브랜드관 제품 리스트 호출 api", tags = { "ProductController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalBrandProductPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/page/brand/{brandId}/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> brandPageMainProducts(
            HttpServletRequest request,
            @Parameter(description = "브랜드 id") @PathVariable Long brandId,
            @Parameter(description = "정렬 기준") @RequestParam(required = false, defaultValue = "1") int sort,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "검색 범위 시작 가격") @RequestParam(required = false, defaultValue = "0") int startRangePrice,
            @Parameter(description = "검색 범위 끝 가격") @RequestParam(required = false, defaultValue = "0") int endRangePrice,
            @Parameter(description = "라벨 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> labelIdList,
            @Parameter(description = "선택 중분류 카테고리 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> relatedMiddleCategoryIdList) {
        log.info("브랜드관 제품 리스트 호출 api");

        TotalBrandProductPageResponseDto brandProducts = productService.brandPageMainProducts(request, brandId, sort, page, startRangePrice, endRangePrice, labelIdList, relatedMiddleCategoryIdList);

        if (brandProducts == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_LABEL, "해당 브랜드는 현재 존재하지 않습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, brandProducts), HttpStatus.OK);
        }
    }

}
