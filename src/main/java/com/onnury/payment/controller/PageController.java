package com.onnury.payment.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.common.util.LogUtil;
import com.onnury.payment.domain.PaymentApproval;
import com.onnury.payment.request.EasyPaymentApprovalInfo;
import com.onnury.payment.response.*;
import com.onnury.query.payment.PaymentQueryData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/page")
@RequiredArgsConstructor
@Controller
public class PageController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentQueryData paymentQueryData;

    /**
     * {
     * "merchantOrderDt":"20201101",
     * "merchantOrderID":"demo_order_no_684839495",
     * "tid":"asdfgw1234",
     * "totalAmount":1000,
     * "token":"token@@@@@"
     * }
     **/

    // 온누리 결제 승인 성공 정보 저장 및 성공 페이지 이동 api
    @Operation(summary = "온누리 결제 승인 성공 정보 저장 및 성공 페이지 이동 api", tags = {"PageController"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/biz_payment_result_approval", consumes = {MediaType.ALL_VALUE})
    public String bizPaymentApproval(
            @Parameter(description = "온누리 결제 승인 정보") @RequestBody String data) throws Exception {
        log.info("온누리 결제 승인 성공 정보 저장 및 성공 페이지 이동 api ");

        // URL 형식으로 전달받은 결제 정보 파라미터 디코딩
        String decodedURL = URLDecoder.decode(data, StandardCharsets.UTF_8.name());
        // 디코딩된 파라미터 분리화
        Map<String, String> params = parseURLParams(decodedURL);
        log.info("파싱된 데이터 확인 : {}", params);

        // 정형화된 객체에 결제 정보 저장
        OnnuryPaymentApprovalInfo onnuryPaymentApprovalInfo = OnnuryPaymentApprovalInfo.builder()
                .merchantOrderDt(params.get("merchantOrderDt"))
                .merchantOrderID(params.get("merchantOrderID"))
                .tid(params.get("tid"))
                .totalAmount(Integer.parseInt(params.get("totalAmount")))
                .token(params.get("token"))
                .status("O200")
                .build();

        // REDIS에 결제 정보 저장
        redisTemplate.opsForValue().set(params.get("merchantOrderID"), onnuryPaymentApprovalInfo);

        return "/success";
    }


    // 온누리 결제 취소 정보 저장 및 페이지 이동 api
    @Operation(summary = "온누리 결제 취소 정보 저장 및 페이지 이동 api", tags = {"PageController"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/biz_payment_result_cancel", consumes = {MediaType.ALL_VALUE})
    public String bizPaymentCancel(
            @Parameter(description = "온누리 결제 취소 정보") @RequestBody String data) throws Exception {
        log.info("온누리 결제 취소 정보 저장 및 페이지 이동 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("온누리 결제 취소 정보", data);

        try {
            // URL 형식으로 전달받은 결제 정보 파라미터 디코딩
            String decodedURL = URLDecoder.decode(data, StandardCharsets.UTF_8.name());
            // 디코딩된 파라미터 분리화
            Map<String, Object> params = parseURLParams2(decodedURL);
            log.info("파싱된 데이터 확인 : {}", params);

            // REDIS에 저장될 취소 객체
            OnnuryPaymentCancelInfo onnuryPaymentCancelInfo;

            // 기존 결제 시도 정보 호출
            PaymentApproval paymentApproval = paymentQueryData.getOnnuryPaymentApprovalInfo((String) params.get("merchantOrderID"));

            if (paymentApproval != null) {
                // 통합 취소일 경우
                if (params.get("partYn").equals("N")) {

                    // REDIS 저장 객체에 취소 정보 기입
                    onnuryPaymentCancelInfo = OnnuryPaymentCancelInfo.builder()
                            .merchantCancelDt((String) params.get("merchantCancelDt"))
                            .merchantCancelID((String) params.get("merchantCancelID"))
                            .merchantOrderDt((String) params.get("merchantOrderDt"))
                            .merchantOrderID((String) params.get("merchantOrderID"))
                            .tid((String) params.get("tid"))
                            .totalAmount((Integer) params.get("totalAmount"))
                            .totalCancelAmount((Integer) params.get("totalCancelAmount"))
                            .cancelTaxFreeAmount((Integer) params.get("cancelTaxFreeAmount"))
                            .cancelVatAmount((Integer) params.get("cancelVatAmount"))
                            .partYn((String) params.get("partYn"))
                            .status("C200")
                            .build();

                } else { // 부분 취소일 경우

                    // 부분 취소할 제품 리스트 추출
                    List<JSONObject> cancelProducts = (List<JSONObject>) params.get("productItems");


                    // 부분 취소할 제품 리스트 기준으로 각 제품들을 OnnuryPaymentCancelProductItem로 매핑 변환
                    List<OnnuryPaymentCancelProductItem> productItems = cancelProducts.stream()
                            .map(eachCancelProduct ->
                                    OnnuryPaymentCancelProductItem.builder()
                                            .seq((String) eachCancelProduct.get("seq"))
                                            .frc_cd((String) eachCancelProduct.get("frc_cd"))
                                            .biz_no((String) eachCancelProduct.get("biz_no"))
                                            .amount((Integer) eachCancelProduct.get("amount"))
                                            .cancelAmount((Integer) eachCancelProduct.get("cancelAmount"))
                                            .build()
                            )
                            .collect(Collectors.toList());

                    // REDIS 저장 객체에 취소 정보 기입
                    onnuryPaymentCancelInfo = OnnuryPaymentCancelInfo.builder()
                            .merchantCancelDt((String) params.get("merchantCancelDt"))
                            .merchantCancelID((String) params.get("merchantCancelID"))
                            .merchantOrderDt((String) params.get("merchantOrderDt"))
                            .merchantOrderID((String) params.get("merchantOrderID"))
                            .tid((String) params.get("tid"))
                            .totalAmount((Integer) params.get("totalAmount"))
                            .totalCancelAmount((Integer) params.get("totalCancelAmount"))
                            .cancelTaxFreeAmount((Integer) params.get("cancelTaxFreeAmount"))
                            .cancelVatAmount((Integer) params.get("cancelVatAmount"))
                            .partYn((String) params.get("partYn"))
                            .status("C200")
                            .productItems(productItems)
                            .build();
                }

                // REDIS에 결제 취소 정보 저장
                redisTemplate.opsForValue().set((String) params.get("merchantOrderID"), onnuryPaymentCancelInfo);

                return "/cancel";
            } else {
                LogUtil.logError("결제 취소 정보가 조회되지 않습니다.", requestParam);

                return "/failpage";
            }
        }catch(Exception e){
            LogUtil.logException(e, requestParam);
            return "/failpage";
        }
    }


    // 온누리 결제 실패 정보 저장 및 페이지 이동 api
    @Operation(summary = "온누리 결제 실패 정보 저장 및 페이지 이동 api", tags = {"PageController"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/biz_payment_result_fail/{merchantOrderId}", consumes = {MediaType.ALL_VALUE})
    public String bizPaymentFail(
            @Parameter(description = "온누리 결제 실패한 주문 번호") @PathVariable String merchantOrderId,
            @Parameter(description = "온누리 결제 실패 정보") @RequestBody String data) throws Exception {
        log.info("온누리 결제 실패 정보 저장 및 페이지 이동 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("온누리 결제 실패한 주문 번호", merchantOrderId);
        requestParam.put("온누리 결제 실패 정보", data);

        try{
            String decodedURL = URLDecoder.decode(data, StandardCharsets.UTF_8.name());
            Map<String, Object> params = parseURLParams2(decodedURL);

            // 결제 검증 실패 에러 코드, 에러 메세지, 검증 실패 가맹점 리스트 추출
            String errorCode = (String) params.get("errorCode");
            String errorMsg = (String) params.get("errorMsg");
            List<JSONObject> failMerchants = (List<JSONObject>) params.get("failMerchantList");

            List<OnnuryPaymentFailMerchant> failMerchantList = new ArrayList<>();

            // 결제 검증 실패한 가맹점 리스트들마다 실패 정보 redis에 저장
            failMerchants
                    .forEach(eachFailMerchant -> {
                        OnnuryPaymentFailMerchant failMerchant = OnnuryPaymentFailMerchant.builder()
                                .biz_no((String) eachFailMerchant.get("biz_no"))
                                .biz_nm((String) eachFailMerchant.get("biz_nm"))
                                .build();

                        failMerchantList.add(failMerchant);
                    });

            OnnuryPaymentFailResponseDto onnuryPaymentFailResponseDto = OnnuryPaymentFailResponseDto.builder()
                    .errorCode(errorCode)
                    .errorMsg(errorMsg)
                    .failMerchants(failMerchantList)
                    .status("F200")
                    .build();

            redisTemplate.opsForValue().set(merchantOrderId, onnuryPaymentFailResponseDto);

            return "/failpage";
        }catch(Exception e){
            LogUtil.logException(e, requestParam);
            return "/failpage";
        }
    }


    // EasyPay 결제 승인 성공 정보 저장 및 성공 페이지 이동 api
    @Operation(summary = "EasyPay 결제 승인 성공 정보 저장 및 성공 페이지 이동 api", tags = {"PageController"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/easy_payment_result_approval", consumes = {MediaType.ALL_VALUE})
    public String easyPaymentApproval(
            @Parameter(description = "EasyPay 결제 승인 성공 정보") @RequestParam HashMap<String, Object> data) {
        log.info("EasyPay 결제 승인 성공 정보 저장 및 성공 페이지 이동 api ");
        log.info("파싱된 데이터 확인 : {}", data);

        // 정형화된 객체에 결제 정보 저장
        EasyPaymentApprovalInfo easyPaymentApprovalInfo = EasyPaymentApprovalInfo.builder()
                .resCd((String) data.get("resCd"))
                .resMsg((String) data.get("resMsg"))
                .shopOrderNo((String) data.get("shopOrderNo"))
                .authorizationId((String) data.get("authorizationId"))
                .shopValue1((String) data.get("shopValue1"))
                .shopValue2((String) data.get("shopValue2"))
                .shopValue3((String) data.get("shopValue3"))
                .shopValue4((String) data.get("shopValue4"))
                .shopValue5((String) data.get("shopValue5"))
                .shopValue6((String) data.get("shopValue6"))
                .shopValue7((String) data.get("shopValue7"))
                .build();

        try{
            // REDIS에 결제 정보 저장
            redisTemplate.opsForValue().set("easy_" + data.get("shopOrderNo"), easyPaymentApprovalInfo);

            return "/success";
        }catch(Exception e){
            LogUtil.logException(e, easyPaymentApprovalInfo);
            return "/failpage";
        }
    }


    // 결제 관련 반환 데이터 컨버팅 함수
    private Map<String, String> parseURLParams(String url) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = url.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx != -1) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                params.put(key, value);
            }
        }

        return params;
    }


    // 결제 관련 반환 데이터 컨버팅 함수 2
    private Map<String, Object> parseURLParams2(String url) {
        Map<String, Object> params = new HashMap<>();
        String[] pairs = url.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx != -1) {
                String key = pair.substring(0, idx);
                Object value = pair.substring(idx + 1);
                params.put(key, value);
            }
        }

        return params;
    }

}
