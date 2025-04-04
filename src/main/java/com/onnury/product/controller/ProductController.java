package com.onnury.product.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.common.util.LogUtil;
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
import java.util.HashMap;
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
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createProduct(
            HttpServletRequest request,
            @Parameter(description = "제품 이미지 파일들") @RequestPart List<MultipartFile> productImgs,
            @Parameter(description = "제품 정보") @Valid @RequestPart ProductCreateRequestDto productCreateRequestDto) throws IOException {
        log.info("제품 생성 등록 api");

        HashMap<String, String> requestParam = new HashMap<>();

        if(productImgs == null || productImgs.isEmpty()){
            LogUtil.logError("제품 생성용 이미지가 존재하지 않습니다.", request);
            return null;
        }

        productImgs.forEach(eachProductImage -> {
            requestParam.put(productImgs.indexOf(eachProductImage) + "번째 제품 생성 이미지", eachProductImage.getOriginalFilename() + " : " + eachProductImage.getContentType());
        });

        try{
            ProductCreateResponseDto createResponse = productService.createProduct(request, productImgs, productCreateRequestDto);

            if (createResponse == null) {
                LogUtil.logError(StatusCode.CANT_CREATE_PRODUCT.getMessage(), request, StatusCode.CANT_CREATE_PRODUCT, productCreateRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_PRODUCT, "제품이 생성되지 않았습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, createResponse), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam, productCreateRequestDto);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/get", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getProduct(
            HttpServletRequest request,
            @Parameter(description = "조회할 제품 id") @RequestParam Long productId){
        log.info("관리자 특정 제품 정보 호출 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("조회할 제품 id", Long.toString(productId));

        try{
            ProductCreateResponseDto createResponse = productService.getProduct(request, productId);

            if (createResponse == null) {
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, "제품이 존재하지 않습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, createResponse), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateProduct(
            HttpServletRequest request,
            @Parameter(description = "수정할 제품 이미지 파일 리스트") @RequestPart(required = false) List<MultipartFile> updateProductImgs,
            @Parameter(description = "수정할 제품 정보") @Valid @RequestPart ProductUpdateRequestDto productUpdateRequestDto) {
        log.info("제품 수정 api");

        HashMap<String, String> requestParam = new HashMap<>();

        if(!updateProductImgs.isEmpty()){
            updateProductImgs.forEach(eachUpdateImage -> {
                requestParam.put(updateProductImgs.indexOf(eachUpdateImage) + "번째 제품 수정 이미지", eachUpdateImage.getOriginalFilename() + " : " + eachUpdateImage.getContentType());
            });
        }

        try{
            ProductUpdateResponseDto updateResponse = productService.updateProduct(request, updateProductImgs, productUpdateRequestDto);

            if (updateResponse == null) {
                LogUtil.logError(StatusCode.CANT_UPDATE_PRODUCT.getMessage(), request, requestParam, productUpdateRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_PRODUCT, "제품이 수정되지 않았습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, updateResponse), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, requestParam, productUpdateRequestDto);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteProduct(
            HttpServletRequest request,
            @Parameter(description = "삭제할 제품 id") @RequestParam Long productId) {
        log.info("제품 삭제 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("삭제할 제품 id", Long.toString(productId));

        try{
            if (!productService.deleteProduct(request, productId)) {
                LogUtil.logError(StatusCode.CANT_DELETE_PRODUCT.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_PRODUCT, "제품을 삭제할 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "제품이 정상적으로 삭제되었습니다."), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
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

        try{
            AdminTotalProductSearchResponseDto productListResponseDto = productService.getProductsList(request, productSearchRequestDto);

            if (productListResponseDto == null) {
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request, productSearchRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, "제품 리스트를 조회할 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, productListResponseDto), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, productSearchRequestDto);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/ready", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getReadyForCreateProductInfo(HttpServletRequest request) {
        log.info("제품 생성 페이지 진입 시 사전 호출되어 활용될 정보 호출 api");

        try{
            ProductReadyInfoResponseDto readyForCreateProductInfo = productService.getReadyForCreateProductInfo(request);

            if (readyForCreateProductInfo == null) {
                LogUtil.logError(StatusCode.NOT_EXIST_READY_INFO.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_READY_INFO, "제품 등록을 위한 사전 데이터들이 존재하지 않습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, readyForCreateProductInfo), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/newrelease", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getNewReleaseProducts(HttpServletRequest request) {
        log.info("메인 페이지 신 상품 리스트 호출 api");

        try{
            List<MainPageNewReleaseProductResponseDto> newReleaseProducts = productService.getNewReleaseProducts(request);

            if (newReleaseProducts.isEmpty()) {
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, newReleaseProducts), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, newReleaseProducts), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
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

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("대분류 카테고리 id", Long.toString(upCategoryId));
        requestParam.put("정렬 기준", Integer.toString(sort));
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("리스트 호출 범위 시작 가격", Integer.toString(startRangePrice));
        requestParam.put("리스트 호출 범위 끝 가격", Integer.toString(endRangePrice));

        if(!brandId.isEmpty()){
            brandId.forEach(eachBrandId -> {
                requestParam.put(brandId.indexOf(eachBrandId) + "번째 선택 브랜드 id", Long.toString(eachBrandId));
            });
        }

        if(!labelId.isEmpty()){
            labelId.forEach(eachLabelId -> {
                requestParam.put(labelId.indexOf(eachLabelId) + "번째 선택 라벨 id", Long.toString(eachLabelId));
            });
        }

        if(!middleCategoryId.isEmpty()){
            middleCategoryId.forEach(eachCategoryId -> {
                requestParam.put(middleCategoryId.indexOf(eachCategoryId) + "번째 선택 카테고리 id", Long.toString(eachCategoryId));
            });
        }

        try{
            TotalProductPageMainProductResponseDto upCategoryProductPageMainProducts =
                    productService.upCategoryPageMainProducts(request, upCategoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, middleCategoryId);

            if(upCategoryProductPageMainProducts != null){
                if (upCategoryProductPageMainProducts.getMainProductList().isEmpty()) {
                    log.info("아무것도 안 뽑힌 제품들");
                    return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, upCategoryProductPageMainProducts), HttpStatus.OK);
                } else {
                    log.info("정상적으로 뽑힌 제품들 갯수 : {}", upCategoryProductPageMainProducts.getMainProductList().size());
                    return new ResponseEntity<>(new ResponseBody(StatusCode.OK, upCategoryProductPageMainProducts), HttpStatus.OK);
                }
            }else{
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, null), HttpStatus.OK);
            }

        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
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

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("중분류 카테고리 id", Long.toString(categoryId));
        requestParam.put("정렬 기준", Integer.toString(sort));
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("리스트 호출 범위 시작 가격", Integer.toString(startRangePrice));
        requestParam.put("리스트 호출 범위 끝 가격", Integer.toString(endRangePrice));

        if(!brandId.isEmpty()){
            brandId.forEach(eachBrandId -> {
                requestParam.put(brandId.indexOf(eachBrandId) + "번째 선택 브랜드 id", Long.toString(eachBrandId));
            });
        }

        if(!labelId.isEmpty()){
            labelId.forEach(eachLabelId -> {
                requestParam.put(labelId.indexOf(eachLabelId) + "번째 선택 라벨 id", Long.toString(eachLabelId));
            });
        }

        if(!relatedDownCategoryId.isEmpty()){
            relatedDownCategoryId.forEach(eachCategoryId -> {
                requestParam.put(relatedDownCategoryId.indexOf(eachCategoryId) + "번째 선택 카테고리 id", Long.toString(eachCategoryId));
            });
        }

        try{
            TotalProductPageMainProductResponseDto middleAndDownCategoryProductPageMainProducts = productService.middleAndDownCategoryPageMainProducts(
                    request, categoryId, sort, page, startRangePrice, endRangePrice, brandId, labelId, relatedDownCategoryId);

            if(middleAndDownCategoryProductPageMainProducts != null){
                if (middleAndDownCategoryProductPageMainProducts.getMainProductList().isEmpty()) {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, middleAndDownCategoryProductPageMainProducts), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.OK, middleAndDownCategoryProductPageMainProducts), HttpStatus.OK);
                }
            }else{
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, null), HttpStatus.OK);
            }

        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
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

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("검색 키워드", searchKeyword);
        requestParam.put("정렬 기준", Integer.toString(sort));
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("리스트 호출 범위 시작 가격", Integer.toString(startRangePrice));
        requestParam.put("리스트 호출 범위 끝 가격", Integer.toString(endRangePrice));

        if(!brandId.isEmpty()){
            brandId.forEach(eachBrandId -> {
                requestParam.put(brandId.indexOf(eachBrandId) + "번째 선택 브랜드 id", Long.toString(eachBrandId));
            });
        }

        if(!labelId.isEmpty()){
            labelId.forEach(eachLabelId -> {
                requestParam.put(labelId.indexOf(eachLabelId) + "번째 선택 라벨 id", Long.toString(eachLabelId));
            });
        }

        if(!middleCategoryId.isEmpty()){
            middleCategoryId.forEach(eachCategoryId -> {
                requestParam.put(middleCategoryId.indexOf(eachCategoryId) + "번째 선택 카테고리 id", Long.toString(eachCategoryId));
            });
        }

        try{
            TotalProductSearchResponseDto searchResult = productService.searchProducts(
                    request, sort, searchKeyword, page, startRangePrice, endRangePrice, brandId, labelId, middleCategoryId);

            if(searchResult != null){
                if (searchResult.getSearchProductList().isEmpty()) {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, searchResult), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.OK, searchResult), HttpStatus.OK);
                }
            }else{
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, null), HttpStatus.OK);
            }

        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
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

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("라벨 id", Long.toString(labelId));
        requestParam.put("정렬 기준", Integer.toString(sort));
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("검색 범위 시작 가격", Integer.toString(startRangePrice));
        requestParam.put("검색 범위 끝 가격", Integer.toString(endRangePrice));

        if(!brandId.isEmpty()){
            brandId.forEach(eachBrandId -> {
                requestParam.put(brandId.indexOf(eachBrandId) + "번째 선택 브랜드 id", Long.toString(eachBrandId));
            });
        }

        if(!relatedMiddleCategoryId.isEmpty()){
            relatedMiddleCategoryId.forEach(eachCategoryId -> {
                requestParam.put(relatedMiddleCategoryId.indexOf(eachCategoryId) + "번째 선택 카테고리 id", Long.toString(eachCategoryId));
            });
        }

        try{
            TotalLabelProductPageResponseDto labelProducts = productService.labelPageMainProducts(request, labelId, sort, page, startRangePrice, endRangePrice, brandId, relatedMiddleCategoryId);

            if (labelProducts != null) {
                if (labelProducts.getLabelProductList().isEmpty()) {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, labelProducts), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.OK, labelProducts), HttpStatus.OK);
                }
            }else {
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCTS, "해당 라벨 제품들은 현재 존재하지 않습니다."), HttpStatus.OK);
            }

        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/page/detail/{productId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> productDetailPage(
            @Parameter(description = "제품 id") @PathVariable Long productId){
        log.info("제품 상세 페이지 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("제품 id", Long.toString(productId));

        try{
            ProductDetailPageResponseDto productDetailInfo = productService.productDetailPage(productId);

            if(productDetailInfo == null){
                LogUtil.logError(StatusCode.NOT_EXIST_PRODUCT_DETAIL.getMessage(), requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_PRODUCT_DETAIL, "제품이 존재하지 않습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, productDetailInfo), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/detailImage", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> saveDetailImage(
            HttpServletRequest request,
            @Parameter(description = "상세 정보 이미지 파일들") @RequestPart List<MultipartFile> detailImages) throws IOException {
        log.info("재품 상세 정보 이미지 링크 반환 api");

        HashMap<String, String> requestParam = new HashMap<>();

        if(detailImages.isEmpty()){
            LogUtil.logError("제품 상세 정보 이미지들이 존재하지 않습니다.", request);
            return null;
        }

        detailImages.forEach(eachDetailImage -> {
            requestParam.put(detailImages.indexOf(eachDetailImage) + "번째 상세 정보 이미지", eachDetailImage.getOriginalFilename() + " : " + eachDetailImage.getContentType());
        });

        try{
            List<ProductDetailImageInfoResponseDto> detailInfoImages = productService.saveDetailImage(request, detailImages);

            if(detailInfoImages == null || detailInfoImages.isEmpty()){
                LogUtil.logError(StatusCode.NOT_SAVE_DETAIL_INFO_IMAGES.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_SAVE_DETAIL_INFO_IMAGES, "제품 상세 정보 이미지를 생성하지 못하였습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, detailInfoImages), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/category/best", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getCategoryBestProducts(
            HttpServletRequest request,
            @Parameter(description = "카테고리 id") @RequestParam(required = false, defaultValue = "1") Long categoryId){
        log.info("메인 페이지 카테고리 베스트 제품 리스트 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("카테고리 id", Long.toString(categoryId));

        try{
            List<MainPageCategoryBestProductResponseDto> categoryBestProductResponseDto = productService.getCategoryBestProducts(request, categoryId);

            if(categoryBestProductResponseDto == null){
                LogUtil.logError(StatusCode.CANT_GET_CATEGORY_BEST_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_CATEGORY_BEST_PRODUCTS, "카테고리 베스트 제품들을 조회할 수 없습니다."), HttpStatus.OK);
            }else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryBestProductResponseDto), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/weekly/best", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getWeeklyBestProducts(HttpServletRequest request){
        log.info("메인 페이지 Weekly 베스트 제품 리스트 조회 api");

        try{
            List<MainPageWeeklyBestProductResponseDto> weeklyBestProductResponseDto = productService.getWeeklyBestProducts(request);

            if(weeklyBestProductResponseDto == null){
                LogUtil.logError(StatusCode.CANT_GET_WEEKLY_BEST_PRODUCTS.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_WEEKLY_BEST_PRODUCTS, "WEEKLY 베스트 제품들을 조회할 수 없습니다."), HttpStatus.OK);
            }else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, weeklyBestProductResponseDto), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
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

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("브랜드 id", Long.toString(brandId));
        requestParam.put("정렬 기준", Integer.toString(sort));
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("검색 범위 시작 가격", Integer.toString(startRangePrice));
        requestParam.put("검색 범위 끝 가격", Integer.toString(endRangePrice));

        if(!labelIdList.isEmpty()){
            labelIdList.forEach(eachLabelId -> {
                requestParam.put(labelIdList.indexOf(eachLabelId) + "번째 선택 라벨 id", Long.toString(eachLabelId));
            });
        }

        if(!relatedMiddleCategoryIdList.isEmpty()){
            relatedMiddleCategoryIdList.forEach(eachCategoryId -> {
                requestParam.put(relatedMiddleCategoryIdList.indexOf(eachCategoryId) + "번째 선택 카테고리 id", Long.toString(eachCategoryId));
            });
        }

        try{
            TotalBrandProductPageResponseDto brandProducts = productService.brandPageMainProducts(request, brandId, sort, page, startRangePrice, endRangePrice, labelIdList, relatedMiddleCategoryIdList);

            if (brandProducts == null) {
                LogUtil.logError(StatusCode.NOT_EXIST_BRAND_PRODUCTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BRAND_PRODUCTS, "해당 브랜드 제품들은 현재 존재하지 않습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, brandProducts), HttpStatus.OK);
            }
        } catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }

}
