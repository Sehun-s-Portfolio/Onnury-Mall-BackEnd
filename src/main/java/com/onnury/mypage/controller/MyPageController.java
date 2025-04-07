package com.onnury.mypage.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.common.util.LogUtil;
import com.onnury.mypage.request.ConfirmPaymentRequestDto;
import com.onnury.mypage.request.MyPageChangePasswordRequestDto;
import com.onnury.mypage.request.MyPageUpdateInfoRequestDto;
import com.onnury.mypage.request.UserCancleRequestDto;
import com.onnury.mypage.response.*;
import com.onnury.mypage.service.MyPageService;
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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@RestController
public class MyPageController {

    private final MyPageService myPageService;

    // 마이페이지 회원 정보 api
    @Operation(summary = "마이페이지 회원 정보 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MyPageInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/info", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getMyPageInfo(HttpServletRequest request) {
        log.info("마이페이지 회원 정보 api");

        try {
            MyPageInfoResponseDto myInfo = myPageService.getMyPageInfo(request);

            if (myInfo == null) {
                LogUtil.logError(StatusCode.CANT_GET_MY_INFO.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_MY_INFO, "마이페이지 정보를 조회할 수 없습니다. 재 로그인 해주십시오."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, myInfo), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request);
            return null;
        }
    }


    // 마이페이지 비밀번호 재설정 api
    @Operation(summary = "마이페이지 비밀번호 재설정 api", tags = {"MyPageController"})
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
    @PutMapping(value = "/change/password", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> changeMyPassword(
            HttpServletRequest request,
            @Parameter(description = "재설정 비밀번호 내용") @RequestBody MyPageChangePasswordRequestDto myPageChangePasswordRequestDto) {
        log.info("마이페이지 비밀번호 재설정 api");

        try {
            String changePasswordResult = myPageService.changeMyPassword(request, myPageChangePasswordRequestDto);

            if (changePasswordResult == null) {
                LogUtil.logError(StatusCode.CANT_CHANGE_MY_PASSWORD.getMessage(), request, myPageChangePasswordRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CHANGE_MY_PASSWORD, "비밀번호를 변경할 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, changePasswordResult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, myPageChangePasswordRequestDto);
            return null;
        }
    }


    // 마이페이지 회원 탈퇴 api
    // #!! 탈퇴 시 장바구니 내용 삭제 처리 필요
    @Operation(summary = "마이페이지 회원 탈퇴 api", tags = {"MyPageController"})
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
    @PutMapping(value = "/withdrawal", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> withdrawalAccount(HttpServletRequest request) {
        log.info("마이페이지 회원 탈퇴 api");

        try {
            String withdrawalResult = myPageService.withdrawalAccount(request);

            if (withdrawalResult == null) {
                LogUtil.logError(StatusCode.CANT_WITHDRAWAL_ACCOUNT.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_WITHDRAWAL_ACCOUNT, "회원 탈퇴를 진행하실 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, withdrawalResult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request);
            return null;
        }
    }


    // 마이페이지 회원 정보 수정 api
    @Operation(summary = "마이페이지 회원 정보 수정 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MyPageUpdateInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PutMapping(value = "/info/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateAccountInfo(
            HttpServletRequest request,
            @Parameter(description = "회원 정보 수정 내용") @RequestBody MyPageUpdateInfoRequestDto myPageUpdateInfoRequestDto) {
        log.info("마이페이지 회원 정보 수정 api");

        try {
            MyPageUpdateInfoResponseDto updateAccountInfo = myPageService.updateAccountInfo(request, myPageUpdateInfoRequestDto);

            if (updateAccountInfo == null) {
                LogUtil.logError(StatusCode.CANT_UPDATE_ACCOUNT_INFO.getMessage(), request, myPageUpdateInfoRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_ACCOUNT_INFO, "회원 정보를 수정할 수 없습니다. \n 다시 확인해주십시오."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, updateAccountInfo), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, myPageUpdateInfoRequestDto);
            return null;
        }
    }


    // 마이페이지 문의 내역 리스트 조회 api
    @Operation(summary = "마이페이지 문의 내역 리스트 조회 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalInquiryListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/inquiry/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getMyInquiryList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("마이페이지 문의 내역 리스트 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));

        try {
            TotalInquiryListResponseDto myInquiryList = myPageService.getMyInquiryList(request, page);

            if (myInquiryList == null) {
                LogUtil.logError(StatusCode.CANT_GET_INQUIRY.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_INQUIRY, "작성하신 문의 내역을 확인할 수 없습니다. 다시 로그인해주십시오."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, myInquiryList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 마이페이지 자신이 작성한 문의 내용 상세 조회 api
    @Operation(summary = "마이페이지 자신이 작성한 문의 내용 상세 조회 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MyPageInquiryDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getMyInquiryDetail(
            HttpServletRequest request,
            @Parameter(description = "문의 id") @RequestParam Long inquiryId) {
        log.info("마이페이지 자신이 작성한 문의 내용 상세 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("문의 id", Long.toString(inquiryId));

        try {
            MyPageInquiryDetailResponseDto getMyInquiryDetail = myPageService.getMyInquiryDetail(request, inquiryId);

            if (getMyInquiryDetail == null) {
                LogUtil.logError(StatusCode.CANT_GET_INQUIRY.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_INQUIRY, "작성하신 문의 내역을 확인할 수 없습니다. 다시 로그인해주십시오."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, getMyInquiryDetail), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 마이페이지 구매 이력 리스트 조회 api
    @Operation(summary = "마이페이지 구매 이력 리스트 조회 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalMyPaymentInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/payment/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getMyPaymentList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "조회 범위 시작 일자 (YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자 (YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("마이페이지 구매 이력 리스트 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("조회 범위 시작 일자 (YYYY-MM-DD)", startDate);
        requestParam.put("조회 범위 끝 일자 (YYYY-MM-DD)", endDate);

        try {
            JSONObject myPaymentList = myPageService.getMyPaymentList(request, page, startDate, endDate);

            if (myPaymentList == null) {
                LogUtil.logError(StatusCode.CANT_GET_MY_PAYMENTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_MY_PAYMENTS, "구매 이력을 조회할 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, myPaymentList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }

    // 마이페이지 취소 이력 리스트 조회 api
    @Operation(summary = "마이페이지 취소 리스트 조회 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalMyPaymentInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/mycancle/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getMyCancleList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "조회 범위 시작 일자 (YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "조회 범위 끝 일자 (YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("마이페이지 취소 이력 리스트 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));
        requestParam.put("조회 범위 시작 일자 (YYYY-MM-DD)", startDate);
        requestParam.put("조회 범위 끝 일자 (YYYY-MM-DD)", endDate);

        try {
            JSONObject myPaymentList = myPageService.getMyCancleList(request, page, startDate, endDate);

            if (myPaymentList == null) {
                LogUtil.logError(StatusCode.CANT_GET_MY_CANCEL_PAYMENTS.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_MY_CANCEL_PAYMENTS, "취소 이력을 조회할 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, myPaymentList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }

    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/payment/canclerequest", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> postcanclerequest(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestPart UserCancleRequestDto userCancleRequestDto) {
        log.info("마이페이지 주문취소 요청 api");

        try {
            JSONObject myPaymentList = myPageService.getMyCancleRequest(userCancleRequestDto);

            if (myPaymentList == null) {
                LogUtil.logError(StatusCode.CANT_REQUEST_PAYMENT_CANCEL.getMessage(), request, userCancleRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REQUEST_PAYMENT_CANCEL, "주문을 취소할 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, myPaymentList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, userCancleRequestDto);
            return null;
        }
    }


    // 마이페이지 결제 주문 확정 api
    @Operation(summary = "마이페이지 결제 주문 확정 api", tags = {"MyPageController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ConfirmPaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PutMapping(value = "/confirm/payment", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> confirmMyPayment(
            HttpServletRequest request,
            @Parameter(description = "주문 확정할 주문 정보") @RequestBody ConfirmPaymentRequestDto confirmPaymentRequestDto) {
        log.info("마이페이지 결제 주문 확정 api");

        try {
            ConfirmPaymentResponseDto confirmStatus = myPageService.confirmMyPayment(request, confirmPaymentRequestDto);

            if (confirmStatus != null) {
                if (confirmStatus.getConfirmPurchaseStatus().equals("N")) {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CONFIRM_PAYMENT, null), HttpStatus.OK);
                } else if (confirmStatus.getConfirmPurchaseStatus().equals("C")) {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.EXIST_CANCEL_INFO_THAN_CANT_CONFIRM_PAYMENT, null), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseBody(StatusCode.OK, confirmStatus), HttpStatus.OK);
                }
            } else {
                LogUtil.logError(StatusCode.CANT_CONFIRM_PAYMENT.getMessage(), request, confirmPaymentRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CONFIRM_PAYMENT, null), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, confirmPaymentRequestDto);
            return null;
        }
    }
}
