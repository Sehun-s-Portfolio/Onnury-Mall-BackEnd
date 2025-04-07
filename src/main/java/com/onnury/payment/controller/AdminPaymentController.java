package com.onnury.payment.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.common.util.LogUtil;
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
import org.springframework.security.core.parameters.P;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/onnury/order/listUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "검색 타입") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "조회 범위 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("결제 주문 리스트업 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("검색 타입", searchType);
        requestParam.put("검색 키워드", searchKeyword);
        requestParam.put("조회 범위 시작 일자", startDate);
        requestParam.put("조회 범위 끝 일자", endDate);

        try{
            AdminPaymentListResponseDto easypayresult = adminPaymentService.paymentListUp(request, page, searchType,searchKeyword,startDate,endDate);

            if(easypayresult == null){
                LogUtil.logError(StatusCode.CANT_GET_ORDER_LIST.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_ORDER_LIST, "결제 주문 리스트들을 조회할 수 없습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/onnury/memberorder/listUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentMemberListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "검색 타입") @RequestParam(required = false, defaultValue = "") String searchType,
            @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @Parameter(description = "조회 범위 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate,
            @Parameter(description = "회원 ID") @RequestParam(required = false, defaultValue = "") String memberId) {
        log.info("결제 주문 리스트업 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("검색 타입", searchType);
        requestParam.put("검색 키워드", searchKeyword);
        requestParam.put("조회 범위 시작 일자", startDate);
        requestParam.put("조회 범위 끝 일자", endDate);
        requestParam.put("회원 ID", memberId);

        try{
            AdminPaymentListResponseDto easypayresult = adminPaymentService.paymentMemberListUp(request, page, searchType,searchKeyword,startDate,endDate, memberId);

            if(easypayresult == null){
                LogUtil.logError(StatusCode.CANT_GET_MEMBER_ORDER_LIST.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_MEMBER_ORDER_LIST, "회원의 결제 주문 리스트를 조회할 수 없습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/onnury/order/detail", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentDetail(
            HttpServletRequest request,
            @Parameter(description = "주문 번호") @RequestParam(required = false, defaultValue = "") String orderNumber) {
        log.info("결제 상세 정보 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("주문 번호", orderNumber);

        try{
            AdminPaymentDetailResponseDto easypayresult = adminPaymentService.paymentDetail(request,orderNumber);

            if(easypayresult == null) {
                LogUtil.logError(StatusCode.CANT_GET_ORDER_DETAIL.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_ORDER_DETAIL, "결제 상세 정보를 조회할 수 없습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/onnury/cancelorder/listUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> paymentCancelListUp(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("결제 주문 취소 리스트업 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));

        try{
            AdminPaymentList3ResponseDto cancelOrderResult = adminPaymentService.paymentCancelListUp(request, page);

            if(cancelOrderResult == null){
                LogUtil.logError(StatusCode.CANT_GET_CANCEL_ORDER_LIST.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_CANCEL_ORDER_LIST, "결제 주문 취소 이력 리스트를 조회할 수 없습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, cancelOrderResult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
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
    @MethodCallMonitor
    @TimeMonitor
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

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("공급사 id", supplierId);
        requestParam.put("검색 타입", searchType);
        requestParam.put("검색 키워드", searchKeyword);
        requestParam.put("조회 범위 시작 일자", startDate);
        requestParam.put("조회 범위 끝 일자", endDate);

        try{
            AdminSupllierPaymentListResponseDto easypayresult = adminPaymentService.supplierPaymentListUp(request, page, supplierId, searchType, searchKeyword,startDate,endDate);

            if(easypayresult == null){
                LogUtil.logError(StatusCode.CANT_GET_SUPPLIER_ORDER_LIST.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_SUPPLIER_ORDER_LIST, "공급사에 해당되는 결제 주문 이력 리스트를 조회할 수 없습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
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
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/transport/confirm", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> confirmTransportNumber(
            HttpServletRequest request,
            @Parameter(description = "운송장 번호 관련 정보") @RequestPart List<TransportInfoRequestDto> transportInfo){
        log.info("운송장 번호 업데이트 api");

        try{
            String confirmText = adminPaymentService.confirmTransportNumber(request, transportInfo);

            if(confirmText == null){
                LogUtil.logError(StatusCode.CANT_CONFIRM_TRANSPORT_NUMBER.getMessage(), request, transportInfo);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CONFIRM_TRANSPORT_NUMBER, "운송장 번호 업데이트에 실패하였습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, confirmText), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, transportInfo);
            return null;
        }
    }

}
