package com.onnury.payment.controller;

import com.onnury.payment.request.*;
import com.onnury.payment.response.OnnuryPaymentApprovalInfo;
import com.onnury.payment.service.BizService;
import com.onnury.payment.service.CompoundPayService;
import com.onnury.payment.service.EasyPayService;
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
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@RestController
public class PaymentController {

    private final EasyPayService easyPayService;
    private final BizService bizService;
    private final CompoundPayService compoundPayService;

    // 온누리 결제 준비 api (url 에 보낼 데이터 셋팅)
    @Operation(summary = "온누리 결제 준비 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = JSONObject.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/onnury/reserve/ready", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> onnuryPay(
            HttpServletRequest request,
            @Parameter(description = "온누리 결제 준비 내용") @RequestPart PaymentOnnuryPayRequestDto paymentOnnuryPayRequestDto) throws Exception {

        JSONObject easypayresult = bizService.reserve(paymentOnnuryPayRequestDto);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easypayresult), HttpStatus.OK);
    }


    // 온누리 비즈 페이먼트 결제 정보 (성공 / 실패 / 취소) 조회 api
    @Operation(summary = "온누리 비즈 페이먼트 결제 정보 (성공 / 실패 / 취소) 조회 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = OnnuryPaymentApprovalInfo.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/onnury/approval/info/get", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getOnnuryPaymentApprovalInfo(
            HttpServletRequest request,
            @Parameter(description = "주문 번호") @RequestParam String merchantOrderID) throws InterruptedException {
        log.info("온누리 비즈 페이먼트 결제 정보 조회 (성공 / 실패 / 취소) api");

        OnnuryPaymentApprovalInfo onnuryPaymentApprovalInfo = bizService.getOnnuryPaymentApprovalInfo(request, merchantOrderID);

        if(onnuryPaymentApprovalInfo == null){
            bizService.getOnnuryPaymentApprovalInfo(request, merchantOrderID);
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_APPROVAL_PAYMENT_INFO_GET, "온누리 비즈 페이먼트 결제 승인 정보가 존재하지 않습니다."), HttpStatus.OK);
        }else{
            log.info("결제 정보 조회 성공 !!");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, onnuryPaymentApprovalInfo), HttpStatus.OK);
        }
    }


    // 온누리 거래 승인 api
    @Operation(summary = "온누리 거래 승인 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = JSONObject.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/onnury/approval", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> onnuryApproval(
            HttpServletRequest request,
            @Parameter(description = "온누리 거래 승인 정보") @RequestPart NewPaymentRequestDto newPaymentRequestDto,
            @Parameter(description = "주문 제품 정보") @RequestPart List<PaymentProductListRequestDto> paymentProductListRequestDto) throws Exception {
        log.info("온누리 거래 승인 api");

        // 온누리로만 결제 할 때
        JSONObject resultObject = bizService.approval(request, newPaymentRequestDto, paymentProductListRequestDto);
        String code = (String) resultObject.get("resCd");

        if (code.equals("0000")) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, resultObject), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_APPROVAL_INFO, resultObject), HttpStatus.OK);
        }
    }


    // 온누리 결제 취소 api
    @Operation(summary = "온누리 결제 취소 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/onnury/cancel", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> onnuryCancel(
            HttpServletRequest request,
            @Parameter(description = "온누리 결제 취소 정보") @RequestParam Map<String, Object> cancelInfoMap) throws Exception {
        log.info("온누리 결제 취소 api");

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, bizService.cancelOnnuryPayment(request, cancelInfoMap)), HttpStatus.OK);
    }


    // EasyPay 결제 준비 api
    @Operation(summary = "EasyPay 결제 준비 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = JSONObject.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/easyPay/reserve/ready", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> easyPayPayreserve(
            HttpServletRequest request,
            @Parameter(description = "EasyPay 결제 준비 내용") @RequestPart PaymentKiccRequestDto paymentKiccRequestDto) throws Exception {
        log.info("EasyPay 결제 준비 api");

        JSONObject resultObject = easyPayService.reserve(request, paymentKiccRequestDto);
        String code = (String) resultObject.get("resCd");

        if (code.equals("0000")) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, resultObject), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_APPROVAL_INFO, resultObject), HttpStatus.OK);
        }
    }


    // EasyPay 결제 승인 성공 정보 조회 (성공 / 실패 / 취소) api
    @Operation(summary = "EasyPay 결제 승인 성공 정보 조회 (성공 / 실패 / 취소) api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = EasyPaymentApprovalInfo.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/easyPay/approval/info/get", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getEasyPaymentApprovalInfo(
            HttpServletRequest request,
            @Parameter(description = "조회 주문 번호") @RequestParam String shopOrderNo) throws InterruptedException {
        log.info("EasyPay 결제 승인 정보 조회 (성공 / 실패 / 취소) api");

        EasyPaymentApprovalInfo easyPaymentApprovalInfo = easyPayService.getEasyPaymentApprovalInfo(request, shopOrderNo);

        if(easyPaymentApprovalInfo == null){
            easyPayService.getEasyPaymentApprovalInfo(request, shopOrderNo);
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_APPROVAL_EASY_PAYMENT_INFO_GET, "EASY PAY 결제 승인 정보가 존재하지 않습니다."), HttpStatus.OK);
        }else{
            log.info("결제 정보 최종 성공 !!");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easyPaymentApprovalInfo), HttpStatus.OK);
        }
    }


    // EasyPay 거래 승인 api
    @Operation(summary = "EasyPay 거래 승인 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = JSONObject.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/easyPay/approval", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> easyPayApproval(
            HttpServletRequest request,
            @Parameter(description = "온누리 거래 승인 정보") @RequestPart NewPaymentRequestDto newPaymentRequestDto,
            @Parameter(description = "주문 제품 정보") @RequestPart List<PaymentProductListRequestDto> paymentProductListRequestDto) throws Exception {
        log.info("EasyPay 거래 승인 api");

        JSONObject resultObject = easyPayService.approval(request, newPaymentRequestDto, paymentProductListRequestDto);
        String code = (String) resultObject.get("resCd");

        if (code.equals("0000")) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, resultObject), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_APPROVAL_INFO, resultObject), HttpStatus.OK);
        }
    }


    // EasyPay 결제 취소 api
    @Operation(summary = "EasyPay 결제 취소 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/easyPay/cancel", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> easyPayCancel(
            HttpServletRequest request,
            @Parameter(description = "EasyPay 결제 취소 정보") @RequestParam Map<String, Object> cancelInfoMap) throws Exception {
        log.info("EasyPay 결제 취소 api");

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, easyPayService.cancel(request, cancelInfoMap)), HttpStatus.OK);
    }


    // 복합 결제 거래 승인 api
    @Operation(summary = "복합 결제 거래 승인 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/compound/approval", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> compoundPayApproval(
            HttpServletRequest request,
            @Parameter(description = "온누리 거래 승인 정보") @RequestPart NewPaymentRequestDto newPaymentRequestDto,
            @Parameter(description = "주문 제품 정보") @RequestPart List<PaymentProductListRequestDto> paymentProductListRequestDto) throws Exception {
        log.info("복합 결제 승인 api");

        HashMap<String, JSONObject> compoundPayApprovalResult = compoundPayService.approval(request, newPaymentRequestDto, paymentProductListRequestDto);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, compoundPayApprovalResult), HttpStatus.OK);
    }


    // 복합 결제 거래 전체 취소 api
    @Operation(summary = "복합 결제 거래 취소 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/compound/allcancel", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> compoundPayAllCancel(
            HttpServletRequest request,
            @Parameter(description = "취소 정보") @RequestPart AllCancleRequestDto allCancleRequestDto) throws Exception {
        log.info("복합 결제 전체 취소 api");

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, compoundPayService.allcancel(request, allCancleRequestDto.getOrderNumber(), allCancleRequestDto.getOnnuryCancelPay(),allCancleRequestDto.getPgCancelPay(), allCancleRequestDto.getCancelRequestIdList())), HttpStatus.OK);
    }


    // 복합 결제 거래 부분 취소 api
    @Operation(summary = "복합 결제 거래 취소 api", tags = { "PaymentController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/compound/partcancel", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> compoundPayPartCancel(
            HttpServletRequest request,
            @Parameter(description = "취소 정보") @RequestPart PartCancleRequestDto partCancleRequestDto) throws Exception {
        log.info("복합 결제 부분 취소 api - seq : {}",partCancleRequestDto.getSeq() );

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, compoundPayService.partCancel(request, partCancleRequestDto)), HttpStatus.OK);
    }
}
