package com.onnury.payment.controller;

import com.onnury.payment.request.TransportInfoRequestDto;
import com.onnury.payment.response.AdminPaymentDetailResponseDto;
import com.onnury.payment.response.AdminPaymentList3ResponseDto;
import com.onnury.payment.response.AdminPaymentListResponseDto;
import com.onnury.payment.response.AdminSupllierPaymentListResponseDto;
import com.onnury.payment.service.AdminPaymentService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/payment")
@RestController
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    // 결제 주문 리스트업 api
    @Operation(summary = "결제 주문 리스트업 api", tags = { "AdminPaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminPaymentListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/onnury/order/listUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "검색 타입") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "조회 범위 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("결제 주문 리스트업 api");

        AdminPaymentListResponseDto easypayresult = adminPaymentService.paymentListUp(request, page, searchType,searchKeyword,startDate,endDate);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
    }


    @Operation(summary = "회원 결제 주문 리스트업 api", tags = { "AdminPaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminPaymentListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/onnury/memberorder/listUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentMwmberListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "검색 타입") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "조회 범위 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate,
            @Parameter(description = "회원 ID") @RequestParam(required = false, defaultValue = "") String memberId) {
        log.info("결제 주문 리스트업 api");

        AdminPaymentListResponseDto easypayresult = adminPaymentService.paymentMemberListUp(request, page, searchType,searchKeyword,startDate,endDate, memberId);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
    }


    // 결제 상세 정보 조회 api
    @Operation(summary = "결제 상세 정보 조회 api", tags = { "AdminPaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminPaymentDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/onnury/order/detail", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentDetail(
            HttpServletRequest request,
            @Parameter(description = "주문 번호") @RequestParam(required = false, defaultValue = "") String orderNumber) {
        log.info("결제 상세 정보 조회 api");

        AdminPaymentDetailResponseDto easypayresult = adminPaymentService.paymentDetail(request,orderNumber);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
    }


    // 결제 주문 취소 리스트업 api
    @Operation(summary = "결제 주문 취소 리스트업 api", tags = { "AdminPaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminPaymentList3ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/onnury/cancelorder/listUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentCancelListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("결제 주문 취소 리스트업 api");

        AdminPaymentList3ResponseDto cancelOrderResult = adminPaymentService.paymentCancelListUp(request, page);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, cancelOrderResult), HttpStatus.OK);
    }


    // 관리자(공급사) 기준 결제 정산 관리 주문 리스트업 api
    @Operation(summary = "공급사 기준 결제 주문 리스트업 api", tags = { "AdminPaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminSupllierPaymentListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/onnury/order/supplierlistUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> supplierPaymentListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "공급사 id") @RequestParam(required = false, defaultValue = "") String supplierId,
            @Parameter(description = "검색 타입") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "조회 범위 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate){
        log.info("공급사 기준 결제 주문 리스트업 api");

        AdminSupllierPaymentListResponseDto easypayresult = adminPaymentService.supplierPaymentListUp(request, page, supplierId, searchType, searchKeyword,startDate,endDate);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
    }


    // 운송장 번호 업데이트 api
    @Operation(summary = "운송장 번호 업데이트 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/transport/confirm", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> confirmTransportNumber(
            HttpServletRequest request,
            @Parameter(description = "운송장 번호 관련 정보") @RequestPart List<TransportInfoRequestDto> transportInfo){
        log.info("운송장 번호 업데이트 api");

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, adminPaymentService.confirmTransportNumber(request, transportInfo)), HttpStatus.OK);
    }

}
