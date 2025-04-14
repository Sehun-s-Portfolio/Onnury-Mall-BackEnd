package com.onnury.payment.service;

import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.member.domain.Member;
import com.onnury.payment.domain.*;
import com.onnury.payment.repository.*;
import com.onnury.payment.request.*;
import com.onnury.payment.response.CancelPaymentResponseDto;
import com.onnury.payment.response.OnnuryPaymentApprovalInfo;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
//import javax.transaction.Transactional;
import java.io.DataInputStream;
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
public class BizService {
    private final BizPointCodeccService bizPointCodeccService;
    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentZppReqRepository paymentZppReqRepository;
    private final PaymentApprovalRepository paymentApprovalRepository;
    private final PaymentRepository paymentRepository;
    private final OrderInProductRepository orderInProductRepository;
    private final OrderInDeliveryAddPriceRepository orderInDeliveryAddPriceRepository;
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
    private String cancelUrl; // 결제 취소 호출 url

    @Value("${onnury.biz.payment.fail.page.url}")
    private String failUrl; // 결제 실패 호출 url

    @Value("${onnury.biz.mid}")
    private String _MID_;


    // 온누리 결제 준비 service
    public JSONObject reserve(PaymentOnnuryPayRequestDto paymentOnnuryPayRequestDto) throws Exception {
        // 가맹점코드
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        Date d1 = new Date();

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(d1);
//        // 9시간 더하기
//        calendar.add(Calendar.HOUR_OF_DAY, 9);
//        Date newDate = calendar.getTime();
        String merchantOrderDt = sf1.format(d1);
        JSONObject reqParams3 = new JSONObject();
        System.out.println("=======================주문 날짜====================");
        System.out.println(d1);
        System.out.println(merchantOrderDt);
        // 공통부의 EV값
        reqParams3.put("merchantOrderDt", merchantOrderDt);
        reqParams3.put("merchantOrderID", paymentOnnuryPayRequestDto.getMerchantOrderID());
        reqParams3.put("merchantUserKey", paymentOnnuryPayRequestDto.getMerchantUserKey());
        reqParams3.put("productName", paymentOnnuryPayRequestDto.getProductName());
        reqParams3.put("quantity", paymentOnnuryPayRequestDto.getQuantity());
        reqParams3.put("totalAmount", paymentOnnuryPayRequestDto.getTotalAmount());
        reqParams3.put("taxFreeAmount", 0);
        reqParams3.put("vatAmount", 0);
        reqParams3.put("complexYn", "N");
        reqParams3.put("approvalURL", approvalUrl);
        reqParams3.put("cancelURL", cancelUrl);
        reqParams3.put("failURL", failUrl + "/" + paymentOnnuryPayRequestDto.getMerchantOrderID());
        ArrayList<JSONObject> reqList = new ArrayList<>();

        paymentOnnuryPayRequestDto.getProductItems().forEach(eachProductItem -> {
            JSONObject productInfo = new JSONObject();

            productInfo.put("seq", eachProductItem.getSeq());
            productInfo.put("category", "E");
            productInfo.put("frc_cd", eachProductItem.getFrc_cd());
            productInfo.put("biz_no", eachProductItem.getBiz_no());
            productInfo.put("name", eachProductItem.getName());
            productInfo.put("count", eachProductItem.getCount());
            productInfo.put("amount", eachProductItem.getAmount());

            reqList.add(productInfo);
        });

        reqParams3.put("productItems", reqList);

        String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams3.toString());
        String reqVV = bizPointCodeccService.getHmacSha256(reqParams3.toString());

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
        Date d = new Date();
        String date = sf.format(d);

        JSONObject postBody = new JSONObject();
        postBody.put("MID", _MID_);
        postBody.put("RQ_DTIME", date);
        postBody.put("TNO", "T" + merchantOrderDt);
        postBody.put("EV", reqEV);
        postBody.put("VV", reqVV);
        postBody.put("RC", "");
        postBody.put("RM", "");

        return postBody;
    }


    // 온누리 결제 승인 service
    @Transactional(transactionManager = "MasterTransactionManager")
    public JSONObject approval(HttpServletRequest request, NewPaymentRequestDto newPaymentRequestDto, List<PaymentProductListRequestDto> PaymentProductListRequestDto) throws Exception {
        log.info("온누리 결제 승인 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        JSONObject returnmsg = new JSONObject();
        OnnuryPaymentApprovalInfo onnuryinfo = (OnnuryPaymentApprovalInfo) redisTemplate.opsForValue().get(newPaymentRequestDto.getOrderNumber());

        log.info("주문 번호 : {}", newPaymentRequestDto.getOrderNumber());
        log.info("온누리 결제 승인 REDIS 정보 : {}", onnuryinfo);

        // 공통부의 EV값
        JSONObject json = new JSONObject();
        json.put("merchantOrderDt", onnuryinfo.getMerchantOrderDt());
        json.put("merchantOrderID", onnuryinfo.getMerchantOrderID());
        json.put("tid", onnuryinfo.getTid());
        json.put("totalAmount", onnuryinfo.getTotalAmount());
        json.put("token", onnuryinfo.getToken());

        String reqEV = bizPointCodeccService.biztotpayEncCode(json.toString());
        String reqVV = bizPointCodeccService.getHmacSha256(json.toString());
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
        Date d = new Date();
        String date = sf.format(d);

        JSONObject postBody = new JSONObject();
        postBody.put("MID", _MID_);
        postBody.put("RQ_DTIME", date);
        postBody.put("TNO", "T" + onnuryinfo.getMerchantOrderID() + "_approval");
        postBody.put("EV", reqEV);
        postBody.put("VV", reqVV);
        postBody.put("RC", "");
        postBody.put("RM", "");
        String url = BZPURL + "api_v1_payment_approval.jct";

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
            log.info("응답 메세지 데이터 존재");

            String temp = new String(resMessage, "UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> bizresult = mapper.readValue(temp, Map.class);

            if (bizresult.get("RC").equals("0000")) {
                log.info("{} : 옳바르게 읽어온 데이터", bizresult.get("RC"));

                Map<String, Object> bizresultEV = mapper.readValue(bizPointCodeccService.biztotpayDecCode((String) bizresult.get("EV")), Map.class);
                log.info("{}", bizresultEV);

                String redcode = (String) bizresultEV.get("code");
                if (redcode.equals("0000")) {

                    // 디비 저장
                    Map<String, Object> bizresultEVJsonData = (Map<String, Object>) bizresultEV.get("data");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
                    String convertDateTime = LocalDateTime.now().toString().substring(0, 19).replace("T", " ");
                    LocalDateTime dateTime = LocalDateTime.parse(convertDateTime, formatter); // 추출한 날짜 데이터에 포맷 형식 적용


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

                        }else {

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

                    Payment payment = Payment.builder()
                            .orderNumber(onnuryinfo.getMerchantOrderID())
                            .buyMemberLoginId(newPaymentRequestDto.getBuyMemberLoginId())
                            .receiver(newPaymentRequestDto.getReceiver())
                            .postNumber(newPaymentRequestDto.getPostNumber())
                            .address(newPaymentRequestDto.getAddress())
                            .message(newPaymentRequestDto.getMessage())
                            .receiverPhone(newPaymentRequestDto.getReceiverPhone())
                            .linkCompany(newPaymentRequestDto.getLinkCompany())
                            .onNuryStatementNumber((String) bizresultEVJsonData.get("tid"))
                            .onNuryApprovalPrice((Integer) bizresultEVJsonData.get("totalAmount"))
                            .creditApprovalPrice(0)
                            .creditStatementNumber("")
                            .totalApprovalPrice((Integer) bizresultEVJsonData.get("totalAmount"))
                            .orderedAt(LocalDateTime.now())
                            .build();

                    paymentRepository.save(payment);

                    PaymentApproval paymentApproval = PaymentApproval.builder()
                            .merchantOrderDt((String) bizresultEVJsonData.get("merchantOrderDt"))
                            .merchantOrderID((String) bizresultEVJsonData.get("merchantOrderID"))
                            .tid((String) bizresultEVJsonData.get("tid"))
                            .productName((String) bizresultEVJsonData.get("productName"))
                            .totalAmount((Integer) bizresultEVJsonData.get("totalAmount"))
                            .taxFreeAmount((Integer) bizresultEVJsonData.get("taxFreeAmount"))
                            .vatAmount((Integer) bizresultEVJsonData.get("vatAmount"))
                            .approvedAt((String) bizresultEVJsonData.get("approvedAt"))
                            .bankCd((String) bizresultEVJsonData.get("bankCd"))
                            .accountNo((String) bizresultEVJsonData.get("accountNo"))
                            .payMeasureTp((String) bizresultEVJsonData.get("payMeasureTp"))
                            .payZppNote((String) bizresultEVJsonData.get("payZppNote"))
                            .build();

                    paymentApprovalRepository.save(paymentApproval);

                    List<Map<String, Object>> payZppReqListJsonData = (List<Map<String, Object>>) bizresultEVJsonData.get("payZppReqList");

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

                    log.info("{} : 결제 승인 정보 저장 성공", redcode);

                    returnmsg.put("resCd", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("data"));

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

                    return returnmsg;

                } else {
                    log.info("{} : 파싱된 데이터가 옳바르지 않음", redcode);

                    returnmsg.put("resCd", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("msg"));
                    return returnmsg;
                }
            } else {
                log.info("{} : 옳바르지 않게 읽어온 데이터", bizresult.get("RC"));

                returnmsg.put("resCd", bizresult.get("RC"));
                returnmsg.put("data", bizresult.get("RM"));
                return returnmsg;
            }
        } else {
            log.info("응답 메세지 데이터 존재하지 않음");

            returnmsg.put("resCd", "9999");
            returnmsg.put("data", "POINT 시스템 에러");
            return returnmsg;
        }
    }


    // 온누리 결제 취소 service
    public Map<String, Object> cancel(HttpServletRequest request, List<CancelPaymentResponseDto> cancelInfoMap, List<String> productSeq, int onuryCanclePay) throws Exception {
        log.info("온누리 결제 취소 service");

        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date d1 = new Date();
        String merchantCancelDt = sf1.format(d1);
        String merchantCancelID = sf2.format(d1);

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

//        String _MID_ = (String) cancelInfoMap.get("pointMid"); // 가맹점코드
        Map<String, Object> returnmsg = new HashMap<>();
        String payr_ci = "";
        String payr_clph_no = "";

        JSONObject reqParams3 = new JSONObject();
        String partYn = "N";
        if (cancelInfoMap.size() != productSeq.size()) {
            partYn = "Y";
        }

        if (partYn.equals("N")) {
            // 공통부의 EV값
            reqParams3.put("merchantCancelDt", merchantCancelDt);
            reqParams3.put("merchantCancelID", merchantCancelID);
            reqParams3.put("merchantOrderDt", cancelInfoMap.get(0).getOrderedAt());
            reqParams3.put("merchantOrderID", cancelInfoMap.get(0).getOrderNumber());
            reqParams3.put("tid", cancelInfoMap.get(0).getOnNuryStatementNumber());
            reqParams3.put("totalAmount", cancelInfoMap.get(0).getOnNuryApprovalPrice());
            reqParams3.put("totalCancelAmount", onuryCanclePay);
            reqParams3.put("cancelVatAmount", 0);
            reqParams3.put("cancelTaxFreeAmount", 0);
            reqParams3.put("partYn", partYn);
        } else {
            reqParams3 = new JSONObject();
        }

        String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams3.toString());
        String reqVV = bizPointCodeccService.getHmacSha256(reqParams3.toString());

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
        Date d = new Date();
        String date = sf.format(d);


        JSONObject postBody = new JSONObject();
        postBody.put("MID", _MID_);
        postBody.put("RQ_DTIME", date);
        postBody.put("TNO", cancelInfoMap.get(0).getOrderNumber() + "_cancel");
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

                    /**
                     List<HashMap<String, Object>> cancelProducts = (List<HashMap<String, Object>>) cancelInfoMap.get("productItems");

                     // 기존 결제 이력 정보를 통한 결제 취소 데이터 저장 처리
                     paymentQueryData.cancelPaymentUpdate(authMember, reqParams3, bizresultEVJsonData, cancelProducts);
                     **/

                    returnmsg.put("code", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("data"));
                } else {
                    returnmsg.put("code", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("msg"));
                }
            } else {
                returnmsg.put("code", bizresult.get("RC"));
                returnmsg.put("data", bizresult.get("RM"));
            }
        } else {
            returnmsg.put("code", "9999");
            returnmsg.put("data", "POINT 시스템 에러");

        }

        return returnmsg;
    }


    // 온누리 결제 취소 service
    public Map<String, Object> cancelOnnuryPayment(HttpServletRequest request, Map<String, Object> cancelInfoMap) throws Exception {
        log.info("온누리 결제 취소 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        Member authMember = jwtTokenProvider.getMemberFromAuthentication();

        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date d1 = new Date();
        String merchantCancelDt = sf1.format(d1);
        String merchantCancelID = sf2.format(d1);

//        String _MID_ = (String) cancelInfoMap.get("pointMid"); // 가맹점코드
        Map<String, Object> returnmsg = new HashMap<>();
        JSONObject reqParams3 = new JSONObject();

        reqParams3.put("merchantCancelDt", merchantCancelDt);
        reqParams3.put("merchantCancelID", merchantCancelID);
        reqParams3.put("merchantOrderDt", cancelInfoMap.get("merchantOrderDt"));
        reqParams3.put("merchantOrderID", cancelInfoMap.get("merchantOrderID"));
        reqParams3.put("tid", cancelInfoMap.get("tid"));
        reqParams3.put("totalAmount", cancelInfoMap.get("totalAmount"));
        reqParams3.put("totalCancelAmount", cancelInfoMap.get("totalCancelAmount"));
        reqParams3.put("cancelVatAmount", cancelInfoMap.get("cancelVatAmount"));
        reqParams3.put("cancelTaxFreeAmount", cancelInfoMap.get("cancelTaxFreeAmount"));
        reqParams3.put("partYn", cancelInfoMap.get("partYn"));

        if (cancelInfoMap.get("partYn").equals("Y")) {
            reqParams3.put("productItems", cancelInfoMap.get("productItems"));
        }

        String reqEV = bizPointCodeccService.biztotpayEncCode(reqParams3.toString());
        String reqVV = bizPointCodeccService.getHmacSha256(reqParams3.toString());

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
        Date d = new Date();
        String date = sf.format(d);


        JSONObject postBody = new JSONObject();
        postBody.put("MID", _MID_);
        postBody.put("RQ_DTIME", date);
        postBody.put("TNO", cancelInfoMap.get("merchantOrderID") + "_cancel");
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

                    List<HashMap<String, Object>> cancelProducts = (List<HashMap<String, Object>>) cancelInfoMap.get("productItems");

//                     // 기존 결제 이력 정보를 통한 결제 취소 데이터 저장 처리
//                     paymentQueryData.cancelPaymentUpdate(authMember, bizresultEVJsonData, cancelProducts);

                    returnmsg.put("code", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("data"));
                } else {
                    returnmsg.put("code", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("msg"));
                }
            } else {
                returnmsg.put("code", bizresult.get("RC"));
                returnmsg.put("data", bizresult.get("RM"));
            }
        } else {
            returnmsg.put("code", "9999");
            returnmsg.put("data", "POINT 시스템 에러");

        }

        return returnmsg;
    }


    public ResponseEntity<ResponseBody> status(Map<String, Object> statusInfoMap) throws Exception {

        String _MID_ = (String) statusInfoMap.get("pointMid"); // 가맹점 코드
        Map<String, Object> returnmsg = new HashMap<>();
        JSONObject jsonObject = new JSONObject(statusInfoMap);

        String reqEV = bizPointCodeccService.biztotpayEncCode(jsonObject.toString());
        String reqVV = bizPointCodeccService.getHmacSha256(jsonObject.toString());
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddkkmmss");
        Date d = new Date();
        String date = sf.format(d);

        jsonObject.remove("pointMid");

        JSONObject postBody = new JSONObject();
        postBody.put("MID", _MID_);
        postBody.put("RQ_DTIME", date);
        postBody.put("TNO", jsonObject.get("merchantTranDt") + "_status");
        postBody.put("EV", reqEV);
        postBody.put("VV", reqVV);
        postBody.put("RC", "");
        postBody.put("RM", "");

        String url = BZPURL + "api_v1_payment_status.jct "; //결제준비URL

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
                    returnmsg.put("code", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("data"));
                } else {
                    returnmsg.put("code", bizresultEV.get("code"));
                    returnmsg.put("data", bizresultEV.get("msg"));

                }
            } else {
                returnmsg.put("code", bizresult.get("RC"));
                returnmsg.put("data", bizresult.get("RM"));
            }
        } else {
            returnmsg.put("code", "9999");
            returnmsg.put("data", "POINT 시스템 에러");

        }
        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, returnmsg), HttpStatus.OK);
    }


    // 온누리 비즈 페이먼트 결제 정보 조회 service
    public OnnuryPaymentApprovalInfo getOnnuryPaymentApprovalInfo(HttpServletRequest request, String merchantOrderID) throws InterruptedException {
        log.info("온누리 비즈 페이먼트 결제 승인 정보 조회 (성공 / 실패 / 취소) service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        LocalDateTime checkStartDateTime = LocalDateTime.now();

        if (!getApprovalInfoUntilTheEnd(merchantOrderID, checkStartDateTime).getTid().isEmpty()) {
            return (OnnuryPaymentApprovalInfo) redisTemplate.opsForValue().get(merchantOrderID);
        } else {
            getApprovalInfoUntilTheEnd(merchantOrderID, checkStartDateTime);
        }

        return null;
    }


    // 결제 승인 정보 확인될 때까지 반복해서 조회 시도 함수
    private OnnuryPaymentApprovalInfo getApprovalInfoUntilTheEnd(String merchantOrderID, LocalDateTime checkStartDateTime) throws InterruptedException {
        Thread.sleep(2000);

        if (redisTemplate.opsForValue().get(merchantOrderID) == null) {
            log.info("아직 저장되지 않음");

            if (LocalDateTime.now().minusMinutes(2).isAfter(checkStartDateTime)) {
                log.info("결제 승인 정보가 조회되지 않아 결제를 중단합니다.");
                return null;
            }

            return getApprovalInfoUntilTheEnd(merchantOrderID, checkStartDateTime);
        } else {
            log.info("저장됨! : {}", merchantOrderID);

            return (OnnuryPaymentApprovalInfo) redisTemplate.opsForValue().get(merchantOrderID);
        }
    }
}
