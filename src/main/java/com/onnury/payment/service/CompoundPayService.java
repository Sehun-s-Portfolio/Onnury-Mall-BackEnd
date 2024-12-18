package com.onnury.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.member.domain.Member;
import com.onnury.payment.domain.*;
import com.onnury.payment.repository.*;
import com.onnury.payment.request.*;
import com.onnury.payment.response.OnnuryPaymentApprovalInfo;
import com.onnury.query.payment.PaymentQueryData;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.onnury.cart.domain.QCart.cart;
import static com.onnury.payment.domain.QCancleOrder.cancleOrder;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.supplier.domain.QSupplier.supplier;
import static com.onnury.payment.domain.QOrderInDeliveryAddPrice.orderInDeliveryAddPrice;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompoundPayService {
    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BizPointCodeccService bizPointCodeccService;
    private final PaymentZppReqRepository paymentZppReqRepository;
    private final PaymentApprovalRepository paymentApprovalRepository;
    private final PaymentRepository paymentRepository;
    private final CancleOrderRepository cancleOrderRepository;
    private final OrderInProductRepository orderInProductRepository;
    private final OrderInDeliveryAddPriceRepository orderInDeliveryAddPriceRepository;
    private final EasyPaymentApprovalRepository easyPaymentApprovalRepository;
    private final EasyPaymentBasketInfoRepository easyPaymentBasketInfoRepository;
    private final EasyPayCodeccService easyPayCodeccService;
    private final PaymentQueryData paymentQueryData;
    private final JwtTokenProvider jwtTokenProvider;
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Value("${onnury.auth.key}")
    private String _AUTH_KEY_; // 헤더 인증키

    @Value("${onnury.biz.payment.url}")
    private String BZPURL; // 비즈 플레이 측 연동 api 호출 경로

    @Value("${onnury.biz.payment.approval.page.url}")
    private String approvalUrl; // 결제 승인 성공 호출 url

    @Value("${onnury.biz.payment.cancel.page.url}")
    private String cancelUrl; // 온누리 결제 취소 호출 url

    @Value("${onnury.biz.payment.fail.page.url}")
    private String failUrl; // 온누리 결제 실패 호출 url

    @Value("${easy.payment.mid}")
    private String PGMID; // 가맹점 ID

    @Value("${easy.payment.url}")
    private String PGURL; // EasyPay 측 연동 api 호출 경로

    @Value("${easy.payment.approval.page.url}")
    private String approvalPageUrl; // EasyPay 승인 페이지 호출 url

    @Value("${onnury.biz.mid}")
    private String _MID_;


    // 복합 거래 승인 service
    @Transactional
    public HashMap<String, JSONObject> approval(
            HttpServletRequest request, NewPaymentRequestDto newPaymentRequestDto, List<PaymentProductListRequestDto> PaymentProductListRequestDto) throws Exception {
        log.info("복합 거래 승인 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        // [ OnnuryPay ]
        log.info("OnnuryPay 거래 승인 절차 시작");

        HashMap<String, JSONObject> compountPayApprovalResult = new HashMap<>();

        JSONObject ounnuryReturnMsg = new JSONObject();
        OnnuryPaymentApprovalInfo onnuryinfo = (OnnuryPaymentApprovalInfo) redisTemplate.opsForValue().get(newPaymentRequestDto.getOrderNumber());

        // 공통부의 EV값
        JSONObject onnuryJsonData = new JSONObject();
        onnuryJsonData.put("merchantOrderDt", onnuryinfo.getMerchantOrderDt());
        onnuryJsonData.put("merchantOrderID", onnuryinfo.getMerchantOrderID());
        onnuryJsonData.put("tid", onnuryinfo.getTid());
        onnuryJsonData.put("totalAmount", onnuryinfo.getTotalAmount());
        onnuryJsonData.put("token", onnuryinfo.getToken());

        String reqEV = bizPointCodeccService.biztotpayEncCode(onnuryJsonData.toString());
        String reqVV = bizPointCodeccService.getHmacSha256(onnuryJsonData.toString());

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
        Date d = new Date();
        String date = sf.format(d);

        JSONObject onnuryRequestData = new JSONObject();
        onnuryRequestData.put("MID", _MID_);
        onnuryRequestData.put("RQ_DTIME", date);
        onnuryRequestData.put("TNO", "T" + onnuryinfo.getMerchantOrderID() + "_approval");
        onnuryRequestData.put("EV", reqEV);
        onnuryRequestData.put("VV", reqVV);
        onnuryRequestData.put("RC", "");
        onnuryRequestData.put("RM", "");

        String onnuryUrl = BZPURL + "api_v1_payment_approval.jct";

        byte[] onnuryResponseMessage = null;

        HttpURLConnection onnuryConn;

        try {
            onnuryConn = (HttpURLConnection) new URL(onnuryUrl).openConnection();
            onnuryConn.setDoInput(true);
            onnuryConn.setDoOutput(true);
            onnuryConn.setRequestMethod("POST");
            onnuryConn.setRequestProperty("Content-Type",
                    "application/json; charset=UTF-8");
            onnuryConn.setRequestProperty("Authorization", _AUTH_KEY_);
            onnuryConn.setUseCaches(false);

            OutputStreamWriter onnuryOutputStream = new OutputStreamWriter(onnuryConn.getOutputStream());
            onnuryOutputStream.write(String.valueOf(onnuryRequestData));
            onnuryOutputStream.flush();
            onnuryOutputStream.close();

            DataInputStream onnuryInputStream = new DataInputStream(onnuryConn.getInputStream());
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            byte[] buf = new byte[2048];

            while (true) {
                int n = onnuryInputStream.read(buf);
                if (n == -1)
                    break;
                bout.write(buf, 0, n);
            }

            bout.flush();
            onnuryResponseMessage = bout.toByteArray();
            onnuryConn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 결제한 제품들 (온누리에서 결제 승인 이후 EasyPay 쪽에 전달하여 EasyPay 내용 업데이트)
        List<OrderInProduct> createOrderInproduct;
        Map<String, Object> onnuryResultEVJsonData = new HashMap<>();

        if (onnuryResponseMessage != null) {
            String temp = new String(onnuryResponseMessage, "UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> onnuryResultData = mapper.readValue(temp, Map.class);

            if (onnuryResultData.get("RC").equals("0000")) {
                Map<String, Object> onnuryResultEV = mapper.readValue(bizPointCodeccService.biztotpayDecCode((String) onnuryResultData.get("EV")), Map.class);
                log.info("{}", onnuryResultEV);

                String onnuryResCode = (String) onnuryResultEV.get("code");

                if (onnuryResCode.equals("0000")) {
                    // 디비 저장

                    onnuryResultEVJsonData = (Map<String, Object>) onnuryResultEV.get("data");


                    PaymentApproval paymentApproval = PaymentApproval.builder()
                            .merchantOrderDt((String) onnuryResultEVJsonData.get("merchantOrderDt"))
                            .merchantOrderID((String) onnuryResultEVJsonData.get("merchantOrderID"))
                            .tid((String) onnuryResultEVJsonData.get("tid"))
                            .productName((String) onnuryResultEVJsonData.get("productName"))
                            .totalAmount((Integer) onnuryResultEVJsonData.get("totalAmount"))
                            .taxFreeAmount((Integer) onnuryResultEVJsonData.get("taxFreeAmount"))
                            .vatAmount((Integer) onnuryResultEVJsonData.get("vatAmount"))
                            .approvedAt((String) onnuryResultEVJsonData.get("approvedAt"))
                            .bankCd((String) onnuryResultEVJsonData.get("bankCd"))
                            .accountNo((String) onnuryResultEVJsonData.get("accountNo"))
                            .payMeasureTp((String) onnuryResultEVJsonData.get("payMeasureTp"))
                            .payZppNote((String) onnuryResultEVJsonData.get("payZppNote"))
                            .build();

                    paymentApprovalRepository.save(paymentApproval);

                    List<Map<String, Object>> payZppReqListJsonData = (List<Map<String, Object>>) onnuryResultEVJsonData.get("payZppReqList");

                    // 사용 상품권 저장
                    List<PaymentZppReq> paymentZppReqList = payZppReqListJsonData.stream()
                            .map(eachPayZpp -> PaymentZppReq.builder()
                                    .seq((Integer) eachPayZpp.get("seq"))
                                    .zppID((String) eachPayZpp.get("zppID"))
                                    .zppName((String) eachPayZpp.get("zppNm"))
                                    .sellerID((String) eachPayZpp.get("sellerID"))
                                    .sellerName((String) eachPayZpp.get("sellerNm"))
                                    .amount((Integer) eachPayZpp.get("amount"))
                                    .paymentApprovalId(paymentApproval.getPaymentApprovalId())
                                    .build()
                            )
                            .collect(Collectors.toList());

                    paymentZppReqRepository.saveAll(paymentZppReqList);

                    ounnuryReturnMsg.put("resCd", onnuryResultEV.get("code"));
                    ounnuryReturnMsg.put("data", onnuryResultEV.get("data"));

                    compountPayApprovalResult.put("onnuryApprovalInfo", ounnuryReturnMsg);

                } else {
                    ounnuryReturnMsg.put("resCd", onnuryResultEV.get("code"));
                    ounnuryReturnMsg.put("data", onnuryResultEV.get("msg"));

                    compountPayApprovalResult.put("onnuryApprovalInfo", ounnuryReturnMsg);

                    return compountPayApprovalResult;
                }
            } else {
                ounnuryReturnMsg.put("resCd", onnuryResultData.get("RC"));
                ounnuryReturnMsg.put("data", onnuryResultData.get("RM"));

                compountPayApprovalResult.put("onnuryApprovalInfo", ounnuryReturnMsg);

                return compountPayApprovalResult;
            }
        } else {
            ounnuryReturnMsg.put("resCd", "9999");
            ounnuryReturnMsg.put("data", "POINT 시스템 에러");

            compountPayApprovalResult.put("onnuryApprovalInfo", ounnuryReturnMsg);

            return compountPayApprovalResult;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // [ EasyPay ]
        log.info("EasyPay 거래 승인 절차 시작");

        SimpleDateFormat easyPayDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date d2 = new Date();
        String easyPayDate = easyPayDateFormat.format(d2);

        EasyPaymentApprovalInfo getEasyPaymentApprovalInfo = (EasyPaymentApprovalInfo) redisTemplate.opsForValue().get("easy_" + newPaymentRequestDto.getOrderNumber());



        /**
        if (getEasyPaymentApprovalInfo == null) {
            compountPayApprovalResult.put("easyPayApprovalInfo", null);
            return compountPayApprovalResult;
        }
         **/

        JSONObject easyPayRequestData = new JSONObject();
        easyPayRequestData.put("mallId", PGMID);
        easyPayRequestData.put("shopTransactionId", "approval_" + getEasyPaymentApprovalInfo.getShopOrderNo());
        easyPayRequestData.put("authorizationId", getEasyPaymentApprovalInfo.getAuthorizationId());
        easyPayRequestData.put("shopOrderNo", getEasyPaymentApprovalInfo.getShopOrderNo());
        easyPayRequestData.put("approvalReqDate", easyPayDate);

        URL easyPayUrl = new URL(PGURL + "approval"); // 호출할 외부 API 를 입력한다.

        HttpURLConnection easyPayConn = (HttpURLConnection) easyPayUrl.openConnection(); // header에 데이터 통신 방법을 지정한다.
        easyPayConn.setRequestMethod("POST");
        easyPayConn.setRequestProperty("Content-Type", "application/json; utf-8");

        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
        easyPayConn.setDoOutput(true);

        // Request body message에 전송
        OutputStreamWriter easyPayOutputStream = new OutputStreamWriter(easyPayConn.getOutputStream());
        easyPayOutputStream.write(easyPayRequestData.toString());
        easyPayOutputStream.flush();

        // 응답
        BufferedReader EasyPayInputStream = new BufferedReader(new InputStreamReader(easyPayConn.getInputStream(), "UTF-8"));
        JSONObject easyPayJsonObj = (JSONObject) JSONValue.parse(EasyPayInputStream.readLine());

        /**
        if (easyPayJsonObj == null || !easyPayJsonObj.get("resCd").equals("0000")) {
            compountPayApprovalResult.put("easyPayApprovalInfo", easyPayJsonObj);
            return compountPayApprovalResult;
        }
         **/

        easyPayJsonObj.put("authorizationId", getEasyPaymentApprovalInfo.getAuthorizationId());

        EasyPayInputStream.close();
        easyPayConn.disconnect();

        log.info("EasyPay 최종 거래 승인 후 응답 데이터 확인");
        log.info("{}", easyPayJsonObj);

        String transactionDate = (String) easyPayJsonObj.get("transactionDate");
        String year = transactionDate.substring(0, 4);
        String month = transactionDate.substring(4, 6);
        String day = transactionDate.substring(6, 8);
        String hour = transactionDate.substring(8, 10);
        String minute = transactionDate.substring(10, 12);
        String second = transactionDate.substring(12);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        String convertDate = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        LocalDateTime dateTime = LocalDateTime.parse(convertDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // EasyPay 거래 승인 정보 데이터들을 추출하여 EasyPaymentApproval 에 알맞은 각 데이터로 다시 한번 추출
        JSONObject paymentInfoData = (JSONObject) easyPayJsonObj.get("paymentInfo"); // 총 결제 정보
        JSONObject cardInfoData = (JSONObject) paymentInfoData.get("cardInfo"); // 총 결제 정보 속 카드 정보
        JSONObject bankInfoData = (JSONObject) paymentInfoData.get("bankInfo"); // 총 결제 정보 속 은행 정보
        JSONObject virtualAccountInfoData = (JSONObject) paymentInfoData.get("virtualAccountInfo"); // 총 결제 정보 속 가상 은행 정보
        JSONObject mobInfoData = (JSONObject) paymentInfoData.get("mobInfo"); // 총 결제 정보 속 모바일 정보
        JSONObject prepaidInfoData = (JSONObject) paymentInfoData.get("prepaidInfo"); // 총 결제 정보 속 뱅크 월렛 정보
        JSONObject cashReceiptInfoData = (JSONObject) paymentInfoData.get("cashReceiptInfo"); // 총 결제 정보 속 현금 영수증 정보

        Payment payment = Payment.builder()
                .orderNumber(onnuryinfo.getMerchantOrderID())
                .buyMemberLoginId(newPaymentRequestDto.getBuyMemberLoginId())
                .receiver(newPaymentRequestDto.getReceiver())
                .postNumber(newPaymentRequestDto.getPostNumber())
                .address(newPaymentRequestDto.getAddress())
                .message(newPaymentRequestDto.getMessage())
                .receiverPhone(newPaymentRequestDto.getReceiverPhone())
                .linkCompany(newPaymentRequestDto.getLinkCompany())
                .onNuryStatementNumber((String) onnuryResultEVJsonData.get("tid"))
                .onNuryApprovalPrice((Integer) onnuryResultEVJsonData.get("totalAmount"))
                .creditStatementNumber((String) easyPayJsonObj.get("pgCno"))
                .creditApprovalPrice(((Long) easyPayJsonObj.get("amount")).intValue())
                .totalApprovalPrice((Integer) onnuryResultEVJsonData.get("totalAmount") + ((Long) easyPayJsonObj.get("amount")).intValue())
                .orderedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);


        List<PaymentProductListRequestDto> prodctReqList = PaymentProductListRequestDto;

        double onnuryCommissionCheck = 0.0;
        double creditCommissionCheck = 0.0;
        List<OrderInProduct> productMapList = new ArrayList<>();
        List<OrderInDeliveryAddPrice> OrderInDeliveryAddPriceMapList = new ArrayList<>();

        for (int p = 0; p < prodctReqList.size(); p++) {
            if(!prodctReqList.get(p).getProductName().contains("도서산간,제주권 추가비용")){
                Tuple productEventInfo = jpaQueryFactory
                        .select(product.eventDescription, product.eventStartDate, product.eventEndDate, product.supplierId)
                        .from(product)
                        .where(product.classificationCode.eq(prodctReqList.get(p).getProductClassificationCode()))
                        .fetchOne();

                assert productEventInfo != null;

                Tuple supplierCommissionInfo = jpaQueryFactory
                        .select(supplier.onnuryCommission, supplier.creditCommission)
                        .from(supplier)
                        .where(supplier.supplierId.eq(productEventInfo.get(product.supplierId)))
                        .fetchOne();

                int onnuryPay = Integer.parseInt(prodctReqList.get(p).getOnnurypay().replace(",", ""));
                int creditPay = prodctReqList.get(p).getProductTotalAmount() - onnuryPay;

                int onnuryCommissionPrice = 0;

                if(supplierCommissionInfo.get(supplier.onnuryCommission) != null ){
                    onnuryCommissionCheck = supplierCommissionInfo.get(supplier.onnuryCommission);

                    if(onnuryCommissionCheck > 0){
                        onnuryCommissionPrice = (int)(onnuryPay * (onnuryCommissionCheck / 100.0));
                        String onnuryCommissionPriceCutLastIndexPrice = String.valueOf(onnuryCommissionPrice);

                        if(onnuryCommissionPriceCutLastIndexPrice.length() != 1){
                            String finalOnnuryCommissionPrice = onnuryCommissionPriceCutLastIndexPrice.substring(0, onnuryCommissionPriceCutLastIndexPrice.length() - 1) + "0";
                            onnuryCommissionPrice = Integer.parseInt(finalOnnuryCommissionPrice);
                        }else{
                            onnuryCommissionPrice = 0;
                        }
                    }
                }

                int creditCommissionPrice = 0;

                if(supplierCommissionInfo.get(supplier.creditCommission) != null){
                    creditCommissionCheck = supplierCommissionInfo.get(supplier.creditCommission);

                    if(creditCommissionCheck > 0){
                        creditCommissionPrice = (int)(creditPay * (creditCommissionCheck / 100.0));
                        String creditCommissionPriceCutLastIndexPrice = String.valueOf(creditCommissionPrice);

                        if(creditCommissionPriceCutLastIndexPrice.length() != 1) {
                            String finalCreditCommissionPrice = creditCommissionPriceCutLastIndexPrice.substring(0, creditCommissionPriceCutLastIndexPrice.length() - 1) + "0";
                            creditCommissionPrice = Integer.parseInt(finalCreditCommissionPrice);
                        }else {
                            creditCommissionPrice = 0;
                        }
                    }
                }

                OrderInProduct OrderInProductInfo =  OrderInProduct.builder()
                        .orderNumber(onnuryinfo.getMerchantOrderID())
                        .seq(prodctReqList.get(p).getSeq())
                        .productName(prodctReqList.get(p).getProductName())
                        .productClassificationCode(prodctReqList.get(p).getProductClassificationCode()) //
                        .detailOptionTitle(prodctReqList.get(p).getDetailOptionTitle())
                        .supplierId(prodctReqList.get(p).getSupplierId())
                        .frcNumber(prodctReqList.get(p).getFrcNumber())
                        .businessNumber(prodctReqList.get(p).getBusinessNumber())
                        .productAmount(prodctReqList.get(p).getProductAmount())
                        .productOptionAmount(prodctReqList.get(p).getProductOptionAmount())
                        .quantity(prodctReqList.get(p).getQuantity())
                        .deliveryPrice(prodctReqList.get(p).getDeliveryPrice())
                        .dangerPlacePrice(prodctReqList.get(p).getDangerPlacePrice())
                        .onnurypay(onnuryPay)
                        .productTotalAmount(prodctReqList.get(p).getProductTotalAmount() + prodctReqList.get(p).getDeliveryPrice() + prodctReqList.get(p).getDangerPlacePrice())
                        .memo(String.valueOf(prodctReqList.get(p).getMemo()))
                        .transportNumber("")
                        .parcelName("")
                        .completePurchaseCheck("N")
                        .cancelAmount(0)
                        .cartId(prodctReqList.get(p).getCartId())
                        .onnuryCommissionPrice(onnuryCommissionPrice)
                        .creditCommissionPrice(creditCommissionPrice)
                        .eventCheck(LocalDateTime.now().isAfter(productEventInfo.get(product.eventStartDate)) && LocalDateTime.now().isBefore(productEventInfo.get(product.eventEndDate)) ? "Y" : "N")
                        .eventInfo(productEventInfo.get(product.eventDescription))
                        .build();

                productMapList.add(OrderInProductInfo);

            } else {
                int onnuryPay = Integer.parseInt(prodctReqList.get(p).getOnnurypay().replace(",", ""));
                int creditPay = prodctReqList.get(p).getProductTotalAmount() - onnuryPay;

                OrderInDeliveryAddPrice odInfo = OrderInDeliveryAddPrice.builder()
                        .orderNumber(onnuryinfo.getMerchantOrderID())
                        .seq(prodctReqList.get(p).getSeq())
                        .productName(prodctReqList.get(p).getProductName())
                        .amount(prodctReqList.get(p).getProductAmount())
                        .onnuryPay(onnuryPay)
                        .creditPay(creditPay)
                        .cancleStatus("N")
                        .supplierId(prodctReqList.get(p).getSupplierId())
                        .build();

                OrderInDeliveryAddPriceMapList.add(odInfo);
            }
        }
        orderInProductRepository.saveAll(productMapList);
        orderInDeliveryAddPriceRepository.saveAll(OrderInDeliveryAddPriceMapList);

        // EasyPay 전용 거래 승인 정보 기입
        EasyPaymentApproval easyPaymentApproval = EasyPaymentApproval.builder()
                .mallId((String) easyPayJsonObj.get("mallId"))
                .pgCno((String) easyPayJsonObj.get("pgCno"))
                .shopTransactionId((String) easyPayJsonObj.get("shopTransactionId"))
                .shopOrderNo((String) easyPayJsonObj.get("shopOrderNo"))
                .amount((Long) easyPayJsonObj.get("amount"))
                .transactionDate((String) easyPayJsonObj.get("transactionDate"))
                .statusCode((String) easyPayJsonObj.get("statusCode"))
                .statusMessage((String) easyPayJsonObj.get("statusMessage"))
                .msgAuthValue((String) easyPayJsonObj.get("msgAuthValue"))
                .escrowUsed((String) easyPayJsonObj.get("escrowUsed"))
                .payMethodTypeCode((String) paymentInfoData.get("payMethodTypeCode"))
                .approvalNo((String) paymentInfoData.get("approvalNo"))
                .approvalDate((String) paymentInfoData.get("approvalDate"))
                .cpCode((String) paymentInfoData.get("cpCode"))
                .multiCardAmount((String) paymentInfoData.get("multiCardAmount"))
                .multiPntAmount((String) paymentInfoData.get("multiPntAmount"))
                .multiCponAmount((String) paymentInfoData.get("multiCponAmount"))
                .cardNo((String) cardInfoData.get("cardNo"))
                .issuerCode((String) cardInfoData.get("issuerCode"))
                .issuerName((String) cardInfoData.get("issuerName"))
                .acquirerCode((String) cardInfoData.get("acquirerCode"))
                .acquirerName((String) cardInfoData.get("acquirerName"))
                .installmentMonth(((Long) cardInfoData.get("installmentMonth")).toString())
                .freeInstallmentTypeCode((String) cardInfoData.get("freeInstallmentTypeCode"))
                .cardGubun((String) cardInfoData.get("cardGubun"))
                .cardBizGubun((String) cardInfoData.get("cardBizGubun"))
                .partCancelUsed((String) cardInfoData.get("partCancelUsed"))
                .couponAmount((Long) cardInfoData.get("couponAmount"))
                .subCardCd((String) cardInfoData.get("subCardCd"))
                .vanSno((String) cardInfoData.get("vanSno"))
                .bankCode((String) bankInfoData.get("bankCode"))
                .bankName((String) bankInfoData.get("bankName"))
                .virtualBankCode((String) virtualAccountInfoData.get("bankCode"))
                .virtualBankName((String) virtualAccountInfoData.get("bankName"))
                .accountNo((String) virtualAccountInfoData.get("accountNo"))
                .depositName((String) virtualAccountInfoData.get("depositName"))
                .expiryDate((String) virtualAccountInfoData.get("expiryDate"))
                .authId((String) mobInfoData.get("authId"))
                .mobBillId((String) mobInfoData.get("billId"))
                .mobileNo((String) mobInfoData.get("mobileNo"))
                .mobileAnsimUsed((String) mobInfoData.get("mobileAnsimUsed"))
                .prepaidBillId((String) prepaidInfoData.get("billId"))
                .prepaidRemainAmount((Long) prepaidInfoData.get("remainAmount"))
                .cashReceiptResCd((String) cashReceiptInfoData.get("resCd"))
                .cashReceiptResMsg((String) cashReceiptInfoData.get("resMsg"))
                .cashReceiptApprovalNo((String) cashReceiptInfoData.get("approvalNo"))
                .cashReceiptApprovalDate((String) cashReceiptInfoData.get("approvalDate"))
                .build();

        // EasyPay 전용 거래 승인 정보 저장
        easyPaymentApprovalRepository.save(easyPaymentApproval);

        // 총 결제 정보 속 장바구니 정보 추출
        List<Map<String, Object>> paymentInfoDataList = (List<Map<String, Object>>) paymentInfoData.get("basketInfoList");

        // 추출한 장바구니 정보를 기준으로 EasyPaymentBasketInfo 장바구니 데이터 리스트 전환
        List<EasyPaymentBasketInfo> savePaymentInfoList = paymentInfoDataList.stream()
                .map(eachPaymentInfo ->
                        EasyPaymentBasketInfo.builder()
                                .productNo((String) eachPaymentInfo.get("productNo"))
                                .productPgCno((String) eachPaymentInfo.get("productPgCno"))
                                .sellerId((String) eachPaymentInfo.get("sellerId"))
                                .easyPaymentApprovalId(easyPaymentApproval.getEasyPaymentApprovalId())
                                .build()
                )
                .collect(Collectors.toList());

        // EasyPaymentBasketInfo 장바구니 데이터 일괄 저장
        easyPaymentBasketInfoRepository.saveAll(savePaymentInfoList);

        // 성공 코드 부여
        easyPayRequestData.put("resCd", easyPayJsonObj.get("resCd"));

        compountPayApprovalResult.put("easyPayApprovalInfo", easyPayRequestData);

        // 결제 완료 후 장바구니 데이터 삭제
        List<Long> deleteCartIdList = productMapList.stream()
                .map(OrderInProduct::getCartId)
                .filter(cartId -> cartId != 0L)
                .collect(Collectors.toList());

        if (!deleteCartIdList.isEmpty()) {
            jpaQueryFactory
                    .delete(cart)
                    .where(cart.memberId.eq(authMember.getMemberId())
                            .and(cart.cartId.in(deleteCartIdList))
                    )
                    .execute();

            entityManager.flush();
            entityManager.clear();
        }

        log.info("온누리 가격 : {}", productMapList.get(0).getOnnurypay());
        log.info("공급사 온누리 수수료 % : {}", onnuryCommissionCheck);
        log.info("공급사 신용카드 수수료 % : {}", creditCommissionCheck);
        log.info("신용 카드 수수료 : {}", productMapList.get(0).getCreditCommissionPrice());
        log.info("온누리 수수료 : {}", productMapList.get(0).getOnnuryCommissionPrice());

        return compountPayApprovalResult;
    }

    // 결제 전체 취소
    @Transactional
    public JSONObject allcancel(HttpServletRequest request, String orderNumber, int onuryCanclePay, int pgCanclePay, List<Long> cancelRequestIdList) throws Exception {

        JSONObject compountPayApprovalResult = new JSONObject();
        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Payment result = paymentQueryData.PaymentInfo(orderNumber);
        List<OrderInProduct> odProductList = paymentQueryData.ProductList(orderNumber);


        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date d1 = new Date();
        String merchantCancelDt = sf1.format(d1);
        String merchantCancelID = sf2.format(d1);
        Date orderdate = java.sql.Timestamp.valueOf(result.getOrderedAt());
        String merchantOrderDt = sf1.format(orderdate);
        JSONObject reqParams = new JSONObject();
        JSONObject ounnuryReturnMsg = new JSONObject();


        if (onuryCanclePay > 0 && pgCanclePay > 0) {

            reqParams.put("merchantCancelDt", merchantCancelDt);
            reqParams.put("merchantCancelID", merchantCancelID);
            reqParams.put("merchantOrderDt", merchantOrderDt);
            reqParams.put("merchantOrderID", result.getOrderNumber());
            reqParams.put("tid", result.getOnNuryStatementNumber());
            reqParams.put("totalAmount", result.getOnNuryApprovalPrice());
            reqParams.put("totalCancelAmount", onuryCanclePay);
            reqParams.put("cancelVatAmount", 0);
            reqParams.put("cancelTaxFreeAmount", 0);
            reqParams.put("partYn", "N");

            String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams.toString());
            String reqVV = bizPointCodeccService.getHmacSha256(reqParams.toString());

            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
            Date d = new Date();
            String date = sf.format(d);


            JSONObject postBody = new JSONObject();
            postBody.put("MID", _MID_);
            postBody.put("RQ_DTIME", date);
            postBody.put("TNO", result.getOrderNumber() + "_cancel");
            postBody.put("EV", reqEV);
            postBody.put("VV", reqVV);
            postBody.put("RC", "");
            postBody.put("RM", "");

            String url = BZPURL + "api_v1_payment_cancel.jct "; //결제준비URL

            byte[] resMessage = null;
            HttpURLConnection conn;

            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", _AUTH_KEY_);
                conn.setUseCaches(false);

                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                os.write(String.valueOf(postBody));
                os.flush();
                os.close();

                DataInputStream in = new DataInputStream(conn.getInputStream());
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                int bcout = 0;
                byte[] buf = new byte[2048];
                while (true) {
                    int n = in.read(buf);
                    if (n == -1)
                        break;
                    bout.write(buf, 0, n);
                }
                bout.flush();
                resMessage = bout.toByteArray();
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (resMessage != null) {
                String temp = new String(resMessage, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> bizresult = mapper.readValue(temp, Map.class);
                if (bizresult.get("RC").equals("0000")) {
                    Map<String, Object> bizresultEV = mapper.readValue(bizPointCodeccService.biztotpayDecCode((String) bizresult.get("EV")), Map.class);
                    String redcode = (String) bizresultEV.get("code");
                    if (redcode.equals("0000")) {
                        // 디비 저장
                        Map<String, Object> bizresultEVJsonData = (Map<String, Object>) bizresultEV.get("data");

                        JSONObject requestdata = new JSONObject();
                        requestdata.put("mallId", PGMID);
                        requestdata.put("shopTransactionId", result.getOrderNumber() + "_cancel");
                        requestdata.put("pgCno", result.getCreditStatementNumber());
                        requestdata.put("reviseTypeCode", "40");
                        requestdata.put("remainAmount", result.getCreditApprovalPrice());
                        requestdata.put("amount", pgCanclePay);
                        requestdata.put("clientIp", "211.253.30.157");
                        requestdata.put("clientId", "onnury");
                        String encMsg = result.getCreditStatementNumber() + "|" + result.getOrderNumber() + "_cancel";
                        requestdata.put("msgAuthValue", easyPayCodeccService.easypayDeccode(encMsg));
                        requestdata.put("cancelReqDate", merchantCancelDt);


                        URL easyPayUrl = new URL(PGURL + "revise"); // 호출할 외부 API 를 입력한다.

                        HttpURLConnection easyPayConn = (HttpURLConnection) easyPayUrl.openConnection(); // header에 데이터 통신 방법을 지정한다.
                        easyPayConn.setRequestMethod("POST");
                        easyPayConn.setRequestProperty("Content-Type", "application/json; utf-8");

                        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
                        easyPayConn.setDoOutput(true);

                        // Request body message에 전송
                        OutputStreamWriter os = new OutputStreamWriter(easyPayConn.getOutputStream());
                        os.write(requestdata.toString());
                        os.flush();

                        // 응답
                        BufferedReader in = new BufferedReader(new InputStreamReader(easyPayConn.getInputStream(), "UTF-8"));
                        JSONObject cancelData = (JSONObject) JSONValue.parse(in.readLine());
                        in.close();
                        easyPayConn.disconnect();

                        if (cancelData.get("resCd").equals("0000")) {

                            if (!cancelRequestIdList.isEmpty()) {

                                List<CancleOrder> cancelInfoList = new ArrayList<>();

                                odProductList.stream()
                                        .map(eachCancelInfo ->
                                                CancleOrder.builder()
                                                        .orderNumber(eachCancelInfo.getOrderNumber())
                                                        .seq(eachCancelInfo.getSeq())
                                                        .productName(eachCancelInfo.getProductName())
                                                        .productClassificationCode(eachCancelInfo.getProductClassificationCode())
                                                        .detailOptionTitle(eachCancelInfo.getDetailOptionTitle())
                                                        .supplierId(eachCancelInfo.getSupplierId())
                                                        .productAmount(eachCancelInfo.getProductAmount())
                                                        .productOptionAmount(eachCancelInfo.getProductOptionAmount())
                                                        .deliveryPrice(eachCancelInfo.getDeliveryPrice())
                                                        .dangerPlacePrice(eachCancelInfo.getDangerPlacePrice())
                                                        .cancelAmount(eachCancelInfo.getQuantity())
                                                        .totalPrice(eachCancelInfo.getProductTotalAmount())
                                                        .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                                                        .onNuryCanclePrice(eachCancelInfo.getOnnurypay())
                                                        .creditStatementNumber((String) cancelData.get("cancelPgCno"))
                                                        .creditCanclePrice(eachCancelInfo.getProductTotalAmount() - eachCancelInfo.getOnnurypay())
                                                        .cancelCheck("Y")
                                                        .cancelRequestAt(LocalDateTime.now())
                                                        .build()
                                        )
                                        .collect(Collectors.toList())
                                        .forEach(eachCancelOrder -> {
                                            CancleOrder alreadyExistCancelOrderData = jpaQueryFactory
                                                    .selectFrom(cancleOrder)
                                                    .where(cancleOrder.cancleOrderId.in(cancelRequestIdList)
                                                            .and(cancleOrder.orderNumber.eq(eachCancelOrder.getOrderNumber()))
                                                            .and(cancleOrder.seq.eq(eachCancelOrder.getSeq())))
                                                    .fetchOne();

                                            if(alreadyExistCancelOrderData != null){
                                                jpaQueryFactory
                                                        .update(cancleOrder)
                                                        .set(cancleOrder.cancelCheck, "Y")
                                                        .set(cancleOrder.cancelRequestAt, LocalDateTime.now())
                                                        .set(cancleOrder.cancelAmount, eachCancelOrder.getCancelAmount())
                                                        .set(cancleOrder.dangerPlacePrice, eachCancelOrder.getDangerPlacePrice())
                                                        .set(cancleOrder.deliveryPrice, eachCancelOrder.getDeliveryPrice())
                                                        .set(cancleOrder.creditCanclePrice, eachCancelOrder.getCreditCanclePrice())
                                                        .set(cancleOrder.creditStatementNumber, eachCancelOrder.getCreditStatementNumber())
                                                        .set(cancleOrder.onNuryCanclePrice, eachCancelOrder.getOnNuryCanclePrice())
                                                        .set(cancleOrder.onNuryStatementNumber, eachCancelOrder.getOnNuryStatementNumber())
                                                        .where(cancleOrder.cancleOrderId.eq(alreadyExistCancelOrderData.getCancleOrderId()))
                                                        .execute();
                                            }else{
                                                cancelInfoList.add(eachCancelOrder);
                                            }
                                        });

                                entityManager.flush();
                                entityManager.clear();

                                cancleOrderRepository.saveAll(cancelInfoList);
                            }else{

                                List<CancleOrder> cancelInfoList = odProductList.stream()
                                        .map(eachCancelInfo ->
                                                CancleOrder.builder()
                                                        .orderNumber(eachCancelInfo.getOrderNumber())
                                                        .seq(eachCancelInfo.getSeq())
                                                        .productName(eachCancelInfo.getProductName())
                                                        .productClassificationCode(eachCancelInfo.getProductClassificationCode())
                                                        .detailOptionTitle(eachCancelInfo.getDetailOptionTitle())
                                                        .supplierId(eachCancelInfo.getSupplierId())
                                                        .productAmount(eachCancelInfo.getProductAmount())
                                                        .productOptionAmount(eachCancelInfo.getProductOptionAmount())
                                                        .deliveryPrice(eachCancelInfo.getDeliveryPrice())
                                                        .dangerPlacePrice(eachCancelInfo.getDangerPlacePrice())
                                                        .cancelAmount(eachCancelInfo.getQuantity())
                                                        .totalPrice(eachCancelInfo.getProductTotalAmount())
                                                        .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                                                        .onNuryCanclePrice(eachCancelInfo.getOnnurypay())
                                                        .creditStatementNumber((String) cancelData.get("cancelPgCno"))
                                                        .creditCanclePrice(eachCancelInfo.getProductTotalAmount() - eachCancelInfo.getOnnurypay())
                                                        .cancelCheck("Y")
                                                        .cancelRequestAt(LocalDateTime.now())
                                                        .build()
                                        )
                                        .collect(Collectors.toList());

                                cancleOrderRepository.saveAll(cancelInfoList);
                            }

                            paymentQueryData.productCancleUpdate(orderNumber);

                        } else {
                            compountPayApprovalResult.put("code", cancelData.get("resCd"));
                            compountPayApprovalResult.put("data", cancelData.get("resMsg"));
                        }
                    } else {
                        compountPayApprovalResult.put("code", bizresultEV.get("code"));
                        compountPayApprovalResult.put("data", bizresultEV.get("msg"));
                    }
                } else {
                    compountPayApprovalResult.put("code", bizresult.get("RC"));
                    compountPayApprovalResult.put("data", bizresult.get("RM"));
                }
            } else {
                compountPayApprovalResult.put("code", "9999");
                compountPayApprovalResult.put("data", "온누리 시스템 에러");

            }

        } else if (onuryCanclePay == 0) {

            JSONObject requestdata = new JSONObject();
            requestdata.put("mallId", PGMID);
            requestdata.put("shopTransactionId", result.getOrderNumber() + "_cancel");
            requestdata.put("pgCno", result.getCreditStatementNumber());
            requestdata.put("reviseTypeCode", "40");
            requestdata.put("remainAmount", result.getCreditApprovalPrice());
            requestdata.put("amount", pgCanclePay);
            requestdata.put("clientIp", "211.253.30.157");
            requestdata.put("clientId", "onnury");
            String encMsg = result.getCreditStatementNumber() + "|" + result.getOrderNumber() + "_cancel";
            requestdata.put("msgAuthValue", easyPayCodeccService.easypayDeccode(encMsg));
            requestdata.put("cancelReqDate", merchantCancelDt);


            URL easyPayUrl = new URL(PGURL + "revise"); // 호출할 외부 API 를 입력한다.
            HttpURLConnection easyPayConn = (HttpURLConnection) easyPayUrl.openConnection(); // header에 데이터 통신 방법을 지정한다.
            easyPayConn.setRequestMethod("POST");
            easyPayConn.setRequestProperty("Content-Type", "application/json; utf-8");

            // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
            easyPayConn.setDoOutput(true);

            // Request body message에 전송
            OutputStreamWriter os = new OutputStreamWriter(easyPayConn.getOutputStream());
            os.write(requestdata.toString());
            os.flush();

            // 응답
            BufferedReader in = new BufferedReader(new InputStreamReader(easyPayConn.getInputStream(), "UTF-8"));
            JSONObject cancelData = (JSONObject) JSONValue.parse(in.readLine());
            in.close();
            easyPayConn.disconnect();

            if (cancelData.get("resCd").equals("0000")) {

                if (!cancelRequestIdList.isEmpty()) {

                    List<CancleOrder> cancelInfoList = new ArrayList<>();

                    odProductList.stream()
                            .map(eachCancelInfo ->
                                    CancleOrder.builder()
                                            .orderNumber(eachCancelInfo.getOrderNumber())
                                            .seq(eachCancelInfo.getSeq())
                                            .productName(eachCancelInfo.getProductName())
                                            .productClassificationCode(eachCancelInfo.getProductClassificationCode())
                                            .detailOptionTitle(eachCancelInfo.getDetailOptionTitle())
                                            .supplierId(eachCancelInfo.getSupplierId())
                                            .productAmount(eachCancelInfo.getProductAmount())
                                            .productOptionAmount(eachCancelInfo.getProductOptionAmount())
                                            .deliveryPrice(eachCancelInfo.getDeliveryPrice())
                                            .dangerPlacePrice(eachCancelInfo.getDangerPlacePrice())
                                            .cancelAmount(eachCancelInfo.getQuantity())
                                            .totalPrice(eachCancelInfo.getProductTotalAmount())
                                            .onNuryStatementNumber("")
                                            .onNuryCanclePrice(eachCancelInfo.getOnnurypay())
                                            .creditStatementNumber((String) cancelData.get("cancelPgCno"))
                                            .creditCanclePrice(eachCancelInfo.getProductTotalAmount() - eachCancelInfo.getOnnurypay())
                                            .cancelCheck("Y")
                                            .cancelRequestAt(LocalDateTime.now())
                                            .build()
                            )
                            .collect(Collectors.toList())
                            .forEach(eachCancelOrder -> {
                                CancleOrder alreadyExistCancelOrderData = jpaQueryFactory
                                        .selectFrom(cancleOrder)
                                        .where(cancleOrder.cancleOrderId.in(cancelRequestIdList)
                                                .and(cancleOrder.orderNumber.eq(eachCancelOrder.getOrderNumber()))
                                                .and(cancleOrder.seq.eq(eachCancelOrder.getSeq())))
                                        .fetchOne();

                                if(alreadyExistCancelOrderData != null){
                                    jpaQueryFactory
                                            .update(cancleOrder)
                                            .set(cancleOrder.cancelCheck, "Y")
                                            .set(cancleOrder.cancelRequestAt, LocalDateTime.now())
                                            .set(cancleOrder.cancelAmount, eachCancelOrder.getCancelAmount())
                                            .set(cancleOrder.dangerPlacePrice, eachCancelOrder.getDangerPlacePrice())
                                            .set(cancleOrder.deliveryPrice, eachCancelOrder.getDeliveryPrice())
                                            .set(cancleOrder.creditCanclePrice, eachCancelOrder.getCreditCanclePrice())
                                            .set(cancleOrder.creditStatementNumber, eachCancelOrder.getCreditStatementNumber())
                                            .set(cancleOrder.onNuryCanclePrice, eachCancelOrder.getOnNuryCanclePrice())
                                            .set(cancleOrder.onNuryStatementNumber, eachCancelOrder.getOnNuryStatementNumber())
                                            .where(cancleOrder.cancleOrderId.eq(alreadyExistCancelOrderData.getCancleOrderId()))
                                            .execute();
                                }else{
                                    cancelInfoList.add(eachCancelOrder);
                                }
                            });

                    entityManager.flush();
                    entityManager.clear();

                    cancleOrderRepository.saveAll(cancelInfoList);
                }else{

                    List<CancleOrder> cancelInfoList = odProductList.stream()
                            .map(eachCancelInfo ->
                                    CancleOrder.builder()
                                            .orderNumber(eachCancelInfo.getOrderNumber())
                                            .seq(eachCancelInfo.getSeq())
                                            .productName(eachCancelInfo.getProductName())
                                            .productClassificationCode(eachCancelInfo.getProductClassificationCode())
                                            .detailOptionTitle(eachCancelInfo.getDetailOptionTitle())
                                            .supplierId(eachCancelInfo.getSupplierId())
                                            .productAmount(eachCancelInfo.getProductAmount())
                                            .productOptionAmount(eachCancelInfo.getProductOptionAmount())
                                            .deliveryPrice(eachCancelInfo.getDeliveryPrice())
                                            .dangerPlacePrice(eachCancelInfo.getDangerPlacePrice())
                                            .cancelAmount(eachCancelInfo.getQuantity())
                                            .totalPrice(eachCancelInfo.getProductTotalAmount())
                                            .onNuryStatementNumber("")
                                            .onNuryCanclePrice(eachCancelInfo.getOnnurypay())
                                            .creditStatementNumber((String) cancelData.get("cancelPgCno"))
                                            .creditCanclePrice(eachCancelInfo.getProductTotalAmount() - eachCancelInfo.getOnnurypay())
                                            .cancelCheck("Y")
                                            .cancelRequestAt(LocalDateTime.now())
                                            .build()
                            )
                            .collect(Collectors.toList());

                    cancleOrderRepository.saveAll(cancelInfoList);
                }

                paymentQueryData.productCancleUpdate(orderNumber);
            } else {
                compountPayApprovalResult.put("code", cancelData.get("resCd"));
                compountPayApprovalResult.put("data", cancelData.get("resMsg"));
            }
        } else if (pgCanclePay == 0) {
            reqParams.put("merchantCancelDt", merchantCancelDt);
            reqParams.put("merchantCancelID", merchantCancelID);
            reqParams.put("merchantOrderDt", merchantOrderDt);
            reqParams.put("merchantOrderID", result.getOrderNumber());
            reqParams.put("tid", result.getOnNuryStatementNumber());
            reqParams.put("totalAmount", result.getOnNuryApprovalPrice());
            reqParams.put("totalCancelAmount", onuryCanclePay);
            reqParams.put("cancelVatAmount", 0);
            reqParams.put("cancelTaxFreeAmount", 0);
            reqParams.put("partYn", "N");

            String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams.toString());
            String reqVV = bizPointCodeccService.getHmacSha256(reqParams.toString());

            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
            Date d = new Date();
            String date = sf.format(d);


            JSONObject postBody = new JSONObject();
            postBody.put("MID", _MID_);
            postBody.put("RQ_DTIME", date);
            postBody.put("TNO", result.getOrderNumber() + "_cancel");
            postBody.put("EV", reqEV);
            postBody.put("VV", reqVV);
            postBody.put("RC", "");
            postBody.put("RM", "");

            String url = BZPURL + "api_v1_payment_cancel.jct "; //결제준비URL

            byte[] resMessage = null;
            HttpURLConnection conn;

            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", _AUTH_KEY_);
                conn.setUseCaches(false);

                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                os.write(String.valueOf(postBody));
                os.flush();
                os.close();

                DataInputStream in = new DataInputStream(conn.getInputStream());
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                int bcout = 0;
                byte[] buf = new byte[2048];
                while (true) {
                    int n = in.read(buf);
                    if (n == -1)
                        break;
                    bout.write(buf, 0, n);
                }
                bout.flush();
                resMessage = bout.toByteArray();
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (resMessage != null) {
                String temp = new String(resMessage, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> bizresult = mapper.readValue(temp, Map.class);
                if (bizresult.get("RC").equals("0000")) {
                    Map<String, Object> bizresultEV = mapper.readValue(bizPointCodeccService.biztotpayDecCode((String) bizresult.get("EV")), Map.class);
                    String redcode = (String) bizresultEV.get("code");
                    if (redcode.equals("0000")) {
                        // 디비 저장
                        Map<String, Object> bizresultEVJsonData = (Map<String, Object>) bizresultEV.get("data");

                        if (!cancelRequestIdList.isEmpty()) {

                            List<CancleOrder> cancelInfoList = new ArrayList<>();

                            odProductList.stream()
                                    .map(eachCancelInfo ->
                                            CancleOrder.builder()
                                                    .orderNumber(eachCancelInfo.getOrderNumber())
                                                    .seq(eachCancelInfo.getSeq())
                                                    .productName(eachCancelInfo.getProductName())
                                                    .productClassificationCode(eachCancelInfo.getProductClassificationCode())
                                                    .detailOptionTitle(eachCancelInfo.getDetailOptionTitle())
                                                    .supplierId(eachCancelInfo.getSupplierId())
                                                    .productAmount(eachCancelInfo.getProductAmount())
                                                    .productOptionAmount(eachCancelInfo.getProductOptionAmount())
                                                    .deliveryPrice(eachCancelInfo.getDeliveryPrice())
                                                    .dangerPlacePrice(eachCancelInfo.getDangerPlacePrice())
                                                    .cancelAmount(eachCancelInfo.getQuantity())
                                                    .totalPrice(eachCancelInfo.getProductTotalAmount())
                                                    .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                                                    .onNuryCanclePrice(eachCancelInfo.getOnnurypay())
                                                    .creditStatementNumber("")
                                                    .creditCanclePrice(eachCancelInfo.getProductTotalAmount() - eachCancelInfo.getOnnurypay())
                                                    .cancelCheck("Y")
                                                    .cancelRequestAt(LocalDateTime.now())
                                                    .build()
                                    )
                                    .collect(Collectors.toList())
                                    .forEach(eachCancelOrder -> {
                                        CancleOrder alreadyExistCancelOrderData = jpaQueryFactory
                                                .selectFrom(cancleOrder)
                                                .where(cancleOrder.cancleOrderId.in(cancelRequestIdList)
                                                        .and(cancleOrder.orderNumber.eq(eachCancelOrder.getOrderNumber()))
                                                        .and(cancleOrder.seq.eq(eachCancelOrder.getSeq())))
                                                .fetchOne();

                                        if(alreadyExistCancelOrderData != null){
                                            jpaQueryFactory
                                                    .update(cancleOrder)
                                                    .set(cancleOrder.cancelCheck, "Y")
                                                    .set(cancleOrder.cancelRequestAt, LocalDateTime.now())
                                                    .set(cancleOrder.cancelAmount, eachCancelOrder.getCancelAmount())
                                                    .set(cancleOrder.dangerPlacePrice, eachCancelOrder.getDangerPlacePrice())
                                                    .set(cancleOrder.deliveryPrice, eachCancelOrder.getDeliveryPrice())
                                                    .set(cancleOrder.creditCanclePrice, eachCancelOrder.getCreditCanclePrice())
                                                    .set(cancleOrder.creditStatementNumber, eachCancelOrder.getCreditStatementNumber())
                                                    .set(cancleOrder.onNuryCanclePrice, eachCancelOrder.getOnNuryCanclePrice())
                                                    .set(cancleOrder.onNuryStatementNumber, eachCancelOrder.getOnNuryStatementNumber())
                                                    .where(cancleOrder.cancleOrderId.eq(alreadyExistCancelOrderData.getCancleOrderId()))
                                                    .execute();
                                        }else{
                                            cancelInfoList.add(eachCancelOrder);
                                        }
                                    });

                            entityManager.flush();
                            entityManager.clear();

                            cancleOrderRepository.saveAll(cancelInfoList);
                        }else{

                            List<CancleOrder> cancelInfoList = odProductList.stream()
                                    .map(eachCancelInfo ->
                                            CancleOrder.builder()
                                                    .orderNumber(eachCancelInfo.getOrderNumber())
                                                    .seq(eachCancelInfo.getSeq())
                                                    .productName(eachCancelInfo.getProductName())
                                                    .productClassificationCode(eachCancelInfo.getProductClassificationCode())
                                                    .detailOptionTitle(eachCancelInfo.getDetailOptionTitle())
                                                    .supplierId(eachCancelInfo.getSupplierId())
                                                    .productAmount(eachCancelInfo.getProductAmount())
                                                    .productOptionAmount(eachCancelInfo.getProductOptionAmount())
                                                    .deliveryPrice(eachCancelInfo.getDeliveryPrice())
                                                    .dangerPlacePrice(eachCancelInfo.getDangerPlacePrice())
                                                    .cancelAmount(eachCancelInfo.getQuantity())
                                                    .totalPrice(eachCancelInfo.getProductTotalAmount())
                                                    .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                                                    .onNuryCanclePrice(eachCancelInfo.getOnnurypay())
                                                    .creditStatementNumber("")
                                                    .creditCanclePrice(eachCancelInfo.getProductTotalAmount() - eachCancelInfo.getOnnurypay())
                                                    .cancelCheck("Y")
                                                    .cancelRequestAt(LocalDateTime.now())
                                                    .build()
                                    )
                                    .collect(Collectors.toList());

                            cancleOrderRepository.saveAll(cancelInfoList);
                        }

                        paymentQueryData.productCancleUpdate(orderNumber);

                    } else {
                        compountPayApprovalResult.put("code", bizresultEV.get("code"));
                        compountPayApprovalResult.put("data", bizresultEV.get("msg"));
                    }
                } else {
                    compountPayApprovalResult.put("code", bizresult.get("RC"));
                    compountPayApprovalResult.put("data", bizresult.get("RM"));
                }
            } else {
                compountPayApprovalResult.put("code", "9999");
                compountPayApprovalResult.put("data", "온누리 시스템 에러");

            }
        }

        return compountPayApprovalResult;
    }

    // 결제 부분 취소
    @Transactional
    public JSONObject partCancel(HttpServletRequest request, PartCancleRequestDto partCancleRequestDto) throws Exception {

        JSONObject compountPayApprovalResult = new JSONObject();
        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        log.info("==========================================================");
        log.info("[주문 취소 동작]");
        log.info("주문 번호 : {}", partCancleRequestDto.getOrderNumber());
        log.info("온누리 취소 금액 : {}", partCancleRequestDto.getOnnuryCancelPay());
        log.info("신용 카드 취소 금액 : {}", partCancleRequestDto.getPgCancelPay());
        log.info("제품 시퀀스 : {}", partCancleRequestDto.getSeq());
        log.info("수량 : {}", partCancleRequestDto.getQuantity());
        log.info("이전 취소 이력 존재 id : {}", partCancleRequestDto.getCancelRequestId());
        log.info("==========================================================");

        Payment result = paymentQueryData.PaymentInfo(partCancleRequestDto.getOrderNumber());
        OrderInProduct odProduct = paymentQueryData.ProductPart(partCancleRequestDto.getOrderNumber(), partCancleRequestDto.getSeq());
        List<OrderInProduct> odProductList = paymentQueryData.ProductList(partCancleRequestDto.getOrderNumber());

        OrderInDeliveryAddPrice odDelivery = paymentQueryData.DeliveryAddPricePart(partCancleRequestDto.getOrderNumber(), partCancleRequestDto.getSeq());
        List<OrderInDeliveryAddPrice> odDeliveryList = paymentQueryData.DeliveryAddPriceList(partCancleRequestDto.getOrderNumber());

        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date d1 = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(d1);
//        // 9시간 더하기
//        calendar.add(Calendar.HOUR_OF_DAY, 9);
//        Date newDate = calendar.getTime();
        String merchantCancelDt = sf1.format(d1);
        String merchantCancelID = sf2.format(d1);

        String merchantOrderDt = result.getOrderedAt().toString().substring(0,10).replaceAll("-", "");
        System.out.println("=======================주문 날짜====================");
        System.out.println(merchantOrderDt);
        System.out.println("=======================취소 날짜====================");
        System.out.println(merchantCancelDt);
        System.out.println(merchantCancelID);
        JSONObject reqParams = new JSONObject();

        if (partCancleRequestDto.getOnnuryCancelPay() > 0 && partCancleRequestDto.getPgCancelPay() > 0 ) {
            ArrayList<JSONObject> onproductList = new ArrayList<>();
            for (int p = 0; p < odProductList.size(); p++) {
                JSONObject onproducyInfo = new JSONObject();
                if (partCancleRequestDto.getSeq().equals(odProductList.get(p).getSeq())) {
                    onproducyInfo.put("seq", odProductList.get(p).getSeq());
                    onproducyInfo.put("frc_cd", odProductList.get(p).getFrcNumber().replaceAll("-", ""));
                    onproducyInfo.put("biz_no", odProductList.get(p).getBusinessNumber().replaceAll("-", ""));
                    onproducyInfo.put("amount", odProductList.get(p).getOnnurypay());
                    onproducyInfo.put("cancelAmount", partCancleRequestDto.getOnnuryCancelPay());
                } else {
                    onproducyInfo.put("seq", odProductList.get(p).getSeq());
                    onproducyInfo.put("frc_cd", odProductList.get(p).getFrcNumber().replaceAll("-", ""));
                    onproducyInfo.put("biz_no", odProductList.get(p).getBusinessNumber().replaceAll("-", ""));
                    onproducyInfo.put("amount", odProductList.get(p).getOnnurypay());
                    onproducyInfo.put("cancelAmount", 0);
                }
                onproductList.add(onproducyInfo);
            }
            for (int p = 0; p < odDeliveryList.size(); p++) {
                JSONObject onderiInfo = new JSONObject();
                if(odDeliveryList.get(p).getOnnuryPay() > 0){
                    if (partCancleRequestDto.getSeq().equals(odDeliveryList.get(p).getSeq())) {
                        onderiInfo.put("seq", odDeliveryList.get(p).getSeq());
                        onderiInfo.put("frc_cd", odDeliveryList.get(p).getFrcNumber());
                        onderiInfo.put("biz_no", odDeliveryList.get(p).getBusinessNumber());
                        onderiInfo.put("amount", odDeliveryList.get(p).getOnnuryPay());
                        onderiInfo.put("cancelAmount", partCancleRequestDto.getOnnuryCancelPay());
                    } else {
                        onderiInfo.put("seq", odDeliveryList.get(p).getSeq());
                        onderiInfo.put("frc_cd", odDeliveryList.get(p).getFrcNumber());
                        onderiInfo.put("biz_no", odDeliveryList.get(p).getBusinessNumber());
                        onderiInfo.put("amount", odDeliveryList.get(p).getOnnuryPay());
                        onderiInfo.put("cancelAmount", 0);
                    }
                onproductList.add(onderiInfo);
                }
            }

            reqParams.put("merchantCancelDt", merchantCancelDt);
            reqParams.put("merchantCancelID", merchantCancelID);
            reqParams.put("merchantOrderDt", merchantOrderDt);
            reqParams.put("merchantOrderID", result.getOrderNumber());
            reqParams.put("tid", result.getOnNuryStatementNumber());
            reqParams.put("totalAmount", result.getOnNuryApprovalPrice());
            reqParams.put("totalCancelAmount", partCancleRequestDto.getOnnuryCancelPay());
            reqParams.put("cancelVatAmount", 0);
            reqParams.put("cancelTaxFreeAmount", 0);
            reqParams.put("partYn", "Y");
            reqParams.put("productItems", onproductList);
            System.out.println(reqParams);
            String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams.toString());
            String reqVV = bizPointCodeccService.getHmacSha256(reqParams.toString());

            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
            Date d = new Date();
            String date = sf.format(d);


            JSONObject postBody = new JSONObject();
            postBody.put("MID", _MID_);
            postBody.put("RQ_DTIME", date);
            postBody.put("TNO", result.getOrderNumber() + "_cancel");
            postBody.put("EV", reqEV);
            postBody.put("VV", reqVV);
            postBody.put("RC", "");
            postBody.put("RM", "");
            String url = BZPURL + "api_v1_payment_cancel.jct "; //결제준비URL

            byte[] resMessage = null;
            HttpURLConnection conn;

            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", _AUTH_KEY_);
                conn.setUseCaches(false);

                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                os.write(String.valueOf(postBody));
                os.flush();
                os.close();

                DataInputStream in = new DataInputStream(conn.getInputStream());
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                int bcout = 0;
                byte[] buf = new byte[2048];
                while (true) {
                    int n = in.read(buf);
                    if (n == -1)
                        break;
                    bout.write(buf, 0, n);
                }
                bout.flush();
                resMessage = bout.toByteArray();
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (resMessage != null) {
                String temp = new String(resMessage, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> bizresult = mapper.readValue(temp, Map.class);

                if (bizresult.get("RC").equals("0000")) {
                    Map<String, Object> bizresultEV = mapper.readValue(bizPointCodeccService.biztotpayDecCode((String) bizresult.get("EV")), Map.class);
                    String redcode = (String) bizresultEV.get("code");

                    if (redcode.equals("0000")) {
                        // 디비 저장
                        Map<String, Object> bizresultEVJsonData = (Map<String, Object>) bizresultEV.get("data");
                        int canclepgetc = 0;
                        for(int cc = 0; cc < odProductList.size(); cc++){
                            if(odProductList.get(cc).getCancelAmount() > 0){
                                canclepgetc += odProductList.get(cc).getProductTotalAmount() - odProductList.get(cc).getOnnurypay();
                            }
                        }
                        for(int dd = 0; dd < odDeliveryList.size(); dd++){
                            if(odDeliveryList.get(dd).getCancleStatus().equals("Y")){
                                canclepgetc += odDeliveryList.get(dd).getAmount();
                            }
                        }
                        JSONObject requestdata = new JSONObject();
                        requestdata.put("mallId", PGMID);
                        requestdata.put("shopTransactionId", result.getOrderNumber() + date + "_cancel");
                        requestdata.put("pgCno", result.getCreditStatementNumber());
                        requestdata.put("reviseTypeCode", "32");
                        requestdata.put("remainAmount", result.getCreditApprovalPrice() - canclepgetc);
                        requestdata.put("amount", partCancleRequestDto.getPgCancelPay());
                        requestdata.put("clientIp", "211.253.30.157");
                        requestdata.put("clientId", "onnury");
                        String encMsg = result.getCreditStatementNumber() + "|" + result.getOrderNumber()  + date + "_cancel";
                        requestdata.put("msgAuthValue", easyPayCodeccService.easypayDeccode(encMsg));
                        requestdata.put("cancelReqDate", merchantCancelDt);

                        URL easyPayUrl = new URL(PGURL + "revise"); // 호출할 외부 API 를 입력한다.

                        HttpURLConnection easyPayConn = (HttpURLConnection) easyPayUrl.openConnection(); // header에 데이터 통신 방법을 지정한다.
                        easyPayConn.setRequestMethod("POST");
                        easyPayConn.setRequestProperty("Content-Type", "application/json; utf-8");

                        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
                        easyPayConn.setDoOutput(true);

                        // Request body message에 전송
                        OutputStreamWriter os = new OutputStreamWriter(easyPayConn.getOutputStream());
                        os.write(requestdata.toString());
                        os.flush();

                        // 응답
                        BufferedReader in = new BufferedReader(new InputStreamReader(easyPayConn.getInputStream(), "UTF-8"));
                        JSONObject cancelData = (JSONObject) JSONValue.parse(in.readLine());
                        in.close();
                        easyPayConn.disconnect();
                        System.out.println(cancelData);
                        if (cancelData.get("resCd").equals("0000")) {

                            if(odProduct != null){

                            CancleOrder co = CancleOrder.builder()
                                    .orderNumber(odProduct.getOrderNumber())
                                    .seq(odProduct.getSeq())
                                    .productName(odProduct.getProductName())
                                    .productClassificationCode(odProduct.getProductClassificationCode())
                                    .detailOptionTitle(odProduct.getDetailOptionTitle())
                                    .supplierId(odProduct.getSupplierId())
                                    .productAmount(odProduct.getProductAmount())
                                    .productOptionAmount(odProduct.getProductOptionAmount())
                                    .deliveryPrice(odProduct.getDeliveryPrice())
                                    .dangerPlacePrice(odProduct.getDangerPlacePrice())
                                    .cancelAmount(partCancleRequestDto.getQuantity()) // 취소 수량
                                    .totalPrice(odProduct.getProductTotalAmount())
                                    .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                                    .onNuryCanclePrice(partCancleRequestDto.getOnnuryCancelPay())
                                    .creditStatementNumber((String) cancelData.get("cancelPgCno"))
                                    .creditCanclePrice(partCancleRequestDto.getPgCancelPay())
                                    .cancelCheck("Y")
                                    .cancelRequestAt(LocalDateTime.now())
                                    .build();

                                cancleOrderRepository.save(co);
                                paymentQueryData.productCanclePartUpdate(partCancleRequestDto.getOrderNumber(), partCancleRequestDto.getSeq(), partCancleRequestDto.getQuantity());
                            } else {
                                jpaQueryFactory
                                        .update(orderInDeliveryAddPrice)
                                        .set(orderInDeliveryAddPrice.cancleStatus, "Y")
                                        .where(orderInDeliveryAddPrice.orderNumber.eq(odDelivery.getOrderNumber()).and(orderInDeliveryAddPrice.seq.eq(odDelivery.getSeq())))
                                        .execute();
                                entityManager.flush();
                                entityManager.clear();
                            }
                        } else {
                            compountPayApprovalResult.put("code", cancelData.get("resCd"));
                            compountPayApprovalResult.put("data", cancelData.get("resMsg"));
                        }
                    } else {
                        compountPayApprovalResult.put("code", bizresultEV.get("code"));
                        compountPayApprovalResult.put("data", bizresultEV.get("msg"));
                    }
                } else {
                    compountPayApprovalResult.put("code", bizresult.get("RC"));
                    compountPayApprovalResult.put("data", bizresult.get("RM"));
                }
            } else {
                compountPayApprovalResult.put("code", "9999");
                compountPayApprovalResult.put("data", "온누리 시스템 에러");

            }

        } else if (partCancleRequestDto.getOnnuryCancelPay() == 0 && partCancleRequestDto.getPgCancelPay() > 0) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
            Date d = new Date();
            String date = sf.format(d);
            JSONObject requestdata = new JSONObject();
            int canclepgetc = 0;
            for(int cc = 0; cc < odProductList.size(); cc++){
                if(odProductList.get(cc).getCancelAmount() > 0){
                    canclepgetc += odProductList.get(cc).getProductTotalAmount() - odProductList.get(cc).getOnnurypay();
                }
            }
            for(int dd = 0; dd < odDeliveryList.size(); dd++){
                if(odDeliveryList.get(dd).getCancleStatus().equals("Y")){
                    canclepgetc += odDeliveryList.get(dd).getAmount();
                }
            }
            requestdata.put("mallId", PGMID);
            requestdata.put("shopTransactionId", result.getOrderNumber() + date + "_cancel");
            requestdata.put("pgCno", result.getCreditStatementNumber());
            requestdata.put("reviseTypeCode", "32");
            requestdata.put("remainAmount", result.getCreditApprovalPrice() - canclepgetc);
            requestdata.put("amount", partCancleRequestDto.getPgCancelPay());
            requestdata.put("clientIp", "211.253.30.157");
            requestdata.put("clientId", "onnury");
            String encMsg = result.getCreditStatementNumber() + "|" + result.getOrderNumber()  + date + "_cancel";
            requestdata.put("msgAuthValue", easyPayCodeccService.easypayDeccode(encMsg));
            requestdata.put("cancelReqDate", merchantCancelDt);

            URL easyPayUrl = new URL(PGURL + "revise"); // 호출할 외부 API 를 입력한다.

            HttpURLConnection easyPayConn = (HttpURLConnection) easyPayUrl.openConnection(); // header에 데이터 통신 방법을 지정한다.
            easyPayConn.setRequestMethod("POST");
            easyPayConn.setRequestProperty("Content-Type", "application/json; utf-8");

            // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
            easyPayConn.setDoOutput(true);

            // Request body message에 전송
            OutputStreamWriter os = new OutputStreamWriter(easyPayConn.getOutputStream());
            os.write(requestdata.toString());
            os.flush();

            // 응답
            BufferedReader in = new BufferedReader(new InputStreamReader(easyPayConn.getInputStream(), "UTF-8"));
            JSONObject cancelData = (JSONObject) JSONValue.parse(in.readLine());
            in.close();
            easyPayConn.disconnect();

            log.info("", cancelData.get("resCd"));
            System.out.println(cancelData);
            if (cancelData.get("resCd").equals("0000")) {

                if(odProduct != null){
                    CancleOrder co = CancleOrder.builder()
                            .orderNumber(odProduct.getOrderNumber())
                            .seq(odProduct.getSeq())
                            .productName(odProduct.getProductName())
                            .productClassificationCode(odProduct.getProductClassificationCode())
                            .detailOptionTitle(odProduct.getDetailOptionTitle())
                            .supplierId(odProduct.getSupplierId())
                            .productAmount(odProduct.getProductAmount())
                            .productOptionAmount(odProduct.getProductOptionAmount())
                            .deliveryPrice(odProduct.getDeliveryPrice())
                            .dangerPlacePrice(odProduct.getDangerPlacePrice())
                            .cancelAmount(partCancleRequestDto.getQuantity())
                            .totalPrice(odProduct.getProductTotalAmount())
                            .onNuryStatementNumber("")
                            .onNuryCanclePrice(partCancleRequestDto.getOnnuryCancelPay())
                            .creditStatementNumber((String) cancelData.get("cancelPgCno"))
                            .creditCanclePrice(partCancleRequestDto.getPgCancelPay())
                            .cancelCheck("Y")
                            .cancelRequestAt(LocalDateTime.now())
                            .build();
                    cancleOrderRepository.save(co);
                    paymentQueryData.productCanclePartUpdate(partCancleRequestDto.getOrderNumber(), partCancleRequestDto.getSeq(), partCancleRequestDto.getQuantity());
                } else {
                    jpaQueryFactory
                            .update(orderInDeliveryAddPrice)
                            .set(orderInDeliveryAddPrice.cancleStatus, "Y")
                            .where(orderInDeliveryAddPrice.orderNumber.eq(odDelivery.getOrderNumber()).and(orderInDeliveryAddPrice.seq.eq(odDelivery.getSeq())))
                            .execute();
                    entityManager.flush();
                    entityManager.clear();
                }
            } else {
                compountPayApprovalResult.put("code", cancelData.get("resCd"));
                compountPayApprovalResult.put("data", cancelData.get("resMsg"));
            }
        } else if (partCancleRequestDto.getPgCancelPay() == 0 && partCancleRequestDto.getOnnuryCancelPay() > 0) {

            log.info("온누리 단일 결제 이력 취소 로직 진입");

            ArrayList<JSONObject> onproductList = new ArrayList<>();

            for (int p = 0; p < odProductList.size(); p++) {
                JSONObject onproducyInfo = new JSONObject();
                if (partCancleRequestDto.getSeq().equals(odProductList.get(p).getSeq())) {
                    onproducyInfo.put("seq", odProductList.get(p).getSeq());
                    onproducyInfo.put("frc_cd", odProductList.get(p).getFrcNumber());
                    onproducyInfo.put("biz_no", odProductList.get(p).getBusinessNumber());
                    onproducyInfo.put("amount", odProductList.get(p).getOnnurypay());
                    onproducyInfo.put("cancelAmount", partCancleRequestDto.getOnnuryCancelPay());
                } else {
                    onproducyInfo.put("seq", odProductList.get(p).getSeq());
                    onproducyInfo.put("frc_cd", odProductList.get(p).getFrcNumber());
                    onproducyInfo.put("biz_no", odProductList.get(p).getBusinessNumber());
                    onproducyInfo.put("amount", odProductList.get(p).getOnnurypay());
                    onproducyInfo.put("cancelAmount", 0);
                }
                onproductList.add(onproducyInfo);
            }

            reqParams.put("merchantCancelDt", merchantCancelDt);
            reqParams.put("merchantCancelID", merchantCancelID);
            reqParams.put("merchantOrderDt", merchantOrderDt);
            reqParams.put("merchantOrderID", result.getOrderNumber());
            reqParams.put("tid", result.getOnNuryStatementNumber());
            reqParams.put("totalAmount", result.getOnNuryApprovalPrice());
            reqParams.put("totalCancelAmount", partCancleRequestDto.getOnnuryCancelPay());
            reqParams.put("cancelVatAmount", 0);
            reqParams.put("cancelTaxFreeAmount", 0);
            reqParams.put("partYn", "Y");
            reqParams.put("productItems", onproductList);

            String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams.toString());
            String reqVV = bizPointCodeccService.getHmacSha256(reqParams.toString());

            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
            Date d = new Date();
            String date = sf.format(d);

            log.info("비즈페이 인코딩 데이터 : {}", reqEV);
            log.info("SHA256 인코딩 데이터 : {}", reqVV);

            JSONObject postBody = new JSONObject();
            postBody.put("MID", _MID_);
            postBody.put("RQ_DTIME", date);
            postBody.put("TNO", result.getOrderNumber() + "_cancel");
            postBody.put("EV", reqEV);
            postBody.put("VV", reqVV);
            postBody.put("RC", "");
            postBody.put("RM", "");

            String url = BZPURL + "api_v1_payment_cancel.jct "; //결제준비URL

            byte[] resMessage = null;
            HttpURLConnection conn;

            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", _AUTH_KEY_);
                conn.setUseCaches(false);

                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                os.write(String.valueOf(postBody));
                os.flush();
                os.close();

                DataInputStream in = new DataInputStream(conn.getInputStream());
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                int bcout = 0;
                byte[] buf = new byte[2048];
                while (true) {
                    int n = in.read(buf);
                    if (n == -1)
                        break;
                    bout.write(buf, 0, n);
                }
                bout.flush();
                resMessage = bout.toByteArray();
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (resMessage != null) {
                String temp = new String(resMessage, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> bizresult = mapper.readValue(temp, Map.class);

                log.info("비즈 페이먼츠 취소 상태 : {}", bizresult.get("RC"));

                if (bizresult.get("RC").equals("0000")) {

                    log.info("1단계 정상");

                    Map<String, Object> bizresultEV = mapper.readValue(bizPointCodeccService.biztotpayDecCode((String) bizresult.get("EV")), Map.class);
                    String redcode = (String) bizresultEV.get("code");
                    System.out.println(bizresultEV);
                    if (redcode.equals("0000")) {

                        log.info("2단계 정상");

                        // 디비 저장
                        Map<String, Object> bizresultEVJsonData = (Map<String, Object>) bizresultEV.get("data");

                        if(odProduct != null){

                            log.info("정상 취소 이력 저장 준비");

                            CancleOrder co = CancleOrder.builder()
                                    .orderNumber(odProduct.getOrderNumber())
                                    .seq(odProduct.getSeq())
                                    .productName(odProduct.getProductName())
                                    .productClassificationCode(odProduct.getProductClassificationCode())
                                    .detailOptionTitle(odProduct.getDetailOptionTitle())
                                    .supplierId(odProduct.getSupplierId())
                                    .productAmount(odProduct.getProductAmount())
                                    .productOptionAmount(odProduct.getProductOptionAmount())
                                    .deliveryPrice(odProduct.getDeliveryPrice())
                                    .dangerPlacePrice(odProduct.getDangerPlacePrice())
                                    .cancelAmount(partCancleRequestDto.getQuantity())
                                    .totalPrice(odProduct.getProductTotalAmount())
                                    .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                                    .onNuryCanclePrice(partCancleRequestDto.getOnnuryCancelPay())
                                    .creditStatementNumber("")
                                    .creditCanclePrice(partCancleRequestDto.getPgCancelPay())
                                    .cancelCheck("Y")
                                    .cancelRequestAt(LocalDateTime.now())
                                    .cancelAt(LocalDateTime.now())
                                    .build();
                            cancleOrderRepository.save(co);
                            paymentQueryData.productCanclePartUpdate(partCancleRequestDto.getOrderNumber(), partCancleRequestDto.getSeq(), partCancleRequestDto.getQuantity());
                        } else {

                            log.info("이전 이력 취소로 업데이트");

                            jpaQueryFactory
                                    .update(orderInDeliveryAddPrice)
                                    .set(orderInDeliveryAddPrice.cancleStatus, "Y")
                                    .where(orderInDeliveryAddPrice.orderNumber.eq(odDelivery.getOrderNumber()).and(orderInDeliveryAddPrice.seq.eq(odDelivery.getSeq())))
                                    .execute();
                            entityManager.flush();
                            entityManager.clear();
                        }

                    } else {

                        log.info("2단계 비정상");

                        compountPayApprovalResult.put("code", bizresultEV.get("code"));
                        compountPayApprovalResult.put("data", bizresultEV.get("msg"));
                    }
                } else {
                    log.info("1단계 비정상");
                    log.info("실패 : {}", bizresult.get("RC"));

                    compountPayApprovalResult.put("code", bizresult.get("RC"));
                    compountPayApprovalResult.put("data", bizresult.get("RM"));
                }
            } else {
                log.info("시스템 에러");

                compountPayApprovalResult.put("code", "9999");
                compountPayApprovalResult.put("data", "온누리 시스템 에러");

            }

        } if(partCancleRequestDto.getOnnuryCancelPay() == 0 && partCancleRequestDto.getPgCancelPay() == 0){

            if(odProduct != null){
                CancleOrder co = CancleOrder.builder()
                        .orderNumber(odProduct.getOrderNumber())
                        .seq(odProduct.getSeq())
                        .productName(odProduct.getProductName())
                        .productClassificationCode(odProduct.getProductClassificationCode())
                        .detailOptionTitle(odProduct.getDetailOptionTitle())
                        .supplierId(odProduct.getSupplierId())
                        .productAmount(odProduct.getProductAmount())
                        .productOptionAmount(odProduct.getProductOptionAmount())
                        .deliveryPrice(odProduct.getDeliveryPrice())
                        .dangerPlacePrice(odProduct.getDangerPlacePrice())
                        .cancelAmount(partCancleRequestDto.getQuantity()) // 취소 수량
                        .totalPrice(odProduct.getProductTotalAmount())
                        .onNuryStatementNumber(null)
                        .onNuryCanclePrice(partCancleRequestDto.getOnnuryCancelPay())
                        .creditStatementNumber(null)
                        .creditCanclePrice(partCancleRequestDto.getPgCancelPay())
                        .cancelCheck("Y")
                        .cancelRequestAt(LocalDateTime.now())
                        .build();

                if(partCancleRequestDto.getCancelRequestId() != 0L){

                    jpaQueryFactory
                            .update(cancleOrder)
                            .set(cancleOrder.productAmount, co.getProductAmount())
                            .set(cancleOrder.productOptionAmount, co.getProductOptionAmount())
                            .set(cancleOrder.deliveryPrice, co.getDeliveryPrice())
                            .set(cancleOrder.dangerPlacePrice, co.getDangerPlacePrice())
                            .set(cancleOrder.cancelAmount, co.getCancelAmount())
                            .set(cancleOrder.totalPrice, co.getTotalPrice())
                            .set(cancleOrder.onNuryStatementNumber, co.getOnNuryStatementNumber())
                            .set(cancleOrder.onNuryCanclePrice, co.getOnNuryCanclePrice())
                            .set(cancleOrder.creditStatementNumber, co.getCreditStatementNumber())
                            .set(cancleOrder.creditCanclePrice, co.getCreditCanclePrice())
                            .set(cancleOrder.cancelCheck, co.getCancelCheck())
                            .set(cancleOrder.cancelRequestAt, co.getCancelRequestAt())
                            .where(cancleOrder.cancleOrderId.eq(partCancleRequestDto.getCancelRequestId()))
                            .execute();

                    entityManager.flush();
                    entityManager.clear();
                }else{
                    cancleOrderRepository.save(co);
                }

                paymentQueryData.productCanclePartUpdate(partCancleRequestDto.getOrderNumber(), partCancleRequestDto.getSeq(), partCancleRequestDto.getQuantity());
            }else {
                jpaQueryFactory
                        .update(orderInDeliveryAddPrice)
                        .set(orderInDeliveryAddPrice.cancleStatus, "Y")
                        .where(orderInDeliveryAddPrice.orderNumber.eq(odDelivery.getOrderNumber()).and(orderInDeliveryAddPrice.seq.eq(odDelivery.getSeq())))
                        .execute();
                entityManager.flush();
                entityManager.clear();
            }

        }

        return compountPayApprovalResult;
    }
}
