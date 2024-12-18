package com.onnury.excel.controller;

import com.onnury.banner.domain.Banner;
import com.onnury.category.response.CategoryDataExcelResponseDto;
import com.onnury.excel.response.BannerExcelResponseDto;
import com.onnury.excel.response.FaqExcelResponseDto;
import com.onnury.excel.response.InquiryExcelResponseDto;
import com.onnury.excel.response.LabelExcelResponseDto;
import com.onnury.excel.service.ExcelService;
import com.onnury.inquiry.domain.Faq;
import com.onnury.inquiry.response.InquiryDataResponseDto;
import com.onnury.label.domain.Label;
import com.onnury.member.domain.Member;
import com.onnury.payment.response.AdminSupplierPaymentResponseExcelQDto;
import com.onnury.product.request.ProductSearchRequestDto;
import com.onnury.product.response.ProductExcelResponseDto;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import com.onnury.supplier.domain.Supplier;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/excel")
@RestController
public class ExcelController {

    private final ExcelService excelService;

    // 배너 리스트 excel api
    @Operation(summary = "배너 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BannerExcelResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/banner/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelBannerList(HttpServletRequest request) {
        log.info("배너 리스트 excel api");

        List<BannerExcelResponseDto> ResultList = excelService.excelBannerList(request);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "배너 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 공급사 리스트 excel api
    @Operation(summary = "공급사 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Supplier.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/supplier/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelSupplierList(HttpServletRequest request) {
        log.info("배너 리스트 excel api");

        List<Supplier> ResultList = excelService.excelSupplierList(request);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "공급사 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 라벨 리스트 excel api
    @Operation(summary = "라벨 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LabelExcelResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/label/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelLabelList(HttpServletRequest request) {
        log.info("라벨 리스트 excel api");

        List<LabelExcelResponseDto> ResultList = excelService.excelLabelList(request);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "라벨 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 카테고리 리스트 excel api
    @Operation(summary = "카테고리 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryDataExcelResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/category/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelCategoryList(HttpServletRequest request) {
        log.info("카테고리 리스트 excel api");

        List<CategoryDataExcelResponseDto> ResultList = excelService.excelCategoryList(request);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "카테고리 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 제품 리스트 excel api
    @Operation(summary = "제품 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductExcelResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/product/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelProductList(
            HttpServletRequest request,
            @Parameter(description = "대분류 카테고리 id") @RequestParam(required = false, defaultValue = "0") Long upCategoryId,
            @Parameter(description = "중분류 카테고리 id") @RequestParam(required = false, defaultValue = "0") Long middleCategoryId,
            @Parameter(description = "소분류 카테고리 id") @RequestParam(required = false, defaultValue = "0") Long downCategoryId,
            @Parameter(description = "브랜드 id") @RequestParam(required = false, defaultValue = "0") Long brandId,
            @Parameter(description = "공급사 id") @RequestParam(required = false, defaultValue = "0") Long supplierId,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword
    ) {
        log.info("상품 리스트 excel api");

        ProductSearchRequestDto productSearchRequestDto = ProductSearchRequestDto.builder()
                .upCategoryId(upCategoryId)
                .middleCategoryId(middleCategoryId)
                .downCategoryId(downCategoryId)
                .brandId(brandId)
                .supplierId(supplierId)
                .searchKeyword(searchKeyword)
                .build();

        List<ProductExcelResponseDto> ResultList = excelService.excelProductList(request, productSearchRequestDto);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "카테고리 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 회원 리스트 excel api
    @Operation(summary = "회원 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Member.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/member/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelMemberList(
            HttpServletRequest request,
            @Parameter(description = "회원 검색 키워드") @RequestParam(required = false) String searchtype,
            @Parameter(description = "검색 타입") @RequestParam(required = false) String search) {
        log.info("회원 리스트 excel api");

        List<Member> ResultList = excelService.excelMemberList(request, searchtype, search);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "회원 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 문의 리스트 excel api
    @Operation(summary = "문의 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = InquiryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/inquiry/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelInquiryList(
            HttpServletRequest request,
            @Parameter(description = "문의 타입 1") @RequestParam(required = false) String searchType,
            @Parameter(description = "문의 타입 2") @RequestParam(required = false) String searchType2,
            @Parameter(description = "문의 검색 키워드") @RequestParam(required = false) String searchKeyword) {
        log.info("문의 리스트 excel api");

        List<InquiryExcelResponseDto> ResultList = excelService.excelInquiryList(request, searchType, searchType2, searchKeyword);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "문의 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // FAQ 리스트 excel api
    @Operation(summary = "FAQ 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Faq.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/faq/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelFaqList(
            HttpServletRequest request,
            @Parameter(description = "FAQ 타입") @RequestParam(required = false, defaultValue = "전체") String type) {
        log.info("FAQ 리스트 excel api");

        List<FaqExcelResponseDto> ResultList = excelService.excelFaqList(request, type);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "FAQ 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 결제 리스트 excel api
    @Operation(summary = "결제 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminSupplierPaymentResponseExcelQDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/payment/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelPaymentList(
            HttpServletRequest request,
            @Parameter(description = "공급사 id") @RequestParam(required = false, defaultValue = "0") Long supplierId,
            @Parameter(description = "조회 시작 기간") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 시작 기간") @RequestParam(required = false, defaultValue = "") String endDate,
            @Parameter(description = "검색 유형") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword) {
        log.info("결제 리스트 excel api");

        List<AdminSupplierPaymentResponseExcelQDto> ResultList = excelService.excelPaymentList(request, supplierId, startDate, endDate, searchType, searchKeyword);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "결제 리스트 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }


    // 정산 리스트 excel api
    @Operation(summary = "정산 리스트 excel api", tags = {"ExcelController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminSupplierPaymentResponseExcelQDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/totalorder/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> excelTotalOrderList(
            HttpServletRequest request,
            @Parameter(description = "공급사 id") @RequestParam(required = false, defaultValue = "0") Long supplierId,
            @Parameter(description = "조회 시작 기간") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 시작 기간") @RequestParam(required = false, defaultValue = "") String endDate,
            @Parameter(description = "검색 유형") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword) {
        log.info("정산 리스트 excel api");

        List<AdminSupplierPaymentResponseExcelQDto> ResultList = excelService.excelTotalOrderList(request, supplierId, startDate, endDate, searchType, searchKeyword);

        if (ResultList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "정산 리스트 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, ResultList), HttpStatus.OK);
        }
    }
}