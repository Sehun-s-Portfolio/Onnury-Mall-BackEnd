package com.onnury.payment.service;

import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.member.domain.Member;
import com.onnury.payment.domain.*;
import com.onnury.payment.repository.*;
import com.onnury.payment.request.*;
import com.onnury.product.domain.Product;
import com.onnury.query.payment.PaymentQueryData;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.BufferedReader;
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
import static com.onnury.supplier.domain.QSupplier.supplier;
import static com.onnury.product.domain.QProduct.product;

@Slf4j
@RequiredArgsConstructor
@Service
public class EasyPayService {
    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderInProductRepository orderInProductRepository;
    private final OrderInDeliveryAddPriceRepository orderInDeliveryAddPriceRepository;
    private final PaymentRepository paymentRepository;
    private final EasyPaymentApprovalRepository easyPaymentApprovalRepository;
    private final EasyPaymentBasketInfoRepository easyPaymentBasketInfoRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JPAQueryFactory jpaQueryFactory;
    private final EasyPayCodeccService easyPayCodeccService;
    private final EntityManager entityManager;
    private final PaymentQueryData paymentQueryData;

    @Value("${easy.payment.mid}")
    private String PGMID; // 가맹점 ID

    @Value("${easy.payment.url}")
    private String PGURL;

    @Value("${easy.payment.approval.page.url}")
    private String approvalPageUrl;

    // EasyPay 결제 준비 service
    public JSONObject reserve(HttpServletRequest request, PaymentKiccRequestDto paymentKiccRequestDto) throws Exception {
        log.info("EasyPay 결제 준비 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        JSONObject requestdata = new JSONObject();

        requestdata.put("mallId", PGMID);
        requestdata.put("payMethodTypeCode", "11");
        requestdata.put("currency", "00");
        requestdata.put("amount", paymentKiccRequestDto.getPgtotalAmount());
        requestdata.put("clientTypeCode", "00");
        requestdata.put("returnUrl", approvalPageUrl);
        requestdata.put("shopOrderNo", paymentKiccRequestDto.getOrderNumber());
        requestdata.put("deviceTypeCode", paymentKiccRequestDto.getDeviceType());

        JSONObject requestdata2 = new JSONObject();
        requestdata2.put("goodsName", paymentKiccRequestDto.getProductName());
        JSONObject requestdata4 = new JSONObject();
        requestdata4.put("customerName", paymentKiccRequestDto.getMerchantUserNm());
        requestdata2.put("customerInfo", requestdata4);
        requestdata.put("orderInfo", requestdata2);

        String result_txt = "";

        URL url = new URL(PGURL + "webpay"); // 호출할 외부 API 를 입력한다.

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // header에 데이터 통신 방법을 지정한다.
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");

        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
        conn.setDoOutput(true);

        // Request body message에 전송
        OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
        os.write(requestdata.toString());
        os.flush();

        // 응답
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        // 세훈아 여기야
        JSONObject jsonObj = (JSONObject) JSONValue.parse(in.readLine());

        in.close();
        conn.disconnect();

        return jsonObj;
    }


    // EasyPay 거래 승인 service
    @Transactional
    public JSONObject approval(
            HttpServletRequest request, NewPaymentRequestDto newPaymentRequestDto, List<PaymentProductListRequestDto> PaymentProductListRequestDto) throws Exception {
        log.info("EasyPay 거래 승인 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date();
        String date = sf.format(d);

        EasyPaymentApprovalInfo getEasyPaymentApprovalInfo = (EasyPaymentApprovalInfo) redisTemplate.opsForValue().get("easy_" + newPaymentRequestDto.getOrderNumber());
        assert getEasyPaymentApprovalInfo != null;

        JSONObject requestdata = new JSONObject();
        requestdata.put("mallId", PGMID);
        requestdata.put("shopTransactionId", "approval_" + getEasyPaymentApprovalInfo.getShopOrderNo());
        requestdata.put("authorizationId", getEasyPaymentApprovalInfo.getAuthorizationId());
        requestdata.put("shopOrderNo", getEasyPaymentApprovalInfo.getShopOrderNo());
        requestdata.put("approvalReqDate", date);

        URL url = new URL(PGURL + "approval"); // 호출할 외부 API 를 입력한다.

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // header에 데이터 통신 방법을 지정한다.
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");

        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
        conn.setDoOutput(true);

        // Request body message에 전송
        OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
        os.write(requestdata.toString());
        os.flush();

        // 응답
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        JSONObject jsonObj = (JSONObject) JSONValue.parse(in.readLine());
        jsonObj.put("authorizationId", getEasyPaymentApprovalInfo.getAuthorizationId());

        in.close();
        conn.disconnect();

        log.info("EasyPay 최종 거래 승인 후 응답 데이터 확인");
        log.info("{}", jsonObj);

        String transactionDate = (String) jsonObj.get("transactionDate");
        String year = transactionDate.substring(0, 4);
        String month = transactionDate.substring(4, 6);
        String day = transactionDate.substring(6, 8);
        String hour = transactionDate.substring(8, 10);
        String minute = transactionDate.substring(10, 12);
        String second = transactionDate.substring(12);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        String convertDate = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        LocalDateTime dateTime = LocalDateTime.parse(convertDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 결제할 제품들 리스트로 변환
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

                if(supplierCommissionInfo.get(supplier.onnuryCommission) != null){
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
                        .orderNumber(newPaymentRequestDto.getOrderNumber())
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

            }else {

                int onnuryPay = Integer.parseInt(prodctReqList.get(p).getOnnurypay().replace(",", ""));
                int creditPay = prodctReqList.get(p).getProductTotalAmount() - onnuryPay;

                OrderInDeliveryAddPrice odInfo = OrderInDeliveryAddPrice.builder()
                        .orderNumber(newPaymentRequestDto.getOrderNumber())
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
//        List<OrderInProduct> productMapList = prodctReqList.stream()
//                .map(eachProduct -> {
//
//                    Tuple productEventInfo = jpaQueryFactory
//                            .select(product.eventDescription, product.eventStartDate, product.eventEndDate, product.supplierId)
//                            .from(product)
//                            .where(product.classificationCode.eq(eachProduct.getProductClassificationCode()))
//                            .fetchOne();
//
//                    Tuple supplierCommissionInfo = jpaQueryFactory
//                            .select(supplier.onnuryCommission, supplier.creditCommission)
//                            .from(supplier)
//                            .where(supplier.supplierId.eq(productEventInfo.get(product.supplierId)))
//                            .fetchOne();
//
//                    int onnuryPay = Integer.parseInt(eachProduct.getOnnurypay().replace(",", ""));
//                    int creditPay = eachProduct.getProductTotalAmount() - onnuryPay;
//
//                    int onnuryCommissionPrice = 0;
//
//                    if(supplierCommissionInfo.get(supplier.onnuryCommission) != null ){
//                        onnuryCommissionCheck.set(supplierCommissionInfo.get(supplier.onnuryCommission));
//
//                        if(onnuryCommissionCheck.get() > 0){
//                            onnuryCommissionPrice = (int)(onnuryPay * (onnuryCommissionCheck.get() / 100.0));
//                            String onnuryCommissionPriceCutLastIndexPrice = String.valueOf(onnuryCommissionPrice);
//
//                            if(onnuryCommissionPriceCutLastIndexPrice.length() != 1){
//                                String finalOnnuryCommissionPrice = onnuryCommissionPriceCutLastIndexPrice.substring(0, onnuryCommissionPriceCutLastIndexPrice.length() - 1) + "0";
//                                onnuryCommissionPrice = Integer.parseInt(finalOnnuryCommissionPrice);
//                            }else{
//                                onnuryCommissionPrice = 0;
//                            }
//                        }
//                    }
//
//                    int creditCommissionPrice = 0;
//
//                    if(supplierCommissionInfo.get(supplier.creditCommission) != null){
//                        creditCommissionCheck.set(supplierCommissionInfo.get(supplier.creditCommission));
//
//                        if(creditCommissionCheck.get() > 0){
//                            creditCommissionPrice = (int)(creditPay * (creditCommissionCheck.get() / 100.0));
//                            String creditCommissionPriceCutLastIndexPrice = String.valueOf(creditCommissionPrice);
//
//                            if(creditCommissionPriceCutLastIndexPrice.length() != 1) {
//                                String finalCreditCommissionPrice = creditCommissionPriceCutLastIndexPrice.substring(0, creditCommissionPriceCutLastIndexPrice.length() - 1) + "0";
//                                creditCommissionPrice = Integer.parseInt(finalCreditCommissionPrice);
//                            }else {
//                                creditCommissionPrice = 0;
//                            }
//                        }
//                    }
//
//                    return OrderInProduct.builder()
//                            .orderNumber(newPaymentRequestDto.getOrderNumber())
//                            .seq(eachProduct.getSeq())
//                            .productName(eachProduct.getProductName())
//                            .productClassificationCode(eachProduct.getProductClassificationCode())
//                            .detailOptionTitle(eachProduct.getDetailOptionTitle())
//                            .supplierId(eachProduct.getSupplierId())
//                            .productAmount(eachProduct.getProductAmount())
//                            .productOptionAmount(eachProduct.getProductOptionAmount())
//                            .quantity(eachProduct.getQuantity())
//                            .deliveryPrice(eachProduct.getDeliveryPrice())
//                            .dangerPlacePrice(eachProduct.getDangerPlacePrice())
//                            .onnurypay(onnuryPay)
//                            .productTotalAmount(eachProduct.getProductTotalAmount() + eachProduct.getDeliveryPrice() + eachProduct.getDangerPlacePrice())
//                            .memo(String.valueOf(eachProduct.getMemo()))
//                            .transportNumber("")
//                            .parcelName("")
//                            .completePurchaseCheck("N")
//                            .cancelAmount(0)
//                            .cartId(eachProduct.getCartId())
//                            .onnuryCommissionPrice(onnuryCommissionPrice)
//                            .creditCommissionPrice(creditCommissionPrice)
//                            .eventCheck(LocalDateTime.now().isAfter(productEventInfo.get(product.eventStartDate)) && LocalDateTime.now().isBefore(productEventInfo.get(product.eventEndDate)) ? "Y" : "N")
//                            .eventInfo(productEventInfo.get(product.eventDescription))
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        orderInProductRepository.saveAll(productMapList);

        Payment payment = Payment.builder()
                .orderNumber(newPaymentRequestDto.getOrderNumber())
                .buyMemberLoginId(newPaymentRequestDto.getBuyMemberLoginId())
                .receiver(newPaymentRequestDto.getReceiver())
                .postNumber(newPaymentRequestDto.getPostNumber())
                .address(newPaymentRequestDto.getAddress())
                .message(newPaymentRequestDto.getMessage())
                .receiverPhone(newPaymentRequestDto.getReceiverPhone())
                .linkCompany(newPaymentRequestDto.getLinkCompany())
                .onNuryStatementNumber("")
                .onNuryApprovalPrice(0)
                .creditApprovalPrice(((Long) jsonObj.get("amount")).intValue())
                .creditStatementNumber((String) jsonObj.get("pgCno"))
                .totalApprovalPrice(((Long) jsonObj.get("amount")).intValue())
                .orderedAt(LocalDateTime.now())
                .build();


        // EasyPay 단일 결제 이력 정보 저장
        paymentRepository.save(payment);

        // EasyPay 거래 승인 정보 데이터들을 추출하여 EasyPaymentApproval 에 알맞은 각 데이터로 다시 한번 추출
        JSONObject paymentInfoData = (JSONObject) jsonObj.get("paymentInfo"); // 총 결제 정보
        JSONObject cardInfoData = (JSONObject) paymentInfoData.get("cardInfo"); // 총 결제 정보 속 카드 정보
        JSONObject bankInfoData = (JSONObject) paymentInfoData.get("bankInfo"); // 총 결제 정보 속 은행 정보
        JSONObject virtualAccountInfoData = (JSONObject) paymentInfoData.get("virtualAccountInfo"); // 총 결제 정보 속 가상 은행 정보
        JSONObject mobInfoData = (JSONObject) paymentInfoData.get("mobInfo"); // 총 결제 정보 속 모바일 정보
        JSONObject prepaidInfoData = (JSONObject) paymentInfoData.get("prepaidInfo"); // 총 결제 정보 속 뱅크 월렛 정보
        JSONObject cashReceiptInfoData = (JSONObject) paymentInfoData.get("cashReceiptInfo"); // 총 결제 정보 속 현금 영수증 정보

        // EasyPay 전용 거래 승인 정보 기입
        EasyPaymentApproval easyPaymentApproval = EasyPaymentApproval.builder()
                .mallId((String) jsonObj.get("mallId"))
                .pgCno((String) jsonObj.get("pgCno"))
                .shopTransactionId((String) jsonObj.get("shopTransactionId"))
                .shopOrderNo((String) jsonObj.get("shopOrderNo"))
                .amount((Long) jsonObj.get("amount"))
                .transactionDate((String) jsonObj.get("transactionDate"))
                .statusCode((String) jsonObj.get("statusCode"))
                .statusMessage((String) jsonObj.get("statusMessage"))
                .msgAuthValue((String) jsonObj.get("msgAuthValue"))
                .escrowUsed((String) jsonObj.get("escrowUsed"))
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
        requestdata.put("resCd", jsonObj.get("resCd"));


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

        return requestdata;
    }


    // 결제 취소 service
    public ResponseEntity<ResponseBody> cancel(HttpServletRequest request, Map<String, Object> cancelInfoMap) throws Exception {
        log.info("EasyPay 결제 취소 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        String result_txt = "";
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date();
        String date = sf.format(d);

        JSONObject requestdata = new JSONObject();
        requestdata.put("mallId", PGMID);
        requestdata.put("shopTransactionId", cancelInfoMap.get("shopTransactionId"));
        requestdata.put("pgCno", cancelInfoMap.get("pgCno"));
        requestdata.put("reviseTypeCode", cancelInfoMap.get("reviseTypeCode"));
        requestdata.put("reviseSubTypeCode", cancelInfoMap.get("reviseSubTypeCode"));
        requestdata.put("reviseMessage", cancelInfoMap.get("reviseMessage"));
        requestdata.put("remainAmount", cancelInfoMap.get("remainAmount"));
        requestdata.put("amount", cancelInfoMap.get("amount"));
        requestdata.put("clientIp", "210.180.79.7");
        requestdata.put("clientId", "BIZP");
        String encMsg = cancelInfoMap.get("pgCno") + "|" + cancelInfoMap.get("shopTransactionId");
        requestdata.put("msgAuthValue", easyPayCodeccService.easypayDeccode(encMsg));
        requestdata.put("cancelReqDate", date);

        URL url = new URL(PGURL + "revise"); // 호출할 외부 API 를 입력한다.

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // header에 데이터 통신 방법을 지정한다.
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");

        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
        conn.setDoOutput(true);

        // Request body message에 전송
        OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
        os.write(requestdata.toString());
        os.flush();

        // 응답
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        JSONObject cancelData = (JSONObject) JSONValue.parse(in.readLine());

        in.close();
        conn.disconnect();
//
//        if(cancelData.get("resCd").equals("0000")){
        //  paymentQueryData.cancelEasyPaymentUpdate(authMember, cancelData);
//        }

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, result_txt), HttpStatus.OK);
    }


    // 결제 조회 service
    public ResponseEntity<ResponseBody> status(Map<String, Object> returnMap) throws Exception {

        String result_txt = "";
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date();
        String date = sf.format(d);

        JSONObject requestdata = new JSONObject();
//        requestdata.put("mallId", PGMID);
//        requestdata.put("shopTransactionId", returnMap.get("shopTransactionId"));
//        requestdata.put("authorizationId", returnMap.get("authorizationId"));
//        requestdata.put("shopOrderNo", returnMap.get("shopOrderNo"));
//        requestdata.put("transactionDate", date);
//
//        URL url = new URL(PGURL + "retrieveTransaction"); // 호출할 외부 API 를 입력한다.
//
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // header에 데이터 통신 방법을 지정한다.
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Content-Type", "application/json; utf-8");
//
//        // Post인 경우 데이터를 OutputStream으로 넘겨 주겠다는 설정
//        conn.setDoOutput(true);
//
//        // Request body message에 전송
//        OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
//        os.write(requestdata.toString());
//        os.flush();
//
//        // 응답
//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//        JSONObject jsonObj = (JSONObject) JSONValue.parse(in.readLine());
//
//        in.close();
//        conn.disconnect();


        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, requestdata), HttpStatus.OK);
    }


    // EASY PAY 결제 승인 정보 조회 (성공 / 실패 / 취소) service
    public EasyPaymentApprovalInfo getEasyPaymentApprovalInfo(HttpServletRequest request, String shopOrderNo) throws InterruptedException {
        log.info("EASY PAY 결제 승인 정보 조회 (성공 / 실패 / 취소) service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        LocalDateTime checkStartDateTime = LocalDateTime.now();

        if (!getApprovalInfoUntilTheEnd(shopOrderNo, checkStartDateTime).getAuthorizationId().isEmpty()) {
            return (EasyPaymentApprovalInfo) redisTemplate.opsForValue().get("easy_" + shopOrderNo);
        } else {
            getApprovalInfoUntilTheEnd(shopOrderNo, checkStartDateTime);
        }

        return null;
    }

    // 결제 승인 정보 확인될 때까지 반복해서 조회 시도 함수
    private EasyPaymentApprovalInfo getApprovalInfoUntilTheEnd(String shopOrderNo, LocalDateTime checkStartDateTime) throws InterruptedException {
        Thread.sleep(2000);

        if (redisTemplate.opsForValue().get("easy_" + shopOrderNo) == null) {
            log.info("아직 저장되지 않음");

            if (LocalDateTime.now().minusMinutes(1).isAfter(checkStartDateTime)) {
                log.info("결제 승인 정보가 조회되지 않아 결제를 중단합니다.");
                return null;
            }

            return getApprovalInfoUntilTheEnd(shopOrderNo, checkStartDateTime);
        } else {
            log.info("저장됨! : {}", shopOrderNo);
            return (EasyPaymentApprovalInfo) redisTemplate.opsForValue().get("easy_" + shopOrderNo);
        }
    }
}
